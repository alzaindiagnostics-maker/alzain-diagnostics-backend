package com.alzain.repository;

import com.alzain.entity.BusinessSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessSettingRepository extends JpaRepository<BusinessSetting, Long> {
    Optional<BusinessSetting> findBySettingKey(String settingKey);
}
