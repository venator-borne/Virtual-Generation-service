package org.example.virtualgene.domain.DAO;

import lombok.Builder;
import lombok.Data;
import org.example.virtualgene.DTO.TrainDetailsDTO;
import org.example.virtualgene.common.enums.ModelStatus;
import org.example.virtualgene.common.enums.ModelUsage;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@Table("model")
public class Model {
    @Id
    private UUID id;
    private String name;
    private Boolean access;
    private ModelUsage usage;
    private ModelStatus status;
    private String detail;
    private UUID accountId;
    private UUID datasetId;
    private ZonedDateTime createTime;
}
