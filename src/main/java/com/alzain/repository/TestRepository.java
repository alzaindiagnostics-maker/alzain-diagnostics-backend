package com.alzain.repository;

import com.alzain.entity.TestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRepository extends JpaRepository<TestItem, Long> {
    Optional<TestItem> findByName(String name);
    List<TestItem> findByActiveTrue();
    List<TestItem> findByCategory(String category);
}
