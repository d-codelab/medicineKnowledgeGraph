package org.zuel.medicineknowledge.model.dto.category;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 */
@Data
@ApiModel(value = "CategoryAddRequest", description = "创建分类请求参数")
public class CategoryAddRequest implements Serializable {

    /**
     * 分类名称
     */
    @ApiModelProperty(value = "分类名称", required = true)
    private String name;

    /**
     * 分类描述
     */
    @ApiModelProperty(value = "分类描述")
    private String description;

    private static final long serialVersionUID = 1L;
}