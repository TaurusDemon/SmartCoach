wpackage com.smartcoach.backend.domain.exception;

import java.util.UUID;

public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException(UUID id) {
        super("El perfil de usuario no esta creado");
    }
}
