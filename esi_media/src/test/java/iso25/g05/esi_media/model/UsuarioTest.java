package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    @Test
    void testUsuarioGettersAndSetters() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Test User");
        usuario.setApellidos("Test Apellidos");
        usuario.setEmail("test@example.com");
        usuario.setBloqueado(false);
        Date now = new Date();
        usuario.setFechaRegistro(now);
        usuario.setTwoFactorAutenticationEnabled(true);
        usuario.setThreeFactorAutenticationEnabled(false);

        assertEquals("Test User", usuario.getNombre());
        assertEquals("Test Apellidos", usuario.getApellidos());
        assertEquals("test@example.com", usuario.getEmail());
        assertFalse(usuario.isBloqueado());
        assertEquals(now, usuario.getFechaRegistro());
        assertTrue(usuario.isTwoFactorAutenticationEnabled());
        assertFalse(usuario.isThreeFactorAutenticationEnabled());
    }

    @Test
    void testUsuarioConstructor() {
        List<String> contraseniasUsadas = new ArrayList<>();
        contraseniasUsadas.add("oldPassword");
        Contrasenia contrasenia = new Contrasenia("1", new Date(), "newPassword", contraseniasUsadas);
        Usuario usuario = new Usuario("Apellidos", false, contrasenia, "constructor@example.com", null, "Constructor User");

        assertEquals("Constructor User", usuario.getNombre());
        assertEquals("Apellidos", usuario.getApellidos());
        assertEquals("constructor@example.com", usuario.getEmail());
        assertFalse(usuario.isBloqueado());
        assertEquals(contrasenia, usuario.getContrasenia());
        assertNotNull(usuario.getFechaRegistro());
    }
}
