package com.smartcoach.backend.infrastructure.persistence;

import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.Email;
import com.smartcoach.backend.domain.vo.PasswordHash;
import com.smartcoach.backend.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ============================================================================
 * DOCUMENTACIÓN DEL TEST DE INTEGRACIÓN — JpaUserRepositoryAdapter (adaptador de persistencia)
 * ============================================================================
 *
 * QUÉ SE PRUEBA
 * -------------
 * La integración REAL entre tres piezas:
 *   1. El adaptador de salida {@link UserRepository} (implementado por
 *      {@code JpaUserRepositoryAdapter}).
 *   2. Los mappers de ida y vuelta ({@code UserPersistenceMapper}):
 *      dominio {@code User} <-> entidad JPA {@code UserEntity}.
 *   3. PostgreSQL real, levivantado en un contenedor Docker por Testcontainers
 *      (heredado de {@link AbstractIntegrationTest}).
 *
 * A diferencia de un test unitario (donde el repositorio se mockea), aquí NO se
 * sustituye nada: se ejecuta SQL de verdad contra un motor PostgreSQL real. Así
 * se detectan fallos que un mock jamás mostraría: mapeos de columnas, constraints,
 * tipos de dato, claves/índices, etc.
 *
 * CÓMO FUNCIONA EL ARRANQUE
 * -------------------------
 * - {@link AbstractIntegrationTest} arranca un contenedor PostgreSQL una sola vez
 *   (bloque {@code static}) y publica su URL/usuario/contraseña como propiedades
 *   dinámicas de Spring ({@code @DynamicPropertySource}).
 * - {@code @SpringBootTest} levanta el contexto completo de la aplicación
 *   (beans, JPA, EntityManagerFactory) apuntando a ese contenedor.
 * - {@code @ActiveProfiles("test")} activa {@code application-test.yml}, que evita
 *   requerir variables de entorno como GEMINI_API_KEY o JWT_SECRET en local/CI.
 * - {@code @Autowired UserRepository} inyecta el adaptador real, no un mock.
 *
 * NOTA DE AISLAMIENTO
 * -------------------
 * Cada test genera IDs y emails aleatorios, por lo que no dependen del estado
 * previo de la base de datos ni de otros tests.
 */
