package com.example.kursach.controller;

import com.example.kursach.dto.EmployeeDTO;
import com.example.kursach.dto.ProductDTO;
import com.example.kursach.repository.model.Category;
import com.example.kursach.repository.model.Product;
import com.example.kursach.security.AuditLogger;
import com.example.kursach.service.CategoryService;
import com.example.kursach.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class ProductsManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ProductsManagementController.class);
    private final ProductService productService;
    private final CategoryService categoryService;
    private final AuditLogger auditLogger;

    public ProductsManagementController(ProductService productService, CategoryService categoryService, AuditLogger auditLogger) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.auditLogger = auditLogger;
    }

    @GetMapping
    public String listProducts(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        // Показываем все товары без привязки к магазину
        List<ProductDTO> products = productService.getAllProducts();

        model.addAttribute("employee", employee);
        model.addAttribute("products", products);

        return "admin-products";
    }

    @GetMapping("/create")
    public String createProductForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        List<Category> categories = categoryService.getAll();

        model.addAttribute("employee", employee);
        model.addAttribute("categories", categories);

        return "create-product";
    }

    @PostMapping("/create")
    public String createProduct(@RequestParam String name,
                                @RequestParam Long categoryId,
                                @RequestParam String unit,
                                @RequestParam Double price,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        try {
            productService.createProduct(name, categoryId, unit, price);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "CREATE", "Product", "Создан товар: " + name);
            redirectAttributes.addFlashAttribute("success", "Товар успешно добавлен");
        } catch (Exception e) {
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "CREATE_FAILED", "Product", "Ошибка создания товара: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id,
                                  HttpServletRequest request,
                                  Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        ProductDTO product = productService.getProductDTOById(id);
        List<Category> categories = categoryService.getAll();

        model.addAttribute("employee", employee);
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);

        return "edit-product";
    }

    @PostMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam Long categoryId,
                              @RequestParam String unit,
                              @RequestParam Double price,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        try {
            productService.updateProduct(id, name, categoryId, unit, price);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "UPDATE", "Product", "Изменен товар: " + id);
            redirectAttributes.addFlashAttribute("success", "Товар обновлен");
        } catch (Exception e) {
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "UPDATE_FAILED", "Product", "Ошибка изменения товара: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        try {
            Product product = productService.getProductById(id);
            productService.deleteProduct(id);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "DELETE", "Product", "Удален товар: " + product.getName());
            redirectAttributes.addFlashAttribute("success", "Товар удален");
        } catch (Exception e) {
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "DELETE_FAILED", "Product", "Ошибка удаления товара: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/products";
    }

    // Управление остатками товаров в магазинах
    @GetMapping("/stock/{productId}")
    public String manageStock(@PathVariable Long productId,
                              HttpServletRequest request,
                              Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        ProductDTO product = productService.getProductDTOById(productId);

        model.addAttribute("employee", employee);
        model.addAttribute("product", product);

        return "manage-stock";
    }

    @PostMapping("/stock/{productId}")
    public String updateStock(@PathVariable Long productId,
                              @RequestParam Long shopId,
                              @RequestParam Integer count,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        try {
            Product product = productService.getProductById(productId);
            productService.updateStock(productId, shopId, count);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "UPDATE", "Product", "Изменено количество товара: " + product.getName());
            redirectAttributes.addFlashAttribute("success", "Остаток обновлен");
        } catch (Exception e) {
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "UPDATE_FAILED", "Product", "Ошибка изменения количества товара: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/products";
    }
}