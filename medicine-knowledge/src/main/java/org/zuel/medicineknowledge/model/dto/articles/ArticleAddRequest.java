package org.zuel.medicineknowledge.model.dto.articles;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *
 */
@Data
@ApiModel(value = "ArticleAddRequest", description = "文章创建请求参数")
public class ArticleAddRequest implements Serializable {

    /**
     * 文章标题
     */
    @ApiModelProperty(value = "文章标题", required = true)
    private String title;

    /**
     * 文章内容
     */
    @ApiModelProperty(value = "文章内容", required = true)
    private String content;

    /**
     * 封面图片URL
     */
    @ApiModelProperty(value = "封面图片URL")
    private String picture;

    /**
     * 文章所属分类ID
     */
    @ApiModelProperty(value = "文章所属分类ID", required = true)
    private Integer categoryId;

    private static final long serialVersionUID = 1L;
}