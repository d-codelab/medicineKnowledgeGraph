package org.zuel.medicineknowledge.config;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.zuel.medicineknowledge.common.ErrorCode;
import org.zuel.medicineknowledge.exception.BusinessException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        System.out.println("认证失败");
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名或密码错误");
    }
}
