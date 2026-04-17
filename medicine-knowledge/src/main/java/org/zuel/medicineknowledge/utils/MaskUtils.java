package org.zuel.medicineknowledge.utils;

import org.apache.commons.lang3.StringUtils;
import org.zuel.medicineknowledge.common.ErrorCode;
import org.zuel.medicineknowledge.exception.BusinessException;

public class MaskUtils {

    /**
     * 对邮箱地址进行脱敏处理
     *
     * @param email 原始邮箱地址
     * @return 脱敏后的邮箱地址
     */
    public static String maskEmail(String email) {
        if (StringUtils.isNotBlank(email) && email.contains("@")) {
            int atIndex = email.indexOf("@");
            return email.substring(0, atIndex - 3) + "***" + email.substring(atIndex);
        }else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }
}
