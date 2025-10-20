package iso25.g05.esi_media.controller;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import iso25.g05.esi_media.dto.VisualizadorRegistroDTO;

/**
 * Test class converted from a manual helper into unit tests.
 * Cada método original se convierte en un test independiente.
 */
class VisualizadorControllerTest {

    // Helper factory methods kept private for reuse in tests
    private static VisualizadorRegistroDTO crearDTOValido() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -25);
        Date fechaNac = cal.getTime();

        return new VisualizadorRegistroDTO(
            "Juan",
            "Pérez García",
            "juan.perez@email.com",
            "juanito",
            fechaNac,
            "MiPassword123!",
            "MiPassword123!",
            false,
            "avatar.jpg"
        );
    }

    private static VisualizadorRegistroDTO crearDTOInvalido() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        Date fechaFutura = cal.getTime();

        return new VisualizadorRegistroDTO(
            "",
            "",
            "email-invalido",
            "alias-muy-largo-para-12-caracteres",
            fechaFutura,
            "123",
            "456",
            false,
            null
        );
    }

    private static VisualizadorRegistroDTO crearDTOContrasenasNoCoinciden() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20);
        Date fechaNac = cal.getTime();

        return new VisualizadorRegistroDTO(
            "María",
            "González López",
            "maria@email.com",
            "maria",
            fechaNac,
            "Password123!",
            "Password456!",
            true,
            "foto.png"
        );
    }

    private static String convertirAJSON(VisualizadorRegistroDTO dto) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(dto);
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    void dtoValidoShouldBeCreatedAndJsonSerializable() {
        VisualizadorRegistroDTO dto = crearDTOValido();
        assertNotNull(dto, "DTO válido no debe ser null");

        String json = convertirAJSON(dto);
        assertNotNull(json, "La conversión a JSON no debe devolver null");
        assertTrue(json.contains("juan.perez@email.com"), "JSON debe contener el email");
    }

    @Test
    void dtoInvalidoShouldBeCreatedAndJsonSerializable() {
        VisualizadorRegistroDTO dto = crearDTOInvalido();
        assertNotNull(dto, "DTO inválido no debe ser null");

        String json = convertirAJSON(dto);
        assertNotNull(json, "La conversión a JSON no debe devolver null aunque los datos sean inválidos");
        assertTrue(json.contains("email-invalido"), "JSON debe contener el email inválido");
    }

    @Test
    void dtoContrasenasNoCoincidenShouldBeCreatedAndJsonSerializable() {
        VisualizadorRegistroDTO dto = crearDTOContrasenasNoCoinciden();
        assertNotNull(dto, "DTO con contraseñas no coincidentes no debe ser null");

        String json = convertirAJSON(dto);
        assertNotNull(json, "La conversión a JSON no debe devolver null");
        assertTrue(json.contains("Password123"), "JSON debe contener la contraseña");
    }
}