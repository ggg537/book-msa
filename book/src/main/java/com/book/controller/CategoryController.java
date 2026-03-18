package com.book.controller;

import com.book.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    // 전체 카테고리 목록
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getAllCategories() {
        List<Map<String, String>> categories = Arrays.stream(Category.values())
                .map(c -> Map.of(
                        "code", c.getCode(),
                        "name", c.getName(),
                        "fullName", c.getFullName()
                ))
                .toList();
        return ResponseEntity.ok(categories);
    }

    // 1차 분류 목록만
    @GetMapping("/parents")
    public ResponseEntity<List<Map<String, String>>> getParentCategories() {
        List<Map<String, String>> categories = Arrays.stream(Category.values())
                .filter(Category::isParent)
                .map(c -> Map.of(
                        "code", c.getCode(),
                        "name", c.getName()
                ))
                .toList();
        return ResponseEntity.ok(categories);
    }

    // 특정 1차 분류의 2차 목록
    @GetMapping("/{parentCode}/children")
    public ResponseEntity<List<Map<String, String>>> getChildCategories(
            @PathVariable String parentCode) {
        List<Map<String, String>> categories = Category.getChildren(parentCode)
                .stream()
                .map(c -> Map.of(
                        "code", c.getCode(),
                        "name", c.getName(),
                        "fullName", c.getFullName()
                ))
                .toList();
        return ResponseEntity.ok(categories);
    }
}
