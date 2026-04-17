package org.zuel.medicinegateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class rateConfig {

    @Bean
    public RedisScript<Boolean> loadRedisScript(){
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        //lua脚本路径
        redisScript.setLocation(new ClassPathResource("limit.lua"));
        //lua脚本返回值
        redisScript.setResultType(java.lang.Boolean.class);
        return redisScript;
    }

}