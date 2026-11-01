package com.hendisantika;

import com.hendisantika.model.Product;
import com.hendisantika.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SpringBootEcommerceApplicationTests {

    @Container
    private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:9.3.0")
            .withDatabaseName("shopDB")
            .withUsername("yu71")
            .withPassword("53cret");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProductRepository productRepository;

    @DynamicPropertySource
    static void registerMySQLProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void unauthenticatedRootRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void publicAuthPagesUseTheEditorialStylesheet() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/css/editorial.css")))
                .andExpect(content().string(containsString("Sign in")));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/css/editorial.css")))
                .andExpect(content().string(containsString("Create an account")));
    }

    @Test
    void authenticatedStorefrontRendersEditorialHeroAndStyles() throws Exception {
        mockMvc.perform(get("/").with(user("shopper")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("css/editorial.css")))
                .andExpect(content().string(containsString("Make room for things you love.")))
                .andExpect(content().string(containsString("Bag (")))
                .andExpect(content().string(containsString("<span>0</span>")))
                .andExpect(content().string(containsString("Add to bag")))
                .andExpect(content().string(containsString("₹")));
    }

    @Test
    void addToCartUpdatesTheSameSessionCartAndHeaderQuantity() throws Exception {
        Product product = productRepository.findByNameContainingIgnoreCase("Everyday Runner").get(0);
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/addToCart")
                        .session(session)
                        .with(user("shopper"))
                        .with(csrf())
                        .param("id", product.getId().toString())
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mockMvc.perform(post("/addToCart")
                        .session(session)
                        .with(user("shopper"))
                        .with(csrf())
                        .param("id", product.getId().toString())
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/").session(session).with(user("shopper")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bag (")))
                .andExpect(content().string(containsString("<span>3</span>")));

        mockMvc.perform(get("/shoppingCart").session(session).with(user("shopper")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(product.getName())))
                .andExpect(content().string(containsString("₹")))
                .andExpect(content().string(containsString("Subtotal:")));
    }

    @Test
    void catalogEndpointsReturnSeededBrandsAndProducts() throws Exception {
        mockMvc.perform(get("/api/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Casio"));

        mockMvc.perform(get("/api/products").param("brand", "Sony"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Studio Wireless Headphones"));

        mockMvc.perform(get("/api/brands/Nike/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Everyday Runner"));
    }

    @Test
    void wishlistApiAddsDeduplicatesAndClearsSessionWishlist() throws Exception {
        Product product = productRepository.findByNameContainingIgnoreCase("Everyday Runner").get(0);
        MockHttpSession session = new MockHttpSession();
        String requestBody = "{\"productId\":" + product.getId() + "}";

        mockMvc.perform(post("/api/wishlist/items")
                        .session(session)
                        .with(user("shopper"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Everyday Runner"));

        mockMvc.perform(post("/addtowishlist")
                        .session(session)
                        .with(user("shopper"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1));

        mockMvc.perform(delete("/clearWishList")
                        .session(session)
                        .with(user("shopper"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/wishlist")
                        .session(session)
                        .with(user("shopper")))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":null,\"items\":[]}"));
    }

    @Test
    void wishlistBrowserActionsArePostOnlyAndRedirectBackToWishlist() throws Exception {
        Product product = productRepository.findByNameContainingIgnoreCase("Everyday Runner").get(0);
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/addtowishlist")
                        .session(session)
                        .with(user("shopper"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("productId", product.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shoppingCart#wishlist"));

        mockMvc.perform(post("/clearWishList")
                        .session(session)
                        .with(user("shopper"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shoppingCart"));
    }

    @Test
    void cartPageRendersWishlistEmptyState() throws Exception {
        mockMvc.perform(get("/shoppingCart")
                        .session(new MockHttpSession())
                        .with(user("shopper")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nothing saved yet.")))
                .andExpect(content().string(containsString("Saved")));
    }

}
