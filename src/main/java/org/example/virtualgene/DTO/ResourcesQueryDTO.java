package org.example.virtualgene.DTO;

import lombok.Data;
import org.example.virtualgene.common.enums.ResourcesCategory;
import org.example.virtualgene.common.enums.ResourcesType;

import java.util.List;

@Data
public class ResourcesQueryDTO {
    private Integer page;
    private Integer size;
    private List<ResourcesType> types;
    private List<ResourcesCategory> categories;
}
