package org.zuel.medicineknowledge.common;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VerificationCodeManager {
    private final Map<String, String> verificationCodeMap = new ConcurrentHashMap<>();

    public void storeVerificationCode(String email, String code) {
        verificationCodeMap.put(email, code);
    }

    public String getVerificationCode(String email) {
        return verificationCodeMap.get(email);
    }

    public void removeVerificationCode(String email) {
        verificationCodeMap.remove(email);
    }
}
