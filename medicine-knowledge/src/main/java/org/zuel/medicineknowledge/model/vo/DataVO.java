package org.zuel.medicineknowledge.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class DataVO {
    private List<CategoryCount> articlesByCategory;
    private List<PageViewsData> pageViewsLast7Days;
    private List<DateCount> uvLast7Days;
    private List<DateCount> pvLast7Days;
    private List<TopArticle> top5Articles;

    // Getters and setters
}

