package org.zuel.medicineknowledge.model.vo;

import lombok.Data;

@Data
public class TopArticle {
    private String id;
    private String name;
    private int views;

    public TopArticle(String id, String name, int views) {
        this.id = id;
        this.name = name;
        this.views = views;
    }
// Getters and setters
}
