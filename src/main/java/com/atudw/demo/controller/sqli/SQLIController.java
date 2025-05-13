package com.atudw.demo.controller.sqli;

import com.atudw.demo.entity.User;
import com.atudw.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/sqli")
public class SQLIController {

    @Autowired
    private UserService userService;

    /**
     * Shows the vulnerable form for SQL injection demonstration
     * This endpoint is intentionally vulnerable to SQL injection
     */
    @GetMapping("/vulnerable")
    public String showVulnerableForm(HttpServletRequest request, Model model) {
        model.addAttribute("title", "Vulnerable SQL Query");
        model.addAttribute("description",
                "This form uses direct string concatenation in SQL queries, making it vulnerable to SQL injection attacks.");
        model.addAttribute("request", request);
        return "sqli/form";
    }

    /**
     * Shows the safe form with proper SQL injection prevention
     * This endpoint uses parameterized queries to prevent SQL injection
     */
    @GetMapping("/safe")
    public String showSafeForm(HttpServletRequest request, Model model) {
        model.addAttribute("title", "Safe SQL Query");
        model.addAttribute("description", "This form uses parameterized queries to prevent SQL injection attacks.");
        model.addAttribute("request", request);
        return "sqli/form";
    }

    /**
     * Vulnerable endpoint - uses direct string concatenation in SQL query
     * Example of SQL injection vulnerability:
     * Input: ' OR '1'='1
     * Result: Returns all users in the database
     */
    @PostMapping("/vulnerable/search")
    public String searchVulnerable(@RequestParam String username, Model model) {
        List<User> users = userService.findUsersVulnerable(username);
        model.addAttribute("users", users);
        model.addAttribute("isVulnerable", true);
        model.addAttribute("query", "SELECT * FROM users WHERE username = '" + username + "'");
        return "sqli/result";
    }

    /**
     * Safe endpoint - uses parameterized queries
     * Same input will be treated as literal string, preventing SQL injection
     */
    @PostMapping("/safe/search")
    public String searchSafe(@RequestParam String username, Model model) {
        List<User> users = userService.findUsersSafe(username);
        model.addAttribute("users", users);
        model.addAttribute("isVulnerable", false);
        model.addAttribute("query", "SELECT u FROM User u WHERE u.username = :username");
        return "sqli/result";
    }
}