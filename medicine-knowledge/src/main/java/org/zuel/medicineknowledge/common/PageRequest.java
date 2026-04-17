package org.zuel.medicineknowledge.common;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.zuel.medicineknowledge.constant.CommonConstant;

/**
 * 分页请求
 *

 */
/**
 * 分页请求
 */
@Data
@ApiModel(description = "分页请求参数")
public class PageRequest {

    @ApiModelProperty(value = "当前页号", example = "1")
    private Integer current = 1;

    @ApiModelProperty(value = "页面大小", example = "10")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "排序字段",example = "created_at")
    private String sortField;

    @ApiModelProperty(value = "排序顺序（默认升序）", allowableValues = "asc, desc", example = "asc")
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;
}
