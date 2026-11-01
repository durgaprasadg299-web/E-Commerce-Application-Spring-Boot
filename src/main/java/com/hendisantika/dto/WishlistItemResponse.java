package com.hendisantika.dto;

public record WishlistItemResponse(
        Long id,
        Long productId,
        String name,
        String brand,
        Double price,
        String image
) {
}
