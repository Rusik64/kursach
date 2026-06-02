package com.example.kursach.service;

import com.example.kursach.repository.ShopRepository;
import com.example.kursach.repository.model.Shop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShopService {

    private static final Logger logger = LoggerFactory.getLogger(ShopService.class);
    private final ShopRepository shopRepository;

    public ShopService(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public List<Map<String, Object>> getAllShopsWithStats() {
        List<Shop> shops = shopRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Shop shop : shops) {
            Map<String, Object> shopData = new HashMap<>();
            shopData.put("id", shop.getId());
            shopData.put("address", shop.getAddress());
            shopData.put("employeeCount", shopRepository.getEmployeeCount(shop.getId()));
            shopData.put("productCount", shopRepository.getProductCount(shop.getId()));
            result.add(shopData);
        }

        return result;
    }

    public List<Shop> getAllShops() {
        return shopRepository.findAll();
    }

    public Shop getShopById(Long id) {
        return shopRepository.findById(id);
    }

    public void createShop(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new RuntimeException("Адрес магазина не может быть пустым");
        }

        Shop shop = new Shop();
        shop.setAddress(address.trim());
        shopRepository.save(shop);
        logger.info("Магазин создан: {}", address);
    }

    public void updateShop(Long id, String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new RuntimeException("Адрес магазина не может быть пустым");
        }

        Shop shop = shopRepository.findById(id);
        shop.setAddress(address.trim());
        shopRepository.update(shop);
        logger.info("Магазин обновлен: id={}, address={}", id, address);
    }

    public void deleteShop(Long id) {
        int employeeCount = shopRepository.getEmployeeCount(id);
        if (employeeCount > 0) {
            throw new RuntimeException(
                    String.format("Нельзя удалить магазин, в котором есть сотрудники (%d чел.)", employeeCount)
            );
        }

        shopRepository.delete(id);
        logger.info("Магазин удален: id={}", id);
    }

    public String getShopAddress(Long shopId) {
        return shopRepository.getAddressById(shopId);
    }
}
