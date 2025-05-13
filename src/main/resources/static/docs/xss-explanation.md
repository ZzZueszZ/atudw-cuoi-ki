# Reflected XSS Vulnerability and Security

## What is Reflected XSS?

Reflected Cross-Site Scripting (XSS) is a type of security vulnerability that occurs when an application receives data in an HTTP request and includes that data in the immediate response in an unsafe way. This allows attackers to inject client-side scripts (usually JavaScript) into web pages viewed by other users.

## Vulnerable Code Example

In our demo, the vulnerable implementation directly outputs user input without proper sanitization using Thymeleaf's `th:utext` attribute:

```java
@PostMapping("/unsafe")
public String processVulnerableInput(@RequestParam String input, Model model) {
    // Unsafe: directly passing user input to the view without sanitization
    model.addAttribute("result", input);
    return "xss/result";
}
```

In the view template (result.html):
```html
<div th:utext="${result}">
    <!-- Result will be displayed here -->
</div>
```

The `th:utext` (unescaped text) attribute does not escape HTML content, allowing attackers to inject malicious scripts.

## Attack Payloads

Attackers can use various payloads to exploit Reflected XSS vulnerabilities:

1. Basic JavaScript alert:
   ```
   <script>alert('XSS')</script>
   ```

2. Image tag with error event handler:
   ```
   <img src="x" onerror="alert('XSS')">
   ```

3. HTML injection:
   ```
   <div style="background:red;font-size:24px">Hacked!</div>
   ```

## Impact of Reflected XSS

When exploited, Reflected XSS can allow attackers to:
- Steal session cookies and hijack user sessions
- Perform actions on behalf of the victim
- Capture sensitive information
- Redirect users to malicious websites
- Deface websites or manipulate their appearance

## Secure Code Example

The secure implementation uses proper output encoding with Thymeleaf's `th:text` attribute:

```java
@PostMapping("/safe")
public String processSafeInput(@RequestParam String input, Model model) {
    // Safe: Using Thymeleaf's default behavior for th:text which escapes HTML
    model.addAttribute("result", input);
    return "xss/result";
}
```

In the view template (result.html):
```html
<div th:text="${result}">
    <!-- Result will be displayed here safely -->
</div>
```

The `th:text` attribute automatically escapes HTML content, preventing script execution.

## Best Practices for XSS Prevention

1. **Input Validation**: Validate and sanitize all user input according to expected format.
2. **Output Encoding**: Always encode output data before rendering it in HTML (use `th:text` instead of `th:utext` in Thymeleaf).
3. **Content Security Policy (CSP)**: Implement strong CSP headers to restrict script execution.
4. **Use Modern Frameworks**: Modern web frameworks like Angular, React, and Vue automatically escape output to prevent XSS.
5. **X-XSS-Protection Header**: Enable browser's built-in XSS protection with `X-XSS-Protection: 1; mode=block`.
6. **HttpOnly Cookies**: Use HttpOnly flag on sensitive cookies to prevent JavaScript access.

## Additional Resources

- [OWASP XSS Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)
- [PortSwigger XSS Guide](https://portswigger.net/web-security/cross-site-scripting)
- [MDN Content Security Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP) 