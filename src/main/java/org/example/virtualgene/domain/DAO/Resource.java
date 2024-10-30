package org.example.virtualgene.domain.DAO;

import lombok.Builder;
import lombok.Data;
import org.example.virtualgene.common.enums.ResourcesCategory;
import org.example.virtualgene.common.enums.ResourcesType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@Table("resource")
public class Resource {
    @Id
    private UUID id;
    private String identifier;
    private String name;
    private Boolean access;
    private ResourcesCategory category;
    private ResourcesType type;
    private UUID accountId;
    private ZonedDateTime createTime;
}
