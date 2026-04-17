package org.zuel.medicineknowledge.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("重新设置密码请求体")
public class PasswordUpdateRequest {

    @ApiModelProperty(value = "新密码",required = true)
    private String newPassword;
    @ApiModelProperty(value = "确认密码",required = true)
    private String checkPassword;
    @ApiModelProperty(value = "邮箱验证码",required = true)
    private String code;
    @ApiModelProperty(value = "账号",required = true)
    private String username;
    @ApiModelProperty(value = "邮箱",required = true)
    private String email;

}
