package com.example.kursach.service;

import com.example.kursach.dto.ProductDTO;
import com.example.kursach.repository.CategoryRepository;
import com.example.kursach.repository.DiscountRepository;
import com.example.kursach.repository.ProductRepository;
import com.example.kursach.repository.model.Discount;
import com.example.kursach.repository.model.Product;
import com.example.kursach.repository.model.Category;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final DiscountRepository discountRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, DiscountRepository discountRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.discountRepository = discountRepository;
    }

    public List<ProductDTO> getProductsByShop(Long shopId) {
        List<Product> products = productRepository.findAllByShopId(shopId);
        Map<Long, String> categoryMap = getAllCategoriesMap();
        LocalDateTime now = LocalDateTime.now();

        return products.stream()
                .map(product -> {
                    ProductDTO dto = new ProductDTO();
                    dto.setId(product.getId());
                    dto.setName(product.getName());
                    dto.setCategoryName(categoryMap.getOrDefault(product.getCategoryId(), "Unknown"));
                    dto.setPrice(product.getPrice());
                    dto.setUnit(product.getUnit());
                    dto.setAvailableCount(productRepository.getProductCountInShop(shopId, product.getId()));

                    // Проверяем активные скидки
                    Discount discount = discountRepository.findByProductAndDate(product.getId(), now);
                    if (discount == null) {
                        discount = discountRepository.findByCategoryAndDate(product.getCategoryId(), now);
                    }
                    if (discount != null) {
                        dto.setDiscountPercent(discount.getPercentage());
                        dto.setDiscountId(discount.getId());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByCategory(Long shopId, Long categoryId) {
        List<Product> products = productRepository.findAllByShopId(shopId);
        Map<Long, String> categoryMap = getAllCategoriesMap();

        return products.stream()
                .filter(product -> product.getCategoryId().equals(categoryId))
                .map(product -> {
                    ProductDTO dto = new ProductDTO();
                    dto.setId(product.getId());
                    dto.setName(product.getName());
                    dto.setCategoryName(categoryMap.getOrDefault(product.getCategoryId(), "Unknown"));
                    dto.setPrice(product.getPrice());
                    dto.setUnit(product.getUnit());
                    dto.setAvailableCount(productRepository.getProductCountInShop(shopId, product.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Product getProductById(Long productId) {
        return productRepository.findById(productId);
    }

    private Map<Long, String> getAllCategoriesMap() {
        return categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        Map<Long, String> categoryMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        return products.stream()
                .map(product -> {
                    ProductDTO dto = new ProductDTO();
                    dto.setId(product.getId());
                    dto.setName(product.getName());
                    dto.setCategoryName(categoryMap.getOrDefault(product.getCategoryId(), "Неизвестно"));
                    dto.setPrice(product.getPrice());
                    dto.setUnit(product.getUnit());
                    dto.setCategoryId(product.getCategoryId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public ProductDTO getProductDTOById(Long productId) {
        Product product = productRepository.findById(productId);
        String categoryName = categoryRepository.findById(product.getCategoryId()).getName();

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCategoryId(product.getCategoryId());
        dto.setCategoryName(categoryName);
        dto.setPrice(product.getPrice());
        dto.setUnit(product.getUnit());

        return dto;
    }

    public void createProduct(String name, Long categoryId, String unit, Double price) {
        Product product = new Product();
        product.setName(name);
        product.setCategoryId(categoryId);
        product.setUnit(unit);
        product.setPrice(price);
        productRepository.save(product);
    }

    public void updateProduct(Long id, String name, Long categoryId, String unit, Double price) {
        Product product = productRepository.findById(id);
        product.setName(name);
        product.setCategoryId(categoryId);
        product.setUnit(unit);
        product.setPrice(price);
        productRepository.update(product);
    }

    public void deleteProduct(Long id) {
        productRepository.delete(id);
    }

    public void updateStock(Long productId, Long shopId, Integer count) {
        productRepository.updateOrInsertStock(productId, shopId, count);
    }
}
