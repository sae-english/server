package com.englishmovies.server.settings.repository;

import com.englishmovies.server.settings.domain.entity.AppSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppSettingRepository extends JpaRepository<AppSettingEntity, String> {

    Optional<AppSettingEntity> findByKey(String key);
}
