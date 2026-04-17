package org.zuel.medicineknowledge.config;

import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.zuel.medicineknowledge.common.ErrorCode;
import org.zuel.medicineknowledge.exception.BusinessException;

import javax.servlet.ServletException;
import java.io.IOException;

public class MySessionExpiredHandler implements SessionInformationExpiredStrategy {
    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
        System.out.println("登录过期");
        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"登录过期");
    }
}
