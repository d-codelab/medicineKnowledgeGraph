package org.zuel.medicineknowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.zuel.medicineknowledge.model.domain.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import org.zuel.medicineknowledge.model.dto.user.UserQueryRequest;
import org.zuel.medicineknowledge.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author mumu
* @description 针对表【users】的数据库操作Service
* @createDate 2024-04-23 21:11:06
*/
public interface UsersService extends IService<Users> {
    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<Users> getQueryWrapper(UserQueryRequest userQueryRequest);


    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, Boolean isAdmin);

    LoginUserVO getLoginUserVO(Users user);

    boolean userLogout(HttpServletRequest request);

    Users getLoginUser(HttpServletRequest request);

    long userRegister(String userAccount, String userPassword, String checkPassword, String email);
}
