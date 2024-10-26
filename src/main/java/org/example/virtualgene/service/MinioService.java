package org.example.virtualgene.service;

import io.minio.*;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.XmlParserException;
import io.minio.messages.*;
import org.example.virtualgene.DTO.FileChunkDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class MinioService {
    @Autowired
    private MinioAsyncClient minioAsyncClient;

    public Mono<Boolean> reactBucketExistsAsync(String bucketName) {

        return Mono.fromFuture(() ->
        {
            try {
                return minioAsyncClient.bucketExists(
                        BucketExistsArgs.builder()
                                .bucket(bucketName)
                                .build());
            } catch (IOException | InsufficientDataException | XmlParserException | InternalException |
                     InvalidKeyException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Mono<Void> reactMakeBucketAsync(String bucketName) {

        return Mono.fromFuture(() ->
                {
                    try {
                        return minioAsyncClient.makeBucket(
                                MakeBucketArgs.builder()
                                        .bucket(bucketName)
                                        .build());
                    } catch (InsufficientDataException | XmlParserException | NoSuchAlgorithmException | IOException |
                             InvalidKeyException | InternalException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    public Mono<StatObjectResponse> reactStatObjectAsync(String bucketName, String objpath) {
        return Mono.fromFuture(() ->
        {
            try {
                return minioAsyncClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objpath)
                                .build());
            } catch (IOException | InsufficientDataException | XmlParserException | InternalException |
                     InvalidKeyException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Mono<ObjectWriteResponse> reactPutObjectAsync(String bucketName, FileChunkDTO chunk, String path) {

        return DataBufferUtils.join(chunk.getFile().content())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                    return Mono.fromFuture(() -> {
                        try {
                            return minioAsyncClient.putObject(
                                    PutObjectArgs.builder().bucket(bucketName).object(path).stream(
                                            inputStream, bytes.length, -1).build()
                            );
                        } catch (IOException | InsufficientDataException | XmlParserException | InternalException |
                                 InvalidKeyException | NoSuchAlgorithmException  e) {
                            throw new RuntimeException(e);
                        }
                    });
                });
    }

    public Mono<ObjectWriteResponse> reactComposeObjectAsync(int totalChunks, String bucket, String tempRootPath, String objPath) {

        List<ComposeSource> sources = new ArrayList<>();
        for (int i = 1; i <= totalChunks; i++) {
            sources.add(ComposeSource.builder().bucket(bucket).object(tempRootPath + String.valueOf(i)).build());
        }

        return Mono.fromFuture(() -> {
            try {
                return minioAsyncClient.composeObject(
                        ComposeObjectArgs.builder().bucket(bucket).object(objPath).sources(sources).build()
                );
            } catch (IOException | InsufficientDataException | XmlParserException | InternalException |
                     InvalidKeyException | NoSuchAlgorithmException  e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void removeChunk(String bucketName, String path) {
        try {
            minioAsyncClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName).object(path)
                            .build());
        } catch (InsufficientDataException | XmlParserException | NoSuchAlgorithmException | IOException |
                 InvalidKeyException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    public Mono<Boolean> reactRemoveObjectAsync(int totalChunks, String bucket, String tempRootPath) {
        return  Flux.range(1, totalChunks)
                .map(i -> tempRootPath + String.valueOf(i))
                .flatMap(obj -> Mono.fromFuture(() -> {
                    try {
                        return minioAsyncClient.removeObject(
                                RemoveObjectArgs.builder().bucket(bucket).object((String)obj).build()
                        );
                    } catch (InsufficientDataException | XmlParserException | NoSuchAlgorithmException |
                             InvalidKeyException | IOException | InternalException e) {
                        throw new RuntimeException(e);
                    }
                }).onErrorResume(Mono::error))
                .then(Mono.just(true));
    }

    public Mono<Void> reactSetBucketLifeCycle(String bucketName, String path, Integer chunkExpiration) {
        List<LifecycleRule> rules = new LinkedList<>();
        rules.add(new LifecycleRule(
                Status.ENABLED,
                null,
                new Expiration((ResponseDate) null, chunkExpiration, null),
                new RuleFilter(path),
                path,
                null,
                null,
                null));

        LifecycleConfiguration config = new LifecycleConfiguration(rules);

        return Mono.fromFuture(() -> {
            try {
                minioAsyncClient.getBucketLifecycle(GetBucketLifecycleArgs.builder().bucket(bucketName).build());
                return minioAsyncClient.setBucketLifecycle(
                        SetBucketLifecycleArgs.builder().bucket(bucketName).config(config).build());
            } catch (InsufficientDataException | XmlParserException | NoSuchAlgorithmException | IOException |
                     InvalidKeyException | InternalException e) {
                throw new RuntimeException(e);
            }
        });

    }
}
