package com.alzain.service;

import com.alzain.dto.TestDTO;
import com.alzain.entity.TestItem;
import com.alzain.exception.ResourceNotFoundException;
import com.alzain.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminTestService {

    @Autowired
    private TestRepository testRepository;

    public List<TestItem> getAllTests() {
        return testRepository.findAll();
    }

    public TestItem createTest(TestDTO dto) {
        TestItem test = TestItem.builder()
                .name(dto.getName().trim())
                .category(dto.getCategory().trim())
                .shortDescription(dto.getShortDescription())
                .detailedDescription(dto.getDetailedDescription())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return testRepository.save(test);
    }

    public TestItem updateTest(Long id, TestDTO dto) {
        TestItem test = testRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));

        test.setName(dto.getName().trim());
        test.setCategory(dto.getCategory().trim());
        test.setShortDescription(dto.getShortDescription());
        test.setDetailedDescription(dto.getDetailedDescription());
        if (dto.getActive() != null) test.setActive(dto.getActive());
        test.setUpdatedAt(LocalDateTime.now());

        return testRepository.save(test);
    }

    public void deleteTest(Long id) {
        if (!testRepository.existsById(id)) {
            throw new ResourceNotFoundException("Test not found with id: " + id);
        }
        testRepository.deleteById(id);
    }
}
