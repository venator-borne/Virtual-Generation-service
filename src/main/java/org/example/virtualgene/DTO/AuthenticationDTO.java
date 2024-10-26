package org.example.virtualgene.DTO;

import lombok.Builder;

@Builder
public record AuthenticationDTO(String token, String feedback) {}
