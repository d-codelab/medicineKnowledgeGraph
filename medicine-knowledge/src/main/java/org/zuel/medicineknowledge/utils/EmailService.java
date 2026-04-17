package org.zuel.medicineknowledge.utils;

import cn.hutool.core.util.RandomUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class EmailService {

    @Value("${spring.mail.username}")
    private String from="";
    @Resource
    private JavaMailSender javaMailSender;

    public String email(String to) {
        Integer code = RandomUtil.randomInt(100000, 999999);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(from);
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject("智医图说注册验证码");
        simpleMailMessage.setText("Code:" + code.toString());
        javaMailSender.send(simpleMailMessage);
        return code.toString();
    }
}