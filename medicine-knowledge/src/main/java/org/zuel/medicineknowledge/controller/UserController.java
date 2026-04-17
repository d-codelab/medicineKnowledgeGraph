package org.zuel.medicineknowledge.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zuel.medicineknowledge.common.*;
import org.zuel.medicineknowledge.exception.BusinessException;
import org.zuel.medicineknowledge.exception.ThrowUtils;
import org.zuel.medicineknowledge.model.domain.Users;
import org.zuel.medicineknowledge.model.dto.user.PasswordUpdateRequest;
import org.zuel.medicineknowledge.model.dto.user.UserAddRequest;
import org.zuel.medicineknowledge.model.dto.user.UserLoginRequest;
import org.zuel.medicineknowledge.model.dto.user.UserQueryRequest;
import org.zuel.medicineknowledge.model.vo.LoginUserVO;
import org.zuel.medicineknowledge.service.UsersService;
import org.zuel.medicineknowledge.utils.EmailService;
import org.zuel.medicineknowledge.utils.SecurityUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
@Api(tags = "用户管理API", description = "提供用户管理相关的接口")
@Slf4j
public class UserController {
    @Resource
    UsersService usersService;
    @Resource
    EmailService emailService;

    @Autowired
    private VerificationCodeManager verificationCodeManager;

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
         if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUsername();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = usersService.userLogin(userAccount, userPassword, request,false);
        return ResultUtils.success(loginUserVO);
    }

    @PostMapping("/login/admin")
    @ApiOperation("管理员登录")
    public BaseResponse<LoginUserVO> adminLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUsername();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = usersService.userLogin(userAccount, userPassword, request,true);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserAddRequest userRegisterRequest,HttpServletRequest request) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUsername();
        String userPassword = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String email = userRegisterRequest.getEmail();
        String code1 = userRegisterRequest.getCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword,email,code1)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String verificationCode = verificationCodeManager.getVerificationCode(email);
        if (verificationCode == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"验证码未发送");
        }
        if (!verificationCode.equals(code1)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
        }
        long result = usersService.userRegister(userAccount, userPassword, checkPassword,email);
        return ResultUtils.success(result);
    }

    @GetMapping("/getCode")
    @ApiOperation("获取验证码")
    public BaseResponse<Boolean> getCode(@RequestParam String email,HttpServletRequest request){
        if (StringUtils.isBlank(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String code = emailService.email(email);
        verificationCodeManager.storeVerificationCode(email,code);
        return ResultUtils.success(true);
    }


    @PostMapping("/forgetPassword")
    @ApiOperation("重设密码")
    public BaseResponse<Boolean> forgetPassword(@RequestBody PasswordUpdateRequest passwordUpdateRequest,HttpServletRequest request){
        if (StringUtils.isAnyBlank(passwordUpdateRequest.getNewPassword(),passwordUpdateRequest.getCheckPassword(),passwordUpdateRequest.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        HttpSession session = request.getSession(false);
        String verificationCode = verificationCodeManager.getVerificationCode(passwordUpdateRequest.getEmail());
        if (verificationCode == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"验证码未发送");
        }
//        String code = (String) session.getAttribute("code");
        if (!verificationCode.equals(passwordUpdateRequest.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
        }
        Users one = usersService.getOne(new QueryWrapper<Users>().eq("username", passwordUpdateRequest.getUsername()));
        one.setPassword(SecurityUtils.encryptPassword(passwordUpdateRequest.getNewPassword()));
        return ResultUtils.success(usersService.updateById(one));
    }

    /**
     * 根据 username 获取用户邮箱（用于重设密码）
     *
     * @param username
     * @param request
     * @return
     */
    @GetMapping("/getEmail")
    @ApiOperation("获取用户邮箱")
    public BaseResponse<String> getEmail(String username, HttpServletRequest request) {
        if (StringUtils.isBlank(username)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Users user = usersService.getOne(new QueryWrapper<Users>().eq("username",username));
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user.getEmail());
    }
    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/admin/delete")
    @ApiOperation("管理员-删除用户")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = usersService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("用户登出")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = usersService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/admin/get")
    @ApiOperation("管理员-获取用户详情")
    public BaseResponse<Users> getUserById(Long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Users user = usersService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/admin/list/page")
    @ApiOperation("管理员-分野获取用户")
    public BaseResponse<Page<Users>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                    HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<Users> userPage = usersService.page(new Page<>(current, size),
                usersService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 添加用户（仅管理员）
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/admin/add")
    @ApiOperation("管理员-添加用户")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest,
                                                    HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userAddRequest.getUsername();
        String userPassword = userAddRequest.getPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || userAddRequest.getCheckPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(userAddRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        Users user = new Users();
        BeanUtils.copyProperties(userAddRequest, user);
        String encryptPassword = SecurityUtils.encryptPassword(userAddRequest.getPassword());
        user.setPassword(encryptPassword);
        boolean result = usersService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

}
