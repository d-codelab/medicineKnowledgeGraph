package org.zuel.medicineknowledge.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageViewsData {
    private String page;
    private List<String> days;
    private List<Integer> views;

    // Getters and setters
}
