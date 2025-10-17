package iso25.g05.esi_media.model;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;



class TokenTest {

    @Test
    void testTokenGettersAndSetters() {
        Token token = new Token();
        token.setToken("test_token");
        Date expected = new Date(2025 - 1900, 9, 15); // October 15, 2025
        token.setFechaExpiracion(expected);

        assertEquals("test_token", token.getToken());
        assertEquals(expected, token.getFechaExpiracion());
    }

    @Test
    void testTokenConstructor() {
        Token token = new Token();

        assertNotNull(token.getToken(), "El token generado no debe ser null");
        assertFalse(token.getToken().isEmpty(), "El token generado no debe estar vacío");
        assertNotNull(token.getFechaExpiracion(), "La fecha de expiración no debe ser null");
        assertFalse(token.isExpirado(), "Por defecto el token no debe estar expirado");
        // La fecha de expiración debe estar en el futuro (o al menos >= ahora)
        assertTrue(token.getFechaExpiracion().after(new Date(System.currentTimeMillis() - 2000)));
    }
}
