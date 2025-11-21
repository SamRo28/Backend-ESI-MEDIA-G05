package iso25.g05.esi_media.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitarios para la clase ContraseniaComun.
 * Verifica la correcta gestión de contraseñas comunes que deben ser bloqueadas.
 */
@DisplayName("Tests para ContraseniaComun")
class ContrasenaComunTest {

    // ==================== TESTS DE CONSTRUCTOR ====================

    @Test
    @DisplayName("Constructor sin parámetros debe crear instancia vacía")
    void testConstructorSinParametros() {
        ContraseniaComun contraseniaComun = new ContraseniaComun();
        
        assertNotNull(contraseniaComun);
        assertNull(contraseniaComun.getUsadas());
    }

    @Test
    @DisplayName("Constructor con parámetro debe inicializar correctamente")
    void testConstructorConParametro() {
        String hashContrasenia = "5f4dcc3b5aa765d61d8327deb882cf99"; // MD5 de "password"
        
        ContraseniaComun contraseniaComun = new ContraseniaComun(hashContrasenia);
        
        assertNotNull(contraseniaComun);
        assertEquals(hashContrasenia, contraseniaComun.getUsadas());
    }

    // ==================== TESTS DE GETTERS Y SETTERS ====================

    @Test
    @DisplayName("Setter y getter de usadas debe funcionar correctamente")
    void testSetterGetterUsadas() {
        ContraseniaComun contraseniaComun = new ContraseniaComun();
        String hashContrasenia = "098f6bcd4621d373cade4e832627b4f6"; // MD5 de "test"
        
        contraseniaComun.setUsadas(hashContrasenia);
        
        assertEquals(hashContrasenia, contraseniaComun.getUsadas());
    }

    @Test
    @DisplayName("Debe manejar correctamente contraseñas comunes típicas")
    void testContraseniasComunesTipicas() {
        // Ejemplos de hashes MD5 de contraseñas comunes
        String[] contrasenasComunes = {
            "5f4dcc3b5aa765d61d8327deb882cf99", // "password"
            "e10adc3949ba59abbe56e057f20f883e", // "123456"
            "25d55ad283aa400af464c76d713c07ad", // "12345678"
            "202cb962ac59075b964b07152d234b70", // "123"
            "d8578edf8458ce06fbc5bb76a58c5ca4"  // "qwerty"
        };
        
        for (String hash : contrasenasComunes) {
            ContraseniaComun contraseniaComun = new ContraseniaComun(hash);
            assertEquals(hash, contraseniaComun.getUsadas());
        }
    }

    @Test
    @DisplayName("Debe poder actualizar una contraseña común existente")
    void testActualizarContraseniaComun() {
        String hashInicial = "5f4dcc3b5aa765d61d8327deb882cf99"; // "password"
        String hashNuevo = "e10adc3949ba59abbe56e057f20f883e";   // "123456"
        
        ContraseniaComun contraseniaComun = new ContraseniaComun(hashInicial);
        assertEquals(hashInicial, contraseniaComun.getUsadas());
        
        contraseniaComun.setUsadas(hashNuevo);
        assertEquals(hashNuevo, contraseniaComun.getUsadas());
    }

    @Test
    @DisplayName("Debe aceptar cadenas vacías")
    void testCadenaVacia() {
        ContraseniaComun contraseniaComun = new ContraseniaComun("");
        
        assertNotNull(contraseniaComun);
        assertEquals("", contraseniaComun.getUsadas());
    }

    @Test
    @DisplayName("Debe aceptar valores null")
    void testValorNull() {
        ContraseniaComun contraseniaComun = new ContraseniaComun(null);
        
        assertNotNull(contraseniaComun);
        assertNull(contraseniaComun.getUsadas());
    }

    @Test
    @DisplayName("Debe manejar hashes largos correctamente")
    void testHashLargo() {
        // Simular un hash SHA-256 (más largo que MD5)
        String hashSha256 = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8";
        
        ContraseniaComun contraseniaComun = new ContraseniaComun(hashSha256);
        
        assertEquals(hashSha256, contraseniaComun.getUsadas());
        assertEquals(64, contraseniaComun.getUsadas().length(), "SHA-256 debe tener 64 caracteres");
    }

    @Test
    @DisplayName("Debe preservar el formato del hash")
    void testPreservarFormatoHash() {
        String hashMinusculas = "5f4dcc3b5aa765d61d8327deb882cf99";
        String hashMayusculas = "5F4DCC3B5AA765D61D8327DEB882CF99";
        
        ContraseniaComun contraseniaMin = new ContraseniaComun(hashMinusculas);
        ContraseniaComun contraseniaMay = new ContraseniaComun(hashMayusculas);
        
        assertEquals(hashMinusculas, contraseniaMin.getUsadas());
        assertEquals(hashMayusculas, contraseniaMay.getUsadas());
    }

    // ==================== TESTS DE CASOS ESPECIALES ====================

    @Test
    @DisplayName("Dos instancias con el mismo hash deben ser independientes")
    void testInstanciasIndependientes() {
        String hash = "5f4dcc3b5aa765d61d8327deb882cf99";
        
        ContraseniaComun contrasenia1 = new ContraseniaComun(hash);
        ContraseniaComun contrasenia2 = new ContraseniaComun(hash);
        
        assertEquals(contrasenia1.getUsadas(), contrasenia2.getUsadas());
        
        // Modificar una no debe afectar la otra
        contrasenia1.setUsadas("nuevo_hash");
        
        assertEquals("nuevo_hash", contrasenia1.getUsadas());
        assertEquals(hash, contrasenia2.getUsadas());
    }

    @Test
    @DisplayName("Debe manejar caracteres especiales en el hash")
    void testCaracteresEspeciales() {
        // Aunque no es común, debe poder manejar cualquier string
        String hashEspecial = "abc-123_xyz!@#";
        
        ContraseniaComun contraseniaComun = new ContraseniaComun(hashEspecial);
        
        assertEquals(hashEspecial, contraseniaComun.getUsadas());
    }

    @Test
    @DisplayName("Debe manejar espacios en blanco")
    void testEspaciosEnBlanco() {
        String hashConEspacios = "5f4dcc3b 5aa765d6 1d8327de b882cf99";
        
        ContraseniaComun contraseniaComun = new ContraseniaComun(hashConEspacios);
        
        assertEquals(hashConEspacios, contraseniaComun.getUsadas());
    }

    @Test
    @DisplayName("Debe poder resetear a null")
    void testResetearANull() {
        String hash = "5f4dcc3b5aa765d61d8327deb882cf99";
        ContraseniaComun contraseniaComun = new ContraseniaComun(hash);
        
        assertEquals(hash, contraseniaComun.getUsadas());
        
        contraseniaComun.setUsadas(null);
        assertNull(contraseniaComun.getUsadas());
    }
}
