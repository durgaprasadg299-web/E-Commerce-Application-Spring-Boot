package com.hendisantika.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ProductRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank String description,
        @Min(0) int quantity,
        @NotNull @DecimalMin("0.0") Double price,
        @NotBlank @Size(max = 255) String brand,
        @Min(0) @Max(75) int discount,
        Set<Long> categoryIds,
        @NotBlank String image
) {
}
