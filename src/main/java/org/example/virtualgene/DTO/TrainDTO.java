package org.example.virtualgene.DTO;

import lombok.Data;
import org.example.virtualgene.common.enums.ModelUsage;

@Data
public class TrainDTO {
    private String identifier;
    private String name;
    private Boolean access;
    private ModelUsage usage;
}
