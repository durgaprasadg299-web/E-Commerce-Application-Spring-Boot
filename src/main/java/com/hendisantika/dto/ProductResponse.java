package com.hendisantika.dto;

import java.util.Set;

public record ProductResponse(
        Long id,
        String name,
        String description,
        int quantity,
        Double price,
        String brand,
        int discount,
        String image,
        Set<String> categories
) {
}
