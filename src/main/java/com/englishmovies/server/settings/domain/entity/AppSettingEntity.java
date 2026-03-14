package com.englishmovies.server.settings.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Настройка приложения (key-value). Например: telegram_sending_enabled = true/false.
 */
@Entity
@Table(name = "app_setting", schema = "englishmovies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppSettingEntity {

    @Id
    @Column(name = "key", nullable = false, length = 100)
    private String key;

    @Column(name = "value", nullable = false, length = 500)
    private String value;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
