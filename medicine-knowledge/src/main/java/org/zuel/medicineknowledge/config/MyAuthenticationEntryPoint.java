package org.zuel.medicineknowledge.config;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.zuel.medicineknowledge.common.ErrorCode;
import org.zuel.medicineknowledge.exception.BusinessException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        System.out.println("需要登录");
        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }
}
