package org.zuel.medicineknowledge.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新个人信息请求
 *

 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * 用户账号
     */
    private String username;


    private static final long serialVersionUID = 1L;
}