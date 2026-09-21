package com.backend.ecommerce_api;

import com.backend.ecommerce_api.entity.Product;
import com.backend.ecommerce_api.repository.OrderRepository;
import com.backend.ecommerce_api.repository.ProductRepository;
import com.backend.ecommerce_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private Long productId;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        productId = productRepository.save(Product.builder()
                .name("Keyboard")
                .price(new BigDecimal("25.00"))
                .stockQuantity(10)
                .build()).getId();

        String registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"buyer@example.com\",\"password\":\"password123\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Matcher tokenMatcher = Pattern.compile("\\\"accessToken\\\":\\\"([^\\\"]+)").matcher(registration);
        accessToken = tokenMatcher.find() ? tokenMatcher.group(1) : "";
    }

    @Test
    void createOrderReturnsCreatedAndDeductsStock() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(50.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createOrderRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":2}"))
                .andExpect(status().isUnauthorized());
    }
}
