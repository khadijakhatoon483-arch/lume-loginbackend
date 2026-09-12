package com.example.lumeloginbackend.controller;

import com.example.lumeloginbackend.entity.CartItem;
import com.example.lumeloginbackend.entity.Product;
import com.example.lumeloginbackend.repository.CartItemRepository;
import com.example.lumeloginbackend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    // Add item to cart (or increase quantity if already there)
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> payload) {
        String userEmail = (String) payload.get("userEmail");
        Integer productId = (Integer) payload.get("productId");

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Product not found");
        }

        Optional<CartItem> existing = cartItemRepository.findByUserEmailAndProductId(userEmail, productId);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + 1);
            cartItemRepository.save(item);
            return ResponseEntity.ok(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserEmail(userEmail);
            newItem.setProduct(productOpt.get());
            newItem.setQuantity(1);
            cartItemRepository.save(newItem);
            return ResponseEntity.ok(newItem);
        }
    }

    // Get all cart items for a user
    @GetMapping("/{userEmail}")
    public ResponseEntity<List<CartItem>> getCart(@PathVariable String userEmail) {
        return ResponseEntity.ok(cartItemRepository.findByUserEmail(userEmail));
    }

    // Remove item from cart
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable Integer id) {
        cartItemRepository.deleteById(id);
        return ResponseEntity.ok("Removed from cart");
    }
}