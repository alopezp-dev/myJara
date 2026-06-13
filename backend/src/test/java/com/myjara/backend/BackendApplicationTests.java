package com.myjara.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:postgresql://localhost:5433/myjara_test",
		"spring.datasource.username=myjara",
		"spring.datasource.password=myjara",
		"spring.flyway.repair-on-migrate=true",
		"spring.flyway.out-of-order=true",
		"myjara.jwt.secret=clave-secreta-para-tests-minimo-256-bits-xxxxxxxxxxx",
		"logging.level.root=WARN"
})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}
}