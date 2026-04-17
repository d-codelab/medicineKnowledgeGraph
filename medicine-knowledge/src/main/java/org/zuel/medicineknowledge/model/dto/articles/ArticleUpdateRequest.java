package org.zuel.medicineknowledge.model.dto.articles;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 */
@Data
public class ArticleUpdateRequest implements Serializable {

    /**
     * id
     */
    private Integer id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 类别id
     */
    private Integer categoryId;

    private String picture;

    private static final long serialVersionUID = 1L;
}