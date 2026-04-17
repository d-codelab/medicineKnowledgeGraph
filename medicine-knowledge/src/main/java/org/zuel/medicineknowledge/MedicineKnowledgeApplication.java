package org.zuel.medicineknowledge;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;

@SpringBootApplication()
@MapperScan("org.zuel.medicineknowledge.mapper")
@EnableKnife4j
public class MedicineKnowledgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicineKnowledgeApplication.class, args);
    }

}
