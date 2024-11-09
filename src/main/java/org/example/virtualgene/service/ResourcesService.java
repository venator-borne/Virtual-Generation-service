package org.example.virtualgene.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.virtualgene.DTO.FileChunkDTO;
import org.example.virtualgene.DTO.FileChunkMergeDTO;
import org.example.virtualgene.DTO.FileChunkUploadResultDTO;
import org.example.virtualgene.common.enums.ResourcesCategory;
import org.example.virtualgene.common.enums.ResourcesType;
import org.example.virtualgene.domain.DAO.Resource;
import org.example.virtualgene.domain.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ResourcesService {

    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    private MinioService minioService;
    @Autowired
    private ReactiveRedisTemplate<String,Object> reactiveRedisTemplate;
    @Value("${minio.chunkTempFolder}")
    String chunkTempFolder;
    @Value("${minio.resourcesBucketName}")
    String resourcesBucketName;
    @Value("${minio.chunkExpiration}")
    Integer chunkExpiration;
    @Value("${redis.app.resources.chunkExpiration}")
    Integer resourcesChunkExpiration;

    public Mono<FileChunkUploadResultDTO> checkAndRetrieveUploadedResources(String identifier, String filename, UUID id) {
        String storageId = generateEncryptedId(id, identifier);
        String objectPath = getMinioObjectPath(storageId, filename);
        return resourceRepository.findByIdentifier(storageId)
                .map(r -> FileChunkUploadResultDTO.builder().path(objectPath).build())
                .switchIfEmpty(Mono.defer(() ->
                        getUploadedChunkList(storageId, filename)
                                .collectList()
                                .map(r -> FileChunkUploadResultDTO.builder().uploadedChunks(r).build())));

    }

    public Mono<Boolean> saveResources(FileChunkDTO fileChunkDTO, UUID id) {
        String storageId = generateEncryptedId(id, fileChunkDTO.getIdentifier());
        String tmpPath = getMinioTempPath(storageId, chunkTempFolder);
        String tmpObjectPath = getMinioTempObjectPath(storageId, chunkTempFolder, fileChunkDTO.getChunkNumber().toString());

        return minioService.reactBucketExistsAsync(resourcesBucketName)
                .filter(f -> !f)
                .flatMap(f -> minioService.reactMakeBucketAsync(resourcesBucketName))
                .then(minioService.reactPutObjectAsync(resourcesBucketName, fileChunkDTO, tmpObjectPath))
                .handle((res, sink) -> {
                    if (!res.etag().equals(fileChunkDTO.getChunkIdentifier())) {
                        minioService.removeChunk(resourcesBucketName, tmpObjectPath);
                        sink.error(new RuntimeException("file chunk upload error"));
                    } else {
                        sink.next(res);
                    }
                })
                .then(minioService.reactSetBucketLifeCycle(resourcesBucketName, tmpPath, chunkExpiration))
                .then(updateUploadedChunkList(storageId, fileChunkDTO.getFilename(), fileChunkDTO.getChunkNumber()))
                .thenReturn(true);
    }

    public Mono<Boolean> mergeResources(FileChunkMergeDTO fileChunkMergeDTO, UUID id) {
        String storageId = generateEncryptedId(id, fileChunkMergeDTO.getIdentifier());
        String objectPath = getMinioObjectPath(storageId, fileChunkMergeDTO.getName());
        String tmpPath = getMinioTempPath(storageId, chunkTempFolder);
        Resource resource = Resource.builder()
                .access(fileChunkMergeDTO.getAccess())
                .type(fileChunkMergeDTO.getResourcesType())
                .category(fileChunkMergeDTO.getResourcesCategory())
                .identifier(storageId)
                .name(fileChunkMergeDTO.getName())
                .accountId(id)
                .bucket(resourcesBucketName)
                .path(objectPath)
                .createTime(ZonedDateTime.now(ZoneId.of("UTC"))).build();

        return minioService.reactComposeObjectAsync(fileChunkMergeDTO.getTotalChunks(), resourcesBucketName, tmpPath, objectPath)
                .then(removeUploadedChunkList(storageId, fileChunkMergeDTO.getName()))
                .then(minioService.reactRemoveObjectAsync(fileChunkMergeDTO.getTotalChunks(), resourcesBucketName, tmpPath))
                .then(resourceRepository.save(resource))
                .map(f -> true)
                .onErrorResume(e -> {
                    log.error(e.getMessage());
                    return Mono.just(false);
                });
    }

    public Flux<Resource> findResource(int page, int size, UUID id) {
        return resourceRepository.findByAccountIdOrAccessIs(PageRequest.of(page, size), id, true);
    }

    public Mono<Long> countResource(List<ResourcesType> types, List<ResourcesCategory> categories, UUID id) {
        return resourceRepository.countByAccountIdOrAccessIsAndTypeInAndCategoryIn(id, true, types, categories);
    }

    private Mono<Boolean> updateUploadedChunkList(String identifier, String name, Integer value) {
        String key = name + ": " + identifier;
        return reactiveRedisTemplate.opsForList().leftPush(key, value)
                .flatMap(r -> reactiveRedisTemplate.expire(identifier, Duration.ofSeconds(resourcesChunkExpiration)));
    }

    private Flux<Integer> getUploadedChunkList(String identifier, String name) {
        String key = name + ": " + identifier;
        return reactiveRedisTemplate.opsForList().range(key,0,-1)
                .map(s->Integer.valueOf(s.toString()));
    }

    private Mono<Long> removeUploadedChunkList(String identifier, String name) {
        String key = name + ": " + identifier;
        return reactiveRedisTemplate.delete(key);
    }

    private static String generateEncryptedId(UUID id, String fileMd5) {
        String c = id.toString() + fileMd5;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(c.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getMinioObjectPath(String identifier, String filename) {
        return identifier + "/" + filename;
    }

    private static String getMinioTempPath(String identifier, String tempFolder) {
        return tempFolder + "/" + identifier + "/";
    }

    private static String getMinioTempObjectPath(String identifier, String tempFolder, String alias) {
        return tempFolder + "/" + identifier + "/" + alias;
    }
}
