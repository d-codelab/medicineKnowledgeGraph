package org.zuel.medicineknowledge.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求
 *

 */
@Data
@ApiModel(description = "用户登录请求")
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    @ApiModelProperty(value = "用户名", example = "123456", required = true)
    private String username;

    @ApiModelProperty(value = "用户密码", example = "12345678", required = true)
    private String userPassword;
}
