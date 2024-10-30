package org.example.virtualgene.controller;

import jakarta.validation.Valid;
import org.example.virtualgene.DTO.FileChunkDTO;
import org.example.virtualgene.DTO.FileChunkMergeDTO;
import org.example.virtualgene.DTO.FileChunkUploadResultDTO;
import org.example.virtualgene.domain.DAO.Resource;
import org.example.virtualgene.service.ResourcesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/resources")
public class ResourcesController {

    @Autowired
    private ResourcesService resourcesService;

    @GetMapping("/upload")
    public Mono<ResponseEntity<FileChunkUploadResultDTO>> upload(@RequestParam String identifier, @RequestParam String filename, Principal user) {
        UUID uuid = UUID.fromString(user.getName());
        return resourcesService.checkAndRetrieveUploadedResources(identifier, filename, uuid).map(ResponseEntity::ok);
    }

    @PostMapping("/upload")
    public Mono<ResponseEntity<Boolean>> uploadChunks(@Valid FileChunkDTO fileChunkDTO, Principal user) {
        UUID uuid = UUID.fromString(user.getName());
        return resourcesService.saveResources(fileChunkDTO, uuid).map(ResponseEntity::ok);
    }

    @PostMapping("/merge")
    public Mono<ResponseEntity<Boolean>> merge(@RequestBody FileChunkMergeDTO fileChunkMergeDTO, Principal user) {
        UUID uuid = UUID.fromString(user.getName());
        return resourcesService.mergeResources(fileChunkMergeDTO, uuid).map(ResponseEntity::ok);
    }

    @GetMapping("/files")
    public Flux<Resource> find(@RequestParam int page, @RequestParam int size, Principal user) {
        UUID uuid = UUID.fromString(user.getName());
        return resourcesService.findResource(page, size, uuid);
    }
}
