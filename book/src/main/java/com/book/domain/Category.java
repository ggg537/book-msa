package com.book.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {

    // 1차 분류
    DOMESTIC("01", "국내도서", null),
    FOREIGN("02", "서양도서", null),

    // 국내도서 2차 분류
    DOMESTIC_NOVEL("01-01", "소설", "01"),
    DOMESTIC_POEM("01-02", "시/에세이", "01"),
    DOMESTIC_HUMANITIES("01-03", "인문", "01"),
    DOMESTIC_IT("01-04", "IT/컴퓨터", "01"),
    DOMESTIC_SCIENCE("01-05", "과학", "01"),
    DOMESTIC_HISTORY("01-06", "역사", "01"),
    DOMESTIC_ECONOMY("01-07", "경제/경영", "01"),
    DOMESTIC_CHILDREN("01-08", "어린이", "01"),
    DOMESTIC_ETC("01-09", "기타", "01"),

    // 서양도서 2차 분류
    FOREIGN_NOVEL("02-01", "소설", "02"),
    FOREIGN_POEM("02-02", "시/에세이", "02");

    private final String code;      // DB/ES 저장값
    private final String name;      // 화면 표시명
    private final String parentCode; // 1차 분류 코드 (1차면 null)

    // 코드로 enum 찾기
    public static Category fromCode(String code) {
        for (Category category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 카테고리 코드: " + code);
    }

    // 전체 경로명 반환 (1차 > 2차)
    public String getFullName() {
        if (parentCode == null) {
            return name;  // 국내도서
        }
        Category parent = fromCode(parentCode);
        return parent.getName() + " > " + name;  // 국내도서 > IT/컴퓨터
    }

    // 1차 분류 여부
    public boolean isParent() {
        return parentCode == null;
    }

    // 특정 1차 분류의 2차 목록 조회
    public static java.util.List<Category> getChildren(String parentCode) {
        return java.util.Arrays.stream(values())
                .filter(c -> parentCode.equals(c.parentCode))
                .toList();
    }
}