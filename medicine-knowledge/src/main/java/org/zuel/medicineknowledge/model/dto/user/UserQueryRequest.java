package org.zuel.medicineknowledge.model.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.zuel.medicineknowledge.common.PageRequest;

import java.io.Serializable;

/**
 * 用户查询请求
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "用户查询请求参数")
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID")
    private Long id;

    @ApiModelProperty(value = "搜索内容")
    private String searchText;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户账号")
    private String username;

    /**
     * 用户角色：user/admin/ban
     */
    @ApiModelProperty(value = "用户角色", allowableValues = "user, admin, ban")
    private String role;

    private static final long serialVersionUID = 1L;
}