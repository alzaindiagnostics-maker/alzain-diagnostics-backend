package com.alzain.repository;

import com.alzain.entity.TestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRepository extends JpaRepository<TestItem, Long> {
    Optional<TestItem> findByName(String name);
    List<TestItem> findByActiveTrue();
    List<TestItem> findByCategory(String category);

    @Query("SELECT DISTINCT TRIM(t.category) FROM TestItem t WHERE t.category IS NOT NULL AND TRIM(t.category) <> '' ORDER BY TRIM(t.category) ASC")
    List<String> findDistinctCategories();
}
