package org.zuel.medicineknowledge.model.vo;

import lombok.Data;

@Data
public class CategoryCount {
    private String category;
    private Long count;

    public CategoryCount(String category, Long count) {
        this.category = category;
        this.count = count;
    }
// Getters and setters
}
