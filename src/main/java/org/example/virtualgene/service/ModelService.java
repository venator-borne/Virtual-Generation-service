package org.example.virtualgene.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.virtualgene.DTO.TrainDTO;
import org.example.virtualgene.DTO.TrainTaskDTO;
import org.example.virtualgene.domain.DAO.Model;
import org.example.virtualgene.domain.DAO.Resource;
import org.example.virtualgene.domain.ModelRepository;
import org.example.virtualgene.domain.ResourceRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.PrivateKey;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class ModelService {

    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Value("${app.train.exchange}")
    private String trainExchange;
    @Value("${app.train.routing-key}")
    private String trainRoutingKey;

    public Mono<Void> train(TrainDTO trainDTO, UUID id) {
        return resourceRepository.findByIdentifierAndAccountId(trainDTO.getIdentifier(), id)
                .switchIfEmpty(Mono.error(new RuntimeException("Cannot find dataset")))
                .flatMap(d -> modelRepository.save(Model.builder().name(trainDTO.getName()).usage(trainDTO.getUsage()).createTime(ZonedDateTime.now(ZoneId.of("UTC"))).access(trainDTO.getAccess()).datasetId(d.getId()).accountId(id).build())
                        .map(m -> TrainTaskDTO.toTask(m, d.getPath(), d.getBucket())))
                .map(t -> {
                    try {
                        String task = new ObjectMapper().writeValueAsString(t);
                        Message message = MessageBuilder.withBody(task.getBytes()).setContentType(MessageProperties.CONTENT_TYPE_JSON).setMessageId(t.getTaskId()).build();
                        rabbitTemplate.convertAndSend(trainExchange, trainRoutingKey + t.getModelUsage(), message);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    return t;
                }).then();
    }
}
