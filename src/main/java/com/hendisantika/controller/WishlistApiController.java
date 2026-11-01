package com.hendisantika.controller;

import com.hendisantika.dto.WishlistItemRequest;
import com.hendisantika.dto.WishlistItemResponse;
import com.hendisantika.dto.WishlistResponse;
import com.hendisantika.model.WishList;
import com.hendisantika.service.WishListService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistApiController {
    private final WishListService wishListService;

    @GetMapping
    public WishlistResponse getWishlist(HttpSession session) {
        return toResponse(wishListService.getWishListBySessionToken(getOrCreateToken(session)));
    }

    @PostMapping("/items")
    public WishlistResponse addItem(@Valid @RequestBody WishlistItemRequest request, HttpSession session) {
        WishList wishList = wishListService.addProduct(request.productId(), getOrCreateToken(session));
        return toResponse(wishList);
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable Long itemId, HttpSession session) {
        wishListService.removeItem(itemId, getExistingToken(session));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearWishlist(HttpSession session) {
        wishListService.clear(getExistingToken(session));
        session.removeAttribute("sessionTokenWishList");
    }

    static WishlistResponse toResponse(WishList wishList) {
        if (wishList == null) {
            return new WishlistResponse(null, List.of());
        }
        List<WishlistItemResponse> items = wishList.getItems().stream()
                .map(item -> new WishlistItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getBrand(),
                        item.getProduct().getPrice(),
                        item.getProduct().getImage()))
                .toList();
        return new WishlistResponse(wishList.getId(), items);
    }

    private static String getOrCreateToken(HttpSession session) {
        String token = (String) session.getAttribute("sessionTokenWishList");
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute("sessionTokenWishList", token);
        }
        return token;
    }

    private static String getExistingToken(HttpSession session) {
        return (String) session.getAttribute("sessionTokenWishList");
    }
}
