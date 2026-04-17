package org.zuel.medicineknowledge.model.dto.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.zuel.medicineknowledge.common.PageRequest;

import java.io.Serializable;


/**
 * 查询请求
 *

 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value = "CategoryQueryRequest", description = "分类查询请求参数")
public class CategoryQueryRequest extends PageRequest implements Serializable {

    /**
     * 分类ID
     */
    @ApiModelProperty(value = "分类ID")
    private Long id;

    /**
     * 搜索词
     */
    @ApiModelProperty(value = "搜索关键词，描述、标题都可")
    private String searchText;

    /**
     * 分类描述
     */
    @ApiModelProperty(value = "分类描述")
    private String description;

    /**
     * 分类名称
     */
    @ApiModelProperty(value = "分类名称")
    private String name;



    private static final long serialVersionUID = 1L;
}