package org.zuel.medicineknowledge.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName articles
 */
@TableName(value ="articles")
@Data
public class Articles implements Serializable {
    /**
     * 文章ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer countv;

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


    /**
     * 封面图片链接
     */
    private String picture;

    /**
     * 是否删除，0为未删除，1为已删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}