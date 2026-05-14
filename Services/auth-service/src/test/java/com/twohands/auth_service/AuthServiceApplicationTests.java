package com.twohands.auth_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

class AuthServiceApplicationTests {

	@Test
	void applicationEntryPointShouldBeInvokable() {
		SpringApplication application = new SpringApplication(AuthServiceApplication.class);
		application.setLogStartupInfo(false);
	}

}
