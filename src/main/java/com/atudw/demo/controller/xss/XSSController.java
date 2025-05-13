package com.atudw.demo.controller.xss;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/xss")
public class XSSController {

    // In-memory storage for Stored XSS demo
    private static final List<String> storedMessagesVulnerable = new ArrayList<>();
    private static final List<String> storedMessagesSafe = new ArrayList<>();

    /**
     * Shows the form for XSS demonstration
     */
    @GetMapping("/unsafe")
    public String showVulnerableForm(HttpServletRequest request, Model model) {
        model.addAttribute("title", "Vulnerable Reflected XSS");
        model.addAttribute("request", request);
        return "xss/form";
    }

    /**
     * Shows the form with proper XSS prevention
     */
    @GetMapping("/safe")
    public String showSafeForm(HttpServletRequest request, Model model) {
        model.addAttribute("title", "Safe XSS Prevention");
        model.addAttribute("request", request);
        return "xss/form";
    }

    /**
     * Vulnerable endpoint - directly outputs user input without sanitization
     * Vulnerable to XSS attacks with payloads like:
     * <script>alert('XSS')</script>
     */
    @PostMapping("/unsafe")
    public String processVulnerableInput(@RequestParam String input, HttpServletRequest request,
            HttpServletResponse response, Model model) {
        // Unsafe: directly passing user input to the view without sanitization
        model.addAttribute("result", input);
        model.addAttribute("method", "Unsafe Method: Directly outputs user input without sanitization");
        model.addAttribute("request", request);

        // Set a cookie with HttpOnly=false for demonstration
        Cookie xssCookie = new Cookie("xss_demo_cookie_unsafe", "unsafe_value_" + System.currentTimeMillis());
        xssCookie.setHttpOnly(false); // Disable HttpOnly
        xssCookie.setPath("/");
        response.addCookie(xssCookie);

        return "xss/result";
    }

    /**
     * Safe endpoint - properly sanitizes user input
     * Same input will be escaped/sanitized to prevent XSS attacks
     */
    @PostMapping("/safe")
    public String processSafeInput(@RequestParam String input, HttpServletRequest request, HttpServletResponse response,
            Model model) {
        // Safe: Using Thymeleaf's default behavior for th:text which escapes HTML
        // The view will use th:text instead of th:utext for safe rendering
        model.addAttribute("result", input);
        model.addAttribute("method", "Safe Method: Input is properly sanitized");
        model.addAttribute("request", request);

        // Set a cookie with HttpOnly=false for demonstration (can still be set, but
        // ideally secure apps use HttpOnly for sensitive cookies)
        Cookie xssCookie = new Cookie("xss_demo_cookie_safe", "safe_value_" + System.currentTimeMillis());
        xssCookie.setHttpOnly(false); // Disable HttpOnly for this demo cookie
        xssCookie.setPath("/");
        response.addCookie(xssCookie);

        return "xss/result";
    }

    // --- Stored XSS ---

    @GetMapping("/stored/form")
    public String showStoredXSSForm(Model model, HttpServletRequest request) {
        model.addAttribute("title", "Stored XSS - Submit Message");
        model.addAttribute("actionUrl", "/xss/stored/submit");
        model.addAttribute("viewUrl", "/xss/stored/view");
        model.addAttribute("isVulnerable", true);
        model.addAttribute("request", request);
        return "xss/stored-form";
    }

    @PostMapping("/stored/submit")
    public String submitStoredXSSVulnerable(@RequestParam String message, Model model, HttpServletRequest request) {
        // UNSAFE: Storing the message as is
        storedMessagesVulnerable.add(message);
        model.addAttribute("message", "Message stored (vulnerable)!");
        model.addAttribute("request", request);
        // Redirect to form or view page, for simplicity, let's show a success message
        // on the form page itself.
        model.addAttribute("title", "Stored XSS - Submit Message");
        model.addAttribute("actionUrl", "/xss/stored/submit");
        model.addAttribute("viewUrl", "/xss/stored/view");
        model.addAttribute("isVulnerable", true);
        return "xss/stored-form";
    }

    @GetMapping("/stored/view")
    public String viewStoredXSSVulnerable(Model model, HttpServletRequest request) {
        model.addAttribute("title", "Stored XSS - View Messages (Vulnerable)");
        model.addAttribute("messages", Collections.unmodifiableList(new ArrayList<>(storedMessagesVulnerable)));
        model.addAttribute("isVulnerable", true);
        model.addAttribute("request", request);
        return "xss/stored-view";
    }

    // --- Safe Stored XSS ---

    @GetMapping("/stored-safe/form")
    public String showStoredXSSSafeForm(Model model, HttpServletRequest request) {
        model.addAttribute("title", "Stored XSS (Safe) - Submit Message");
        model.addAttribute("actionUrl", "/xss/stored-safe/submit");
        model.addAttribute("viewUrl", "/xss/stored-safe/view");
        model.addAttribute("isVulnerable", false);
        model.addAttribute("request", request);
        return "xss/stored-form"; // Reusing the same form template
    }

    @PostMapping("/stored-safe/submit")
    public String submitStoredXSSSafe(@RequestParam String message, Model model, HttpServletRequest request) {
        // SAFE: Storing the message after escaping HTML entities
        String sanitizedMessage = HtmlUtils.htmlEscape(message);
        storedMessagesSafe.add(sanitizedMessage);
        model.addAttribute("message", "Message stored (safe and sanitized)!");
        model.addAttribute("request", request);
        model.addAttribute("title", "Stored XSS (Safe) - Submit Message");
        model.addAttribute("actionUrl", "/xss/stored-safe/submit");
        model.addAttribute("viewUrl", "/xss/stored-safe/view");
        model.addAttribute("isVulnerable", false);
        return "xss/stored-form";
    }

    @GetMapping("/stored-safe/view")
    public String viewStoredXSSSafe(Model model, HttpServletRequest request) {
        model.addAttribute("title", "Stored XSS (Safe) - View Messages");
        model.addAttribute("messages", Collections.unmodifiableList(new ArrayList<>(storedMessagesSafe)));
        model.addAttribute("isVulnerable", false);
        model.addAttribute("request", request);
        return "xss/stored-view"; // Reusing the same view template, safety handled by th:text
    }
}