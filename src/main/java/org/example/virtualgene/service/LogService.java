package org.example.virtualgene.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;

@Service
public class LogService {

    @Autowired
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    @Value("${redis.app.log.chunkSize}")
    private Integer logChunkSize;
    @Value("${redis.app.log.retry}")
    private Integer logRetry;

    public Flux<Map<String, String>> getAllTrainedLogsFromStreamGroup(String streamId) {
        ReactiveStreamOperations<String, String, String> xops = reactiveRedisTemplate.opsForStream();
        return xops.range(streamId, Range.closed("-", "+")).map(Record::getValue);
    }

    public Flux<ServerSentEvent<String>> getTrainingLogsFromStreamGroup(String streamId, String groupId, String lastEventId) {
        ReactiveStreamOperations<String, String, String> xops = reactiveRedisTemplate.opsForStream();
        return checkGroupExitsAndCreate(streamId, groupId)
                .thenMany(readAll(streamId, groupId, lastEventId))
                .flatMap(r -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonValue;
                    try {
                        jsonValue = objectMapper.writeValueAsString(r.getValue());
                    } catch (JsonProcessingException e) {
                        jsonValue = "Error converting log to JSON";
                    }
                    return xops.acknowledge(groupId, r).thenReturn(ServerSentEvent.<String>builder()
                            .event("message")
                            .data(jsonValue)
                            .build());
                }).switchIfEmpty(Mono.just(ServerSentEvent.<String>builder().event("message").data("").build()));
    }

    private Mono<Boolean> checkGroupExitsAndCreate(String streamId, String groupId) {
        return reactiveRedisTemplate.execute(connection -> connection.streamCommands().xInfoGroups(ByteBuffer.wrap(streamId.getBytes())))
                .retryWhen(Retry.backoff(logRetry, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof RedisException))
                .flatMap(xInfoGroup -> {
                    boolean exist = groupId.equals(xInfoGroup.groupName());
                    return Mono.just(exist);
                }).any(f -> f)
                .flatMap(f -> {
                    if (!f) {
                        ReactiveStreamOperations<String, Object, Object> xops = reactiveRedisTemplate.opsForStream();
                        return xops.createGroup(streamId, ReadOffset.from("0-0"), groupId).then(Mono.just(true)).onErrorResume(e -> Mono.just(false));
                    } else return Mono.just(f);
                });
    }

    private Flux<MapRecord<String, String, String>> readFromStart(String streamId, String groupId) {
        ReactiveStreamOperations<String, String, String> xops = reactiveRedisTemplate.opsForStream();
        return xops.groups(streamId).filter(g -> g.groupName().equals(groupId)).flatMap(f -> {
            String lastDeliveredId = f.lastDeliveredId();
            return xops.range(streamId, Range.closed("0", lastDeliveredId));
        });
    }

    private Flux<MapRecord<String, String, String>> readFromNew(String streamId, String groupId) {
        ReactiveStreamOperations<String, String, String> xops = reactiveRedisTemplate.opsForStream();
        StreamReadOptions readOptions = StreamReadOptions.empty().count(logChunkSize);
        return xops.read(Consumer.from(groupId, "s1"), readOptions, StreamOffset.create(streamId, ReadOffset.lastConsumed()));
    }

    private Flux<MapRecord<String, String, String>> readAll(String streamId, String groupId, String lastEventId) {
        Flux<MapRecord<String, String, String>> hisRecord = lastEventId == null ? readFromStart(streamId, groupId) : Flux.empty();
        Flux<MapRecord<String, String, String>> newRecord = Flux.interval(Duration.ofMillis(100)).flatMap(tick -> readFromNew(streamId, groupId));
        return Flux.concat(hisRecord, newRecord);
    }
}
