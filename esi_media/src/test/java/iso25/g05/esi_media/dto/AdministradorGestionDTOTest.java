package iso25.g05.esi_media.dto;

import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class AdministradorGestionDTOTest {

    @Test
    void testGettersAndSetters() {
        AdministradorGestionDTO dto = new AdministradorGestionDTO();
        dto.setId("id123");
        dto.setNombre("Ana");
        dto.setApellidos("García");
        dto.setEmail("ana@example.com");
        dto.setFoto("foto.png");
        dto.setBloqueado(true);
        Date fecha = new Date();
        dto.setFecharegistro(fecha);
        dto.setDepartamento("IT");

        assertEquals("id123", dto.getId());
        assertEquals("Ana", dto.getNombre());
        assertEquals("García", dto.getApellidos());
        assertEquals("ana@example.com", dto.getEmail());
        assertEquals("foto.png", dto.getFoto());
        assertTrue(dto.isBloqueado());
        assertEquals(fecha, dto.getFecharegistro());
        assertEquals("IT", dto.getDepartamento());
    }
}
