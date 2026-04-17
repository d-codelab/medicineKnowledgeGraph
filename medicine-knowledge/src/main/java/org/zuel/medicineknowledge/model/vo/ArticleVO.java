package org.zuel.medicineknowledge.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ArticleVO {
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
     * 作者ID
     */
    private Long authorId;

    /**
     * 发布时间
     */
    private Date publishedAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 类别ID
     */
    private Integer categoryId;

    private String categoryName;


    /**
     * 封面图片链接
     */
    private String picture;
}
