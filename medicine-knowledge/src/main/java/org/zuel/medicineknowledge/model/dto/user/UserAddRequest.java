package org.zuel.medicineknowledge.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "用户添加请求")
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户名", example = "john_doe", required = true)
    private String username;

    @ApiModelProperty(value = "邮箱地址", example = "john@example.com", required = true)

    private String email;

    @ApiModelProperty(value = "邮箱验证码", example = "103784", required = true)
    private String code;

    @ApiModelProperty(value = "密码", example = "Pa$$w0rd", required = true)

    private String password;

    @ApiModelProperty(value = "确认密码", example = "Pa$$w0rd", required = true)
    private String checkPassword;

    @ApiModelProperty(value = "用户角色", allowableValues = "user, admin,ban", example = "user", required = true)
    private String role;
}