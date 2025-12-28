package com.valumetric.repository;

import com.valumetric.document.SystemConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigRepository extends MongoRepository<SystemConfig, String> {

    Optional<SystemConfig> findByConfigType(String configType);

    default SystemConfig getDefaultConfig() {
        return findByConfigType("DEFAULT")
                .orElseGet(() -> {
                    SystemConfig defaultConfig = SystemConfig.createDefault();
                    return save(defaultConfig);
                });
    }
}
