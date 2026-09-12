package com.example.lumeloginbackend.repository;

import com.example.lumeloginbackend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByUserEmail(String userEmail);
    Optional<CartItem> findByUserEmailAndProductId(String userEmail, Integer productId);
}