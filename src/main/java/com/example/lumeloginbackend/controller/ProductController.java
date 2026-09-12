package com.example.lumeloginbackend.controller;

import com.example.lumeloginbackend.entity.Product;
import com.example.lumeloginbackend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody Product product) {
        Product saved = productRepository.save(product);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Product not found");
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Integer id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            return ResponseEntity.status(404).body("Product not found");
        }
        return ResponseEntity.ok(product.get());
    }
    @PostMapping("/convert-images-to-bytes")
    public ResponseEntity<?> convertImagesToBytes() {
        List<Product> allProducts = productRepository.findAll();
        int converted = 0;
        int skipped = 0;
        int failed = 0;

        for (Product product : allProducts) {
            if (product.getImageUrl() != null && product.getImageData() == null) {
                try {
                    // imageUrl looks like "/images/category1.PNG"
                    // Actual file lives in the Vue project's public folder
                    String filePath = "C:/Users/USER/WebstormProjects/vue1-project/public" + product.getImageUrl();
                    byte[] bytes = Files.readAllBytes(Paths.get(filePath));
                    product.setImageData(bytes);
                    productRepository.save(product);
                    converted++;
                } catch (IOException e) {
                    failed++;
                }
            } else {
                skipped++;
            }
        }

        return ResponseEntity.ok("Converted: " + converted + ", Skipped: " + skipped + ", Failed: " + failed);
    }
    @PostMapping("/add-from-url")
    public ResponseEntity<?> addProductFromUrl(@RequestBody Map<String, Object> payload) {
        try {
            String name = (String) payload.get("name");
            String description = (String) payload.get("description");
            Double price = Double.valueOf(payload.get("price").toString());
            String category = (String) payload.get("category");
            Integer stockQuantity = Integer.valueOf(payload.get("stockQuantity").toString());
            String imageLink = (String) payload.get("imageLink");

            URL url = new URL(imageLink);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            connection.setInstanceFollowRedirects(true);

            byte[] imageBytes;
            try (java.io.InputStream in = connection.getInputStream()) {
                imageBytes = in.readAllBytes();
            }

            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setCategory(category);
            product.setStockQuantity(stockQuantity);
            product.setImageData(imageBytes);

            Product saved = productRepository.save(product);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to download image: " + e.getMessage());
        }
    }
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productRepository.findByCategory(category));
    }

    // ===== NEW: Upload product with actual image bytes =====
    @PostMapping("/upload")
    public ResponseEntity<?> addProductWithImage(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("category") String category,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("image") MultipartFile imageFile) {

        try {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setCategory(category);
            product.setStockQuantity(stockQuantity);
            product.setImageData(imageFile.getBytes());

            Product saved = productRepository.save(product);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to save image");
        }
    }

    // ===== NEW: Retrieve image bytes as an actual image =====
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Integer id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty() || productOpt.get().getImageData() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageData = productOpt.get().getImageData();
        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(imageData);
    }
}

