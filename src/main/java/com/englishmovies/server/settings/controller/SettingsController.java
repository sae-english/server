package com.englishmovies.server.settings.controller;

import com.englishmovies.server.settings.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/telegram-sending")
    public ResponseEntity<Map<String, Boolean>> getTelegramSendingEnabled() {
        boolean enabled = settingsService.isTelegramSendingEnabled();
        return ResponseEntity.ok(Map.of("enabled", enabled));
    }

    @PutMapping("/telegram-sending")
    public ResponseEntity<Map<String, Boolean>> setTelegramSendingEnabled(@RequestBody Map<String, Boolean> body) {
        Boolean enabled = body != null ? body.get("enabled") : null;
        if (enabled == null) {
            return ResponseEntity.badRequest().build();
        }
        settingsService.setTelegramSendingEnabled(enabled);
        return ResponseEntity.ok(Map.of("enabled", settingsService.isTelegramSendingEnabled()));
    }
}
