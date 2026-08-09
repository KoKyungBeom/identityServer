package io.github.kyungbeom.identity_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan   // @ConfigurationProperties 클래스(JwtProperties, OAuth2Properties)를 모아서 등록
public class IdentityServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentityServerApplication.class, args);
	}

}
