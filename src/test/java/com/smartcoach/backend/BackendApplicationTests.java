package com.smartcoach.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prueba de arranque del contexto completo. No es un test unitario: valida que
 * todo el grafo de beans (JPA, Security, Spring AI, controladores) se monta.
 * Usa el perfil `test` para no requerir variables de entorno en el equipo de CI.
 */
@ActiveProfiles("test")
@SpringBootTest
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}