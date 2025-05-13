package com.atudw.demo.controller.commandinjection;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/commandinjection")
public class CommandInjectionController {

    @GetMapping
    public String showCommandInjectionPage(Model model, HttpServletRequest request) {
        model.addAttribute("request", request);
        return "commandinjection/index";
    }

    /**
     * VULNERABLE implementation that directly uses user input in a command
     * This is intentionally vulnerable to command injection attacks
     */
    @PostMapping("/unsafe")
    public String executeUnsafeCommand(@RequestParam String command, Model model, HttpServletRequest request) {
        List<String> output = new ArrayList<>();
        String error = null;
        String fullCommand = "ping " + command;

        try {
            // VULNERABLE: Directly using user input in command execution
            // Example attacks: 127.0.0.1 & dir, 127.0.0.1 && ipconfig
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", fullCommand);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.add(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0 && output.isEmpty()) {
                error = "Command failed with exit code: " + exitCode;
            }
        } catch (IOException | InterruptedException e) {
            error = "Error executing command: " + e.getMessage();
        }

        model.addAttribute("command", fullCommand);
        model.addAttribute("output", output);
        model.addAttribute("error", error);
        model.addAttribute("isVulnerable", true);
        model.addAttribute("request", request);

        return "commandinjection/result";
    }

    /**
     * SAFE implementation that validates user input before execution
     * Only allows executing the ping command with valid IP addresses or hostnames
     */
    @PostMapping("/safe")
    public String executeSafeCommand(@RequestParam String host, Model model, HttpServletRequest request) {
        List<String> output = new ArrayList<>();
        String error = null;
        String command = null;

        try {
            // SAFE: Validate input and only allow specific commands with validated
            // parameters
            if (!isValidHostname(host)) {
                error = "Invalid hostname or IP address";
            } else {
                // Build a safe command with validated input
                command = "ping " + host;
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.add(line);
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode != 0 && output.isEmpty()) {
                    error = "Command failed with exit code: " + exitCode;
                }
            }
        } catch (IOException | InterruptedException e) {
            error = "Error executing command: " + e.getMessage();
        }

        model.addAttribute("command", command);
        model.addAttribute("host", host);
        model.addAttribute("output", output);
        model.addAttribute("error", error);
        model.addAttribute("isVulnerable", false);
        model.addAttribute("request", request);

        return "commandinjection/result";
    }

    /**
     * Validates if the input is a valid hostname or IP address
     * Only allows alphanumeric characters, dots, and hyphens
     */
    private boolean isValidHostname(String host) {
        // Simple validation - allows alphanumeric, dots, and hyphens
        // More comprehensive validation would be appropriate for production
        if (host == null || host.trim().isEmpty()) {
            return false;
        }

        // Check for command injection characters
        if (host.contains("&") || host.contains("|") || host.contains(";") ||
                host.contains("`") || host.contains("$") || host.contains("(") ||
                host.contains(")") || host.contains("\\") || host.contains("\"") ||
                host.contains("'") || host.contains(">") || host.contains("<")) {
            return false;
        }

        // Allow only valid hostname characters
        return Pattern.matches("^[a-zA-Z0-9.\\-]+$", host);
    }
}