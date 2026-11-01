package com.hendisantika.service;

import com.hendisantika.model.Product;
import com.hendisantika.model.WishList;
import com.hendisantika.model.WishListItem;
import com.hendisantika.repository.WishListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class WishListService {
    private final WishListRepository wishListRepository;
    private final ProductService productService;

    @Transactional
    public WishList addProduct(Long productId, String sessionToken) {
        Product product = productService.getProductById(productId);
        WishList wishList = wishListRepository.findBySessionToken(sessionToken);
        if (wishList == null) {
            wishList = new WishList();
            wishList.setSessionToken(sessionToken);
            wishList.setDate(new Date());
            wishList.setItems(new ArrayList<>());
        }

        boolean alreadyAdded = wishList.getItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));
        if (!alreadyAdded) {
            WishListItem item = new WishListItem();
            item.setDate(new Date());
            item.setProduct(product);
            wishList.getItems().add(item);
        }
        return wishListRepository.save(wishList);
    }

    @Transactional(readOnly = true)
    public WishList getWishListBySessionToken(String sessionToken) {
        if (sessionToken == null || sessionToken.isBlank()) {
            return null;
        }
        return wishListRepository.findBySessionToken(sessionToken);
    }

    @Transactional
    public void removeItem(Long itemId, String sessionToken) {
        WishList wishList = getRequiredWishList(sessionToken);
        boolean removed = wishList.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist item not found");
        }
        wishListRepository.save(wishList);
    }

    @Transactional
    public void clear(String sessionToken) {
        if (sessionToken == null || sessionToken.isBlank()) {
            return;
        }
        WishList wishList = wishListRepository.findBySessionToken(sessionToken);
        if (wishList != null) {
            wishListRepository.delete(wishList);
        }
    }

    private WishList getRequiredWishList(String sessionToken) {
        if (sessionToken == null || sessionToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found");
        }
        WishList wishList = wishListRepository.findBySessionToken(sessionToken);
        if (wishList == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found");
        }
        return wishList;
    }
}
