package org.example.virtualgene.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.http.codec.multipart.FilePart;

@Data
public class FileChunkDTO {
    @NotNull(message = "chunkNumber cannot be null")
    private Integer chunkNumber;
    @NotNull(message = "chunkSize cannot be null")
    private Float chunkSize;
    @NotNull(message = "currentChunkSize cannot be null")
    private Float currentChunkSize;
    @NotNull(message = "totalChunks cannot be null")
    private Integer totalChunks;
    @Pattern(regexp = "^[a-fA-F0-9]{32}$", message = "identifier is invalid")
    private String identifier;
    @NotBlank(message = "filename cannot be null")
    private String filename;
    private String fileType;
    private String relativePath;
    @NotNull(message = "totalSize cannot be null")
    private Long totalSize;
    private FilePart file;
    private String type;
    @NotNull(message = "chunkIdentifier cannot be null")
    private String chunkIdentifier;
}
