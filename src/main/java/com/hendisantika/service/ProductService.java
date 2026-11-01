package com.hendisantika.service;

import com.hendisantika.dto.ProductRequest;
import com.hendisantika.model.Carousel;
import com.hendisantika.model.Category;
import com.hendisantika.model.Coupon;
import com.hendisantika.model.Product;
import com.hendisantika.repository.CategoryRepository;
import com.hendisantika.repository.CouponRepository;
import com.hendisantika.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-ecommerce
 * User: powercommerce
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 7/30/22
 * Time: 20:41
 * To change this template use File | Settings | File Templates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CouponRepository couponRepository;

    public void saveProductToDB(MultipartFile file, String name, String description, int quantity
            , Double price, String brand, String categories) {
        validateProductFields(name, description, quantity, price, brand);
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A product image is required");
        }
        Product p = new Product();
        String fileName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (fileName.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image filename");
        }
        try {
            p.setImage(resizeImageForUse(Base64.getEncoder().encodeToString(file.getBytes()), 400, 400));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read product image", e);
        }
        p.setDescription(description);

        p.setName(name);
        p.setPrice(price);
        p.setBrand(brand);
        p.setQuantity(quantity);
        Coupon c = new Coupon();
        c.setDiscount(0);
        p.setDiscount(c);
        p.setCategories(parseCategoryIds(categories));
        productRepository.save(p);
    }

    private Set<Category> parseCategoryIds(String categoryIds) {
        Set<Long> ids = new HashSet<>();
        if (categoryIds != null && !categoryIds.isBlank()) {
            try {
                for (String id : categoryIds.split(",")) {
                    ids.add(Long.parseLong(id.trim()));
                }
            } catch (NumberFormatException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category IDs must be numeric", exception);
            }
        }
        return resolveCategories(ids);
    }

    private void validateProductFields(String name, String description, int quantity, Double price, String brand) {
        if (name == null || name.isBlank() || description == null || description.isBlank()
                || brand == null || brand.isBlank()
                || quantity < 0 || price == null || price < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product name, description, brand, non-negative quantity, and non-negative price are required");
        }
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getCatalogProducts(String search, String brand, Long categoryId, Double maxPrice) {
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        String normalizedBrand = brand == null || brand.isBlank() ? null : brand.trim();
        if (maxPrice != null && maxPrice < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum price must not be negative");
        }
        return productRepository.findCatalogProducts(normalizedSearch, normalizedBrand, categoryId, maxPrice);
    }

    public void deleteProductById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with " + id + " not found");
        }
        productRepository.deleteById(id);
    }

    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with " + id + " not found"));
        applyRequest(product, request);
        return productRepository.save(product);
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setDescription(request.description().trim());
        product.setQuantity(request.quantity());
        product.setPrice(request.price());
        product.setBrand(request.brand().trim());
        product.setImage(resizeImageForUse(normalizeImage(request.image()), 400, 400));
        if (product.getDiscount() == null) {
            product.setDiscount(new Coupon());
        }
        product.getDiscount().setDiscount(request.discount());
        product.setCategories(resolveCategories(request.categoryIds()));
    }

    private String normalizeImage(String image) {
        String encodedImage = image.trim();
        int dataUrlSeparator = encodedImage.indexOf(',');
        if (encodedImage.startsWith("data:") && dataUrlSeparator >= 0) {
            encodedImage = encodedImage.substring(dataUrlSeparator + 1);
        }
        try {
            return Base64.getEncoder().encodeToString(Base64.getDecoder().decode(encodedImage));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image must be valid Base64 data", exception);
        }
    }

    private Set<Category> resolveCategories(Set<Long> categoryIds) {
        Set<Category> categories = new HashSet<>();
        if (categoryIds == null) {
            return categories;
        }
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Category with " + categoryId + " not found"));
            categories.add(category);
        }
        return categories;
    }

    public void changeProductName(Long id, String name) {
        Product p = productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with " + id + " not found"));
        p.setName(name);
        productRepository.save(p);
    }

    public void changeProductDescription(Long id, String description) {
        Product p = productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with " + id + " not found"));
        p.setDescription(description);
        productRepository.save(p);
    }

    public void changeProductPrice(Long id, Double price) {
        Product p = productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with " + id + " not found"));
        p.setPrice(price);
        productRepository.save(p);
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public void addImageToProduct(MultipartFile file, Long id) {
        Product p = productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with " + id + " not found"));
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A product image is required");
        }
        Carousel carousel = new Carousel();
        String fileName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (fileName.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image filename");
        }
        try {
            carousel.setImage(resizeImageForUse(Base64.getEncoder().encodeToString(file.getBytes()), 400, 400));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read product image", e);
        }
        p.getCarousel().add(carousel);
        productRepository.save(p);
    }

    public void changeProductQuantity(Long id, int quantity) {
        Product p = productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with " + id + " not found"));
        p.setQuantity(quantity);
        productRepository.save(p);

    }

    public void saveProductDiscount(Long id, int discount) {
        Product p = productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with " + id + " not found"));
        if (p.getDiscount() == null) {
            Coupon c = new Coupon();
            c.setDiscount(discount);
            p.setDiscount(c);
        } else {
            p.getDiscount().setDiscount(discount);
        }
        productRepository.save(p);
    }

    public void changeProductDiscount(Long id, int discount) {
        Product p = productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with " + id + " not found"));
        p.getDiscount().setDiscount(discount);
        productRepository.save(p);
    }

    public String resizeImageForUse(String src, int width, int height) {
        BufferedImage image = base64ToBufferedImage(src);
        try {
            image = resizeImage(image, width, height);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to process product image", e);
        }
        return bufferedImageTobase64(image);
    }

    private BufferedImage resizeImage(BufferedImage image, int width, int height) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Thumbnails.of(image).size(width, height).outputFormat("JPEG").outputQuality(1).toOutputStream(outputStream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] data = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        return ImageIO.read(inputStream);
    }

    private BufferedImage base64ToBufferedImage(String base64Img) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64Img)));
            if (image == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product image must be a supported image format");
            }
            return image;
        } catch (IllegalArgumentException | IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product image data is invalid", exception);
        }
    }

    private String bufferedImageTobase64(BufferedImage image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            if (!ImageIO.write(image, "JPEG", Base64.getEncoder().wrap(out))) {
                throw new IOException("No JPEG image writer is available");
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to encode product image", e);
        }
        return out.toString(java.nio.charset.StandardCharsets.US_ASCII);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with " + id + " not found"));
    }

    public List<Product> searchProductByNameLike(String value) {
        return productRepository.findByNameContainingIgnoreCase(value);
    }

    public List<String> getAllBrands() {
        return productRepository.findAllBrandsDistincts();
    }

    public Product getProductWithBiggestDiscount() {
        Coupon discount = couponRepository.findMax();
        if (discount == null) {
            return null;
        }
        List<Product> products = productRepository.findAll();
        Product featuredProduct = null;
        for (Product p : products) {
            if (p.getDiscount().equals(discount)) {
                featuredProduct = p;
                break;
            }
        }
        return featuredProduct;
    }
}
