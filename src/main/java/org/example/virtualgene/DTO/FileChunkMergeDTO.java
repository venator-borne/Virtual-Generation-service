package org.example.virtualgene.DTO;

import lombok.Builder;
import lombok.Data;
import org.example.virtualgene.common.enums.ResourcesCategory;
import org.example.virtualgene.common.enums.ResourcesType;

@Data
@Builder
public class FileChunkMergeDTO {
    private String name;
    private String identifier;
    private Integer totalChunks;
    private ResourcesCategory resourcesCategory;
    private ResourcesType resourcesType;
    private Boolean access;
}
