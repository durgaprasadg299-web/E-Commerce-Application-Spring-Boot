package com.hendisantika.dto;

import java.util.List;

public record WishlistResponse(Long id, List<WishlistItemResponse> items) {
}
