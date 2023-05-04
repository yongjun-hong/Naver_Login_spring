package Naver_Api_Test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Appconfig {
    @Bean
    public NaverLoginBO naverLoginBO() {
        return new NaverLoginBO();
    }
}
