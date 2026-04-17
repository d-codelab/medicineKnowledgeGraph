package org.zuel.medicineknowledge.model.dto.category;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 */
@Data
@ApiModel(value = "CategoryUpdateRequest", description = "更新分类请求参数")
public class CategoryUpdateRequest implements Serializable {

    /**
     * 分类ID
     */
    @ApiModelProperty(value = "分类ID", required = true)
    private Integer id;

    /**
     * 分类名称
     */
    @ApiModelProperty(value = "分类名称",required = true)
    private String name;

    /**
     * 分类描述
     */
    @ApiModelProperty(value = "分类描述")
    private String description;

    private static final long serialVersionUID = 1L;
}