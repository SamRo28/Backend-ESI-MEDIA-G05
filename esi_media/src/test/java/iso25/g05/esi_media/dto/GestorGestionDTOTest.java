package iso25.g05.esi_media.dto;

import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class GestorGestionDTOTest {

    @Test
    void testGettersAndSetters() {
        GestorGestionDTO dto = new GestorGestionDTO();
        dto.setId("g1");
        dto.setNombre("Pepe");
        dto.setApellidos("Lopez");
        dto.setEmail("pepe@example.com");
        dto.setFoto("foto.png");
        dto.setBloqueado(false);
        Date fecha = new Date();
        dto.setFecharegistro(fecha);
        dto.setAlias("alias1");
        dto.setCampoespecializacion("Música");
        dto.setDescripcion("Descr");

        assertEquals("g1", dto.getId());
        assertEquals("Pepe", dto.getNombre());
        assertEquals("Lopez", dto.getApellidos());
        assertEquals("pepe@example.com", dto.getEmail());
        assertEquals("foto.png", dto.getFoto());
        assertFalse(dto.isBloqueado());
        assertEquals(fecha, dto.getFecharegistro());
        assertEquals("alias1", dto.getAlias());
        assertEquals("Música", dto.getCampoespecializacion());
        assertEquals("Descr", dto.getDescripcion());
    }
}
