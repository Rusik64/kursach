package com.example.kursach.service;

import com.example.kursach.dto.BuyDTO;
import com.example.kursach.dto.BuyProductDTO;
import com.example.kursach.repository.*;
import com.example.kursach.repository.model.Buy;
import com.example.kursach.repository.model.BuyProduct;
import com.example.kursach.repository.model.Discount;
import com.example.kursach.repository.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BuyService {

    private final BuyRepository buyRepository;
    private final BuyProductRepository buyProductRepository;
    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;

    public BuyService(BuyRepository buyRepository,
                      BuyProductRepository buyProductRepository,
                      DiscountRepository discountRepository,
                      ProductRepository productRepository) {
        this.buyRepository = buyRepository;
        this.buyProductRepository = buyProductRepository;
        this.discountRepository = discountRepository;
        this.productRepository = productRepository;
    }

    public BuyDTO getBuyDetails(Long buyId) {
        Buy buy = buyRepository.findById(buyId);
        List<BuyProduct> buyProducts = buyProductRepository.findByBuyId(buyId);
        LocalDateTime buyDate = buy.getCreatedAt(); // Дата создания чека

        List<BuyProductDTO> productDTOs = buyProducts.stream()
                .map(bp -> {
                    Product product = productRepository.findById(bp.getProductId());

                    BuyProductDTO dto = new BuyProductDTO();
                    dto.setProductId(bp.getProductId());
                    dto.setProductName(product.getName());
                    dto.setPrice(product.getPrice());
                    dto.setCount(bp.getCount());

                    double originalTotal = product.getPrice() * bp.getCount();
                    dto.setTotalPrice(Math.round(originalTotal * 100.0) / 100.0);

                    // Проверяем скидки, активные на дату создания чека
                    Discount discount = discountRepository.findByProductAndDate(bp.getProductId(), buyDate);
                    if (discount == null && product.getCategoryId() != null) {
                        discount = discountRepository.findByCategoryAndDate(product.getCategoryId(), buyDate);
                    }

                    if (discount != null) {
                        dto.setDiscountPercent(discount.getPercentage());
                        double discountAmount = originalTotal * (discount.getPercentage() / 100.0);
                        dto.setDiscountAmount(Math.round(discountAmount * 100.0) / 100.0);
                        dto.setFinalPrice(Math.round((originalTotal - discountAmount) * 100.0) / 100.0);
                    } else {
                        dto.setDiscountPercent(0.0);
                        dto.setDiscountAmount(0.0);
                        dto.setFinalPrice(Math.round(originalTotal * 100.0) / 100.0);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        BuyDTO buyDTO = new BuyDTO();
        buyDTO.setId(buy.getId());
        buyDTO.setShopId(buy.getShopId());
        buyDTO.setSum(buy.getSum());
        buyDTO.setCreatedAt(buy.getCreatedAt());
        buyDTO.setProducts(productDTOs);

        return buyDTO;
    }

    public List<BuyDTO> getBuyHistory(Long shopId) {
        List<Buy> buys = buyRepository.findAllByShopId(shopId);

        // Собираем все ID продуктов
        Set<Long> allProductIds = new HashSet<>();
        List<BuyProduct> allBuyProducts = new ArrayList<>();
        Map<Long, LocalDateTime> buyDates = new HashMap<>();

        for (Buy buy : buys) {
            List<BuyProduct> products = buyProductRepository.findByBuyId(buy.getId());
            allBuyProducts.addAll(products);
            products.forEach(p -> allProductIds.add(p.getProductId()));
            buyDates.put(buy.getId(), buy.getCreatedAt());
        }

        // Получаем все продукты одним запросом
        Map<Long, Product> productMap = productRepository.findByIds(new ArrayList<>(allProductIds));

        // Группируем товары по чекам
        Map<Long, List<BuyProduct>> productsByBuyId = allBuyProducts.stream()
                .collect(Collectors.groupingBy(BuyProduct::getBuyId));

        return buys.stream()
                .map(buy -> {
                    List<BuyProduct> buyProducts = productsByBuyId.getOrDefault(buy.getId(), List.of());
                    LocalDateTime buyDate = buyDates.get(buy.getId());

                    List<BuyProductDTO> productDTOs = buyProducts.stream()
                            .map(bp -> {
                                Product product = productMap.get(bp.getProductId());

                                BuyProductDTO dto = new BuyProductDTO();
                                dto.setProductId(bp.getProductId());
                                dto.setProductName(product != null ? product.getName() : "Неизвестный товар");
                                dto.setPrice(product != null ? product.getPrice() : 0.0);
                                dto.setCount(bp.getCount());

                                double originalTotal = (product != null ? product.getPrice() : 0.0) * bp.getCount();
                                dto.setTotalPrice(Math.round(originalTotal * 100.0) / 100.0);

                                if (product != null) {
                                    // Проверяем скидки на дату чека
                                    Discount discount = discountRepository.findByProductAndDate(bp.getProductId(), buyDate);
                                    if (discount == null && product.getCategoryId() != null) {
                                        discount = discountRepository.findByCategoryAndDate(product.getCategoryId(), buyDate);
                                    }

                                    if (discount != null) {
                                        dto.setDiscountPercent(discount.getPercentage());
                                        double discountAmount = originalTotal * (discount.getPercentage() / 100.0);
                                        dto.setDiscountAmount(Math.round(discountAmount * 100.0) / 100.0);
                                        dto.setFinalPrice(Math.round((originalTotal - discountAmount) * 100.0) / 100.0);
                                    } else {
                                        dto.setDiscountPercent(0.0);
                                        dto.setDiscountAmount(0.0);
                                        dto.setFinalPrice(Math.round(originalTotal * 100.0) / 100.0);
                                    }
                                }

                                return dto;
                            })
                            .collect(Collectors.toList());

                    BuyDTO buyDTO = new BuyDTO();
                    buyDTO.setId(buy.getId());
                    buyDTO.setShopId(buy.getShopId());
                    buyDTO.setSum(buy.getSum());
                    buyDTO.setCreatedAt(buy.getCreatedAt());
                    buyDTO.setProducts(productDTOs);

                    return buyDTO;
                })
                .collect(Collectors.toList());
    }

    public Buy getBuyById(Long buyId) {
        return buyRepository.findById(buyId);
    }

    public List<BuyProduct> getBuyProducts(Long buyId) {
        return buyProductRepository.findByBuyId(buyId);
    }

    @Transactional
    public Buy createBuy(Long shopId, List<BuyProduct> buyProducts) {
        for (BuyProduct bp : buyProducts) {
            Integer stock = buyRepository.getProductStock(shopId, bp.getProductId());
            if (stock == null || stock < bp.getCount()) {
                Product product = productRepository.findById(bp.getProductId());
                throw new RuntimeException(
                        String.format("Недостаточно товара '%s' на складе. Доступно: %d, запрошено: %d",
                                product.getName(), stock != null ? stock : 0, bp.getCount())
                );
            }
        }

        double totalSum = calculateTotalSum(buyProducts);
        totalSum = Math.round(totalSum * 100.0) / 100.0;

        Buy buy = new Buy();
        buy.setShopId(shopId);
        buy.setSum(totalSum);
        buy = buyRepository.createBuy(buy);

        for (BuyProduct bp : buyProducts) {
            bp.setBuyId(buy.getId());
        }
        buyProductRepository.saveAll(buyProducts);

        for (BuyProduct bp : buyProducts) {
            buyRepository.updateProductStock(shopId, bp.getProductId(), -bp.getCount());
        }

        return buy;
    }

    private double calculateTotalSum(List<BuyProduct> buyProducts) {
        double totalSum = 0.0;
        LocalDateTime now = LocalDateTime.now();

        for (BuyProduct bp : buyProducts) {
            Product product = productRepository.findById(bp.getProductId());
            double price = product.getPrice();
            int count = bp.getCount();
            double itemSum = price * count;

            // Проверяем активные скидки на текущий момент
            Discount discount = discountRepository.findByProductAndDate(bp.getProductId(), now);
            if (discount == null && product.getCategoryId() != null) {
                discount = discountRepository.findByCategoryAndDate(product.getCategoryId(), now);
            }

            if (discount != null) {
                double discountMultiplier = 1.0 - (discount.getPercentage() / 100.0);
                itemSum = itemSum * discountMultiplier;
            }

            totalSum += itemSum;
        }

        return totalSum;
    }

    public double calculateBuySum(List<BuyProduct> buyProducts) {
        return Math.round(calculateTotalSum(buyProducts) * 100.0) / 100.0;
    }

    public double applyDiscount(double price, double discountPercentage) {
        double discountMultiplier = 1.0 - (discountPercentage / 100.0);
        double discountedPrice = price * discountMultiplier;
        return Math.round(discountedPrice * 100.0) / 100.0;
    }

    public double calculateItemSum(double price, int count, double discountPercentage) {
        double itemSum = price * count;
        if (discountPercentage > 0) {
            double discountMultiplier = 1.0 - (discountPercentage / 100.0);
            itemSum = itemSum * discountMultiplier;
        }
        return Math.round(itemSum * 100.0) / 100.0;
    }

    public double calculateTotalRevenue(Long shopId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        List<Buy> buys = buyRepository.findByShopIdAndDateRange(shopId, startDate, endDate);
        double totalRevenue = 0.0;

        for (Buy buy : buys) {
            totalRevenue += buy.getSum();
        }

        return Math.round(totalRevenue * 100.0) / 100.0;
    }
}