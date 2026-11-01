package com.hendisantika.controller;

import com.hendisantika.dto.WishlistItemRequest;
import com.hendisantika.dto.WishlistResponse;
import com.hendisantika.model.WishList;
import com.hendisantika.service.WishListService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WishlistApiAliasController {
    private final WishListService wishListService;

    @PostMapping(path = {"/addtowishlist", "/addToWishlist"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public WishlistResponse addToWishlist(@Valid @RequestBody WishlistItemRequest request, HttpSession session) {
        String token = getOrCreateToken(session);
        WishList wishList = wishListService.addProduct(request.productId(), token);
        return WishlistApiController.toResponse(wishList);
    }

    @DeleteMapping({"/clearWishList", "/clearwishlist"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearWishlist(HttpSession session) {
        String token = (String) session.getAttribute("sessionTokenWishList");
        wishListService.clear(token);
        session.removeAttribute("sessionTokenWishList");
    }

    private static String getOrCreateToken(HttpSession session) {
        String token = (String) session.getAttribute("sessionTokenWishList");
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute("sessionTokenWishList", token);
        }
        return token;
    }
}
