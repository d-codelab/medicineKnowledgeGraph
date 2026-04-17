package org.zuel.medicineknowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.zuel.medicineknowledge.common.ErrorCode;
import org.zuel.medicineknowledge.constant.CommonConstant;
import org.zuel.medicineknowledge.constant.UserConstant;
import org.zuel.medicineknowledge.exception.BusinessException;
import org.zuel.medicineknowledge.mapper.UsersMapper;
import org.zuel.medicineknowledge.model.domain.Users;
import org.zuel.medicineknowledge.model.dto.user.UserQueryRequest;
import org.zuel.medicineknowledge.model.vo.LoginUserVO;
import org.zuel.medicineknowledge.service.UsersService;
import org.zuel.medicineknowledge.utils.JwtUtils;
import org.zuel.medicineknowledge.utils.SqlUtils;

import javax.servlet.http.HttpServletRequest;

/**
* @author mumu
* @description 针对表【users】的数据库操作Service实现
* @createDate 2024-04-23 21:11:06
*/
@Service
public  class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService {
    @Override
    public QueryWrapper<Users> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUsername();
        String userRole = userQueryRequest.getRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        String searchText = userQueryRequest.getSearchText();
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("role", searchText).or().like("username", searchText).or().like("role", searchText));
        }
        queryWrapper.eq(id != null&&id!=0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(userRole), "role", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userName), "username", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, Boolean isAdmin) {
        // 1. 校验
        if (org.apache.commons.lang3.StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 查询用户是否存在
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userAccount);
        Users user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        //校验密码
        if (!isMatchEncryptPassword(userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码错误");
        }
        if (isAdmin&&!user.getRole().equals(UserConstant.ADMIN_ROLE)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"非管理员账户");
        }
        // 生成 token
        String token = JwtUtils.generateToken(userAccount, user.getRole(), user.getId(),true);

        // 认证成功后，设置认证信息到 Spring Security 上下文中
        Authentication authentication = JwtUtils.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        loginUserVO.setToken(token);
        return loginUserVO ;
    }
    /**
     * 是否匹配生成的BCryptPasswordEncoder密码
     *
     * @param password 密码
     * @return 加密字符串
     */
    private static boolean isMatchEncryptPassword(String password) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(password);
        return passwordEncoder.matches(password,encode);
    }

    @Override
    public LoginUserVO getLoginUserVO(Users user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 移除登录态

        return true;
    }
    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public Users getLoginUser(HttpServletRequest request) {
//        String requestHeader = request.getHeader(SecurityConstants.TOKEN_HEADER);
//        Claims tokenBody = JwtUtils.getTokenBody(requestHeader);
//        tokenBody.get("username")

        // 从数据库查询（追求性能的话可以注释，直接走缓存）
//        long userId = currentUser.getId();
//        currentUser = this.getById(userId);
//        if (currentUser == null) {
//            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
//        }
//        return currentUser;
        return null;
    }

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String email) {
        // 1. 校验
        if (org.apache.commons.lang3.StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encryptPassword = passwordEncoder.encode(userPassword);
            // 3. 插入数据
            Users user = new Users();
            user.setId(null);
            user.setUsername(userAccount);
            user.setPassword(encryptPassword);
            user.setEmail(email);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }
}




