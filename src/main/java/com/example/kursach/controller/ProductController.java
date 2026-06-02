package com.example.kursach.controller;

import com.example.kursach.dto.EmployeeDTO;
import com.example.kursach.dto.ProductDTO;
import com.example.kursach.security.RequirePermission;
import com.example.kursach.service.CategoryService;
import com.example.kursach.service.ProductService;
import com.example.kursach.service.ShopService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ShopService shopService;

    public ProductController(ProductService productService, CategoryService categoryService, ShopService shopService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.shopService = shopService;
    }

    @GetMapping
    @RequirePermission(resource = "products", action = "READ")
    public String listProducts(@RequestParam(required = false) Long categoryId,
                               @RequestParam(required = false) Long shopId,
                               HttpSession session,
                               Model model) {
        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        Long targetShopId;
        if (employee.isAdmin() && shopId != null) {
            targetShopId = shopId; // Админ выбрал конкретный магазин
        } else {
            targetShopId = employee.getShopId(); // Свой магазин
        }

        List<ProductDTO> products;
        if (categoryId != null) {
            products = productService.getProductsByCategory(targetShopId, categoryId);
        } else {
            products = productService.getProductsByShop(targetShopId);
        }

        String shopAddress = shopService.getShopAddress(targetShopId);

        model.addAttribute("employee", employee);
        model.addAttribute("categories", categoryService.getAll());

        model.addAttribute("employee", employee);
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("shopAddress", shopAddress);
        model.addAttribute("selectedShopId", targetShopId);

        if (employee.isAdmin()) {
            model.addAttribute("shops", shopService.getAllShops());
            model.addAttribute("isAdmin", true);
        }

        return "products";
    }
}