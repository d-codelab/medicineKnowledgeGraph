package org.zuel.medicineknowledge.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class LoginUserVO implements Serializable {

    /**
     * 用户 id
     */
    private Integer id;

    /**
     * 用户昵称
     */
    private String username;


    /**
     * 用户角色：user/admin/ban
     */
    private String role;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    private String token;

    private static final long serialVersionUID = 1L;
}