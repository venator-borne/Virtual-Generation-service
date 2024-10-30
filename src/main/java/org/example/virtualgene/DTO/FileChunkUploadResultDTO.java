package org.example.virtualgene.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FileChunkUploadResultDTO {
    private String path;
    private List<Integer> uploadedChunks;
}
