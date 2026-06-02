package com.example.kursach.service;

import com.example.kursach.dto.DiscountDTO;
import com.example.kursach.repository.CategoryRepository;
import com.example.kursach.repository.DiscountRepository;
import com.example.kursach.repository.ProductRepository;
import com.example.kursach.repository.model.Discount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private static final Logger logger = LoggerFactory.getLogger(DiscountService.class);

    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public DiscountService(DiscountRepository discountRepository,
                           ProductRepository productRepository,
                           CategoryRepository categoryRepository) {
        this.discountRepository = discountRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<DiscountDTO> getAllDiscounts() {
        List<Discount> discounts = discountRepository.findAllWithDetails();
        LocalDateTime now = LocalDateTime.now();

        logger.info("Found {} discounts", discounts.size());

        return discounts.stream().map(d -> {
            // Логируем даты для отладки
            logger.debug("Discount id={}, startDate={}, endDate={}",
                    d.getId(), d.getStartDate(), d.getEndDate());

            DiscountDTO dto = new DiscountDTO();
            dto.setId(d.getId());
            dto.setProductId(d.getProductId());
            dto.setProductName(d.getProductName());
            dto.setCategoryId(d.getCategoryId());
            dto.setCategoryName(d.getCategoryName());
            dto.setPercentage(d.getPercentage());
            dto.setStartDate(d.getStartDate());
            dto.setEndDate(d.getEndDate());

            boolean active = false;
            if (d.getStartDate() != null && d.getEndDate() != null) {
                active = !now.isBefore(d.getStartDate()) && !now.isAfter(d.getEndDate());
            }
            dto.setActive(active);

            return dto;
        }).collect(Collectors.toList());
    }

    public DiscountDTO getDiscountById(Long id) {
        Discount discount = discountRepository.findByIdWithDetails(id);

        DiscountDTO dto = new DiscountDTO();
        dto.setId(discount.getId());
        dto.setProductId(discount.getProductId());
        dto.setProductName(discount.getProductName());
        dto.setCategoryId(discount.getCategoryId());
        dto.setCategoryName(discount.getCategoryName());
        dto.setPercentage(discount.getPercentage());
        dto.setStartDate(discount.getStartDate());
        dto.setEndDate(discount.getEndDate());
        dto.setActive(discount.isActive());

        return dto;
    }

    public void createDiscount(Long productId, Long categoryId, Double percentage,
                               LocalDateTime startDate, LocalDateTime endDate) {
        if (productId == null && categoryId == null) {
            throw new RuntimeException("Необходимо указать товар или категорию");
        }

        if (percentage == null || percentage < 0.1 || percentage > 99.9) {
            throw new RuntimeException("Процент скидки должен быть от 0.1 до 99.9");
        }

        if (startDate == null || endDate == null) {
            throw new RuntimeException("Необходимо указать даты начала и окончания скидки");
        }

        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Дата начала не может быть позже даты окончания");
        }

        Discount discount = new Discount();
        discount.setProductId(productId);
        discount.setCategoryId(categoryId);
        discount.setPercentage(percentage);
        discount.setStartDate(startDate);
        discount.setEndDate(endDate);
        discountRepository.save(discount);

        logger.info("Скидка создана: productId={}, categoryId={}, percentage={}, period={} - {}",
                productId, categoryId, percentage, startDate, endDate);
    }

    public void updateDiscount(Long id, Long productId, Long categoryId, Double percentage, LocalDateTime startDate, LocalDateTime endDate) {
        Discount discount = discountRepository.findById(id);

        if (percentage == null || percentage < 0.1 || percentage > 99.9) {
            throw new RuntimeException("Процент скидки должен быть от 0.1 до 99.9");
        }

        discount.setProductId(productId);
        discount.setCategoryId(categoryId);
        discount.setPercentage(percentage);
        discount.setStartDate(startDate);
        discount.setEndDate(endDate);
        discountRepository.update(discount);

        logger.info("Скидка обновлена: id={}, percentage={}", id, percentage);
    }

    public void deleteDiscount(Long id) {
        discountRepository.deleteById(id);
        logger.info("Скидка удалена: id={}", id);
    }
}