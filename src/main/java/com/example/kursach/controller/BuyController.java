package com.example.kursach.controller;

import com.example.kursach.dto.BuyDTO;
import com.example.kursach.dto.EmployeeDTO;
import com.example.kursach.repository.model.Buy;
import com.example.kursach.repository.model.BuyProduct;
import com.example.kursach.security.AuditLogger;
import com.example.kursach.security.RequirePermission;
import com.example.kursach.service.BuyService;
import com.example.kursach.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/buys")
public class BuyController {

    private final BuyService buyService;
    private final ProductService productService;
    private final AuditLogger auditLogger;

    public BuyController(BuyService buyService, ProductService productService, AuditLogger auditLogger) {
        this.buyService = buyService;
        this.productService = productService;
        this.auditLogger = auditLogger;
    }

    @GetMapping("/create")
    @RequirePermission(resource = "buy", action = "CREATE")
    public String createBuyForm(HttpSession session, Model model) {
        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        model.addAttribute("employee", employee);
        model.addAttribute("products",
                productService.getProductsByShop(employee.getShopId()));
        return "create-buy";
    }

    @PostMapping("/create")
    @ResponseBody
    @RequirePermission(resource = "buy", action = "CREATE")
    public String createBuy(@RequestBody List<BuyProduct> products,
                            HttpSession session) {
        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        try {
            Buy buy = buyService.createBuy(employee.getShopId(), products);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "CREATE", "Buy", buy.getId(),
                    "Создан чек на сумму: " + buy.getSum() + " руб., товаров: " + products.size());
            return "success";
        } catch (Exception e) {
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "CREATE_FAILED", "Buy", "Ошибка создания чека: " + e.getMessage());
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/history")
    @RequirePermission(resource = "buy_history", action = "READ")
    public String buyHistory(HttpSession session, Model model) {
        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        List<BuyDTO> buys = buyService.getBuyHistory(employee.getShopId());
        model.addAttribute("employee", employee);
        model.addAttribute("buys", buys);
        return "buy-history";
    }

    @GetMapping("/{buyId}")
    @RequirePermission(resource = "buy_history", action = "READ")
    public String buyDetails(@PathVariable Long buyId,
                             HttpSession session,
                             Model model) {
        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        BuyDTO buy = buyService.getBuyDetails(buyId);
        model.addAttribute("employee", employee);
        model.addAttribute("buy", buy);
        return "buy-details";
    }
}