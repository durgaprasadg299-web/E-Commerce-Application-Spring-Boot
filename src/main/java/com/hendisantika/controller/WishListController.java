package com.hendisantika.controller;

import com.hendisantika.service.WishListService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WishListController {
    private final WishListService wishListService;

    @PostMapping(path = {"/addtowishlist", "/addToWishlist"}, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String addToWishList(@RequestParam("productId") Long productId, HttpServletRequest request) {
        wishListService.addProduct(productId, getOrCreateToken(request.getSession(true)));
        return "redirect:/shoppingCart#wishlist";
    }

    @PostMapping({"/addtowishlist/{id}", "/addToWishlist/{id}"})
    public String addToWishListById(@PathVariable Long id, HttpServletRequest request) {
        wishListService.addProduct(id, getOrCreateToken(request.getSession(true)));
        return "redirect:/shoppingCart#wishlist";
    }

    @PostMapping({"/clearWishList", "/clearwishlist"})
    public String clearWishList(HttpSession session) {
        wishListService.clear(getExistingToken(session));
        session.removeAttribute("sessionTokenWishList");
        return "redirect:/shoppingCart";
    }

    @PostMapping({"/removeWishListItem/{id}", "/removewishlistitem/{id}"})
    public String removeItem(@PathVariable Long id, HttpSession session) {
        wishListService.removeItem(id, getExistingToken(session));
        return "redirect:/shoppingCart#wishlist";
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