@ActiveProfiles("test")
@SpringBootTest
public class UserRepositoryAdapterITest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("IT-01: Debe guardar un usuario y recuperarlo por ID desde PostgreSQL")
    void shouldSaveAndFindUserByIdInRealPostgres() {
        // ------------------------------------------------------------------
        // GIVEN (preparación): construimos un usuario de DOMINIO válido.
        // Ojo: Email y PasswordHash son Value Objects que validan en su
        // constructor, así que aquí usamos datos correctos.
        // ------------------------------------------------------------------
        UUID id = UUID.randomUUID();
        Email email = new Email("it.user." + id + "@smartcoach.com");
        User user = new User(id, "Usuario IT", email, new PasswordHash("hash-de-prueba"));

        // ------------------------------------------------------------------
        // WHEN (acción): persistimos a través del puerto de dominio.
        // Dentro del adaptador se hace el mapeo User -> UserEntity y el save.
        // ------------------------------------------------------------------
        userRepository.save(user);

        // Recuperamos pidiendo explícitamente por el mismo ID.
        Optional<User> found = userRepository.findById(id);

        // ------------------------------------------------------------------
        // THEN (verificación): el usuario debe existir y conservar sus datos.
        // Se comparan CAMPO A CAMPO (no equals()): User es una clase de dominio
        // sin equals(), y el objeto recuperado es una instancia distinta que la
        // guardada. Comparar campos hace la aserción robusta y expresiva.
        // ------------------------------------------------------------------
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(id);
        assertThat(found.get().getName()).isEqualTo("Usuario IT");
        assertThat(found.get().getEmail().value()).isEqualTo(email.value());
        assertThat(found.get().getPassword().value()).isEqualTo("hash-de-prueba");
    }

    @Test
    @DisplayName("IT-02: Debe encontrar un usuario por email y devolver vacío si no existe")
    void shouldFindUserByEmailInRealPostgres() {
        // ------------------------------------------------------------------
        // GIVEN: dos usuarios distintos para comprobar que el filtro por email
        // realmente discrimina filas y no devuelve cualquiera.
        // ------------------------------------------------------------------
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Email email1 = new Email("find.me." + id1 + "@smartcoach.com");
        Email email2 = new Email("find.me." + id2 + "@smartcoach.com");

        userRepository.save(new User(id1, "Buscado", email1, new PasswordHash("hash-1")));
        userRepository.save(new User(id2, "Otro", email2, new PasswordHash("hash-2")));

        // ------------------------------------------------------------------
        // WHEN: buscamos por el email del primero y por uno inexistente.
        // ------------------------------------------------------------------
        Optional<User> encontrado = userRepository.findByEmail(email1);
        Optional<User> noExiste = userRepository.findByEmail(new Email("nadie+" + UUID.randomUUID() + "@smartcoach.com"));

        // ------------------------------------------------------------------
        // THEN: el encontrado debe ser exactamente el usuario 1; el inexistente
        // debe ser un Optional vacío (el puerto devuelve Optional, regla del
        // proyecto: nunca null).
        // ------------------------------------------------------------------
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getId()).isEqualTo(id1);
        assertThat(encontrado.get().getName()).isEqualTo("Buscado");

        assertThat(noExiste).isEmpty();
    }

    @Test
    @DisplayName("IT-03: Debe actualizar (upsert) un usuario existente sin duplicar filas")
    void shouldUpdateExistingUserInRealPostgres() {
        // ------------------------------------------------------------------
        // GIVEN: un usuario ya persistido. Como el ID es la clave primaria,
        // volver a llamar a save() con el mismo ID debe hacer UPDATE, no INSERT.
        // ------------------------------------------------------------------
        UUID id = UUID.randomUUID();
        Email email = new Email("update.me." + id + "@smartcoach.com");
        userRepository.save(new User(id, "Nombre Original", email, new PasswordHash("hash-original")));

        // ------------------------------------------------------------------
        // WHEN: guardamos un nuevo objeto de dominio con el MISMO id pero otros
        // datos (por ejemplo, un cambio de contraseña / nombre).
        // ------------------------------------------------------------------
        userRepository.save(new User(id, "Nombre Actualizado", email, new PasswordHash("hash-nuevo")));

        // ------------------------------------------------------------------
        // THEN: debe recuperarse una única fila con los datos nuevos.
        // ------------------------------------------------------------------
        Optional<User> actualizado = userRepository.findById(id);
        assertThat(actualizado).isPresent();
        assertThat(actualizado.get().getName()).isEqualTo("Nombre Actualizado");
        assertThat(actualizado.get().getPassword().value()).isEqualTo("hash-nuevo");
    }

    @Test
    @DisplayName("IT-04: Debe eliminar un usuario por ID y dejarlo de encontrar")
    void shouldDeleteUserByIdInRealPostgres() {
        // ------------------------------------------------------------------
        // GIVEN: un usuario persistido para poder borrarlo después.
        // ------------------------------------------------------------------
        UUID id = UUID.randomUUID();
        Email email = new Email("delete.me." + id + "@smartcoach.com");
        userRepository.save(new User(id, "Para Borrar", email, new PasswordHash("hash-borrar")));

        // ------------------------------------------------------------------
        // WHEN: borramos por ID a través del puerto.
        // ------------------------------------------------------------------
        userRepository.deleteById(id);

        // ------------------------------------------------------------------
        // THEN: ya no debe existir ni por ID ni por email.
        // ------------------------------------------------------------------
        assertThat(userRepository.findById(id)).isEmpty();
        assertThat(userRepository.findByEmail(email)).isEmpty();
    }
}
