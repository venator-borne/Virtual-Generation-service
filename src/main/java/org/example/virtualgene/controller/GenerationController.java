package org.example.virtualgene.controller;

import org.example.virtualgene.DTO.TrainDTO;
import org.example.virtualgene.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.nio.file.attribute.UserPrincipal;
import java.util.UUID;

@RestController
@RequestMapping("/virtual")
public class GenerationController {

    @Autowired
    private ModelService modelService;

    @PostMapping("/train")
    public Mono<ResponseEntity<?>> train(@RequestBody TrainDTO trainDTO, UserPrincipal user) {
        UUID id = UUID.fromString(user.getName());
        return modelService.train(trainDTO, id).then(Mono.just(ResponseEntity.ok().build()));
    }
}
