package br.com.system.data.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AssistantRequestDTO(@NotBlank String message) {
}
