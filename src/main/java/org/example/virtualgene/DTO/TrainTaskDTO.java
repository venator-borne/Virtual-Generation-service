package org.example.virtualgene.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.virtualgene.common.enums.ModelUsage;
import org.example.virtualgene.domain.DAO.Model;

import java.util.Base64;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TrainTaskDTO {
    private String taskId;
    private String name;
    private ModelUsage modelUsage;
    private String datasetPath;
    private String datasetBucket;

    public static TrainTaskDTO toTask(Model model, String path, String bucket) {
        String taskId = Base64.getEncoder().withoutPadding().encodeToString(model.getId().toString().getBytes());
        return new TrainTaskDTO(taskId, model.getName(), model.getUsage(), path, bucket);
    }
}
