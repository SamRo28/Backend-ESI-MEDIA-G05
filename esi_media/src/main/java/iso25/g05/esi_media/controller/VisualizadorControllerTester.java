package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.model.VisualizadorRegistroDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Date;
import java.util.Calendar;

/**
 * Clase helper para testing manual del controlador.
 * 
 * PROPÓSITO: Proporciona ejemplos de JSON válidos e inválidos
 * para probar el endpoint sin necesidad de Postman o frontend.
 * 
 * USO: Ejecutar los métodos main para ver ejemplos de DTOs
 * y usarlos para testing con herramientas como curl o Postman.
 */
public class VisualizadorControllerTester {
    
    /**
     * Crea un DTO de ejemplo VÁLIDO para testing
     */
    public static VisualizadorRegistroDTO crearDTOValido() {
        // Crear fecha de nacimiento (25 años)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -25);
        Date fechaNac = cal.getTime();
        
        return new VisualizadorRegistroDTO(
            "Juan",                    // nombre
            "Pérez García",            // apellidos  
            "juan.perez@email.com",    // email
            "juanito",                 // alias
            fechaNac,                  // fechaNac
            "MiPassword123!",          // password
            "MiPassword123!",          // passwordConfirm
            false,                     // vip
            "avatar.jpg"               // foto
        );
    }
    
    /**
     * Crea un DTO de ejemplo INVÁLIDO para testing de errores
     */
    public static VisualizadorRegistroDTO crearDTOInvalido() {
        // Crear fecha de nacimiento futura (imposible)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        Date fechaFutura = cal.getTime();
        
        return new VisualizadorRegistroDTO(
            "",                        // nombre VACÍO (error)
            "",                        // apellidos VACÍO (error)
            "email-invalido",          // email INVÁLIDO (error)
            "alias-muy-largo-para-12-caracteres", // alias DEMASIADO LARGO (error)
            fechaFutura,               // fechaNac FUTURA (error)
            "123",                     // password MUY CORTA (error)
            "456",                     // passwordConfirm NO COINCIDE (error)
            false,                     // vip
            null                       // foto
        );
    }
    
    /**
     * Crea un DTO donde las contraseñas no coinciden
     */
    public static VisualizadorRegistroDTO crearDTOContrasenasNoCoinciden() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20);
        Date fechaNac = cal.getTime();
        
        return new VisualizadorRegistroDTO(
            "María",                   // nombre
            "González López",          // apellidos
            "maria@email.com",         // email
            "maria",                   // alias
            fechaNac,                  // fechaNac
            "Password123!",            // password
            "Password456!",            // passwordConfirm DIFERENTE (error)
            true,                      // vip
            "foto.png"                 // foto
        );
    }
    
    /**
     * Convierte un DTO a JSON para usar en requests HTTP
     */
    public static String convertirAJSON(VisualizadorRegistroDTO dto) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(dto);
        } catch (Exception e) {
            return "Error convirtiendo a JSON: " + e.getMessage();
        }
    }
    
    /**
     * Método main para generar ejemplos de JSON
     */
    public static void main(String[] args) {
        System.out.println("=== TESTING VISUALIZADOR CONTROLLER ===\n");
        
        // DTO VÁLIDO
        System.out.println("1. EJEMPLO DE JSON VÁLIDO:");
        System.out.println("POST /api/visualizador/registro");
        System.out.println("Content-Type: application/json");
        System.out.println();
        VisualizadorRegistroDTO dtoValido = crearDTOValido();
        System.out.println(convertirAJSON(dtoValido));
        System.out.println();
        System.out.println("RESULTADO ESPERADO: HTTP 201 Created con datos del usuario");
        System.out.println("========================================================\n");
        
        // DTO INVÁLIDO
        System.out.println("2. EJEMPLO DE JSON INVÁLIDO (múltiples errores):");
        System.out.println("POST /api/visualizador/registro");
        System.out.println("Content-Type: application/json");
        System.out.println();
        VisualizadorRegistroDTO dtoInvalido = crearDTOInvalido();
        System.out.println(convertirAJSON(dtoInvalido));
        System.out.println();
        System.out.println("RESULTADO ESPERADO: HTTP 400 Bad Request con lista de errores");
        System.out.println("===============================================================\n");
        
        // DTO CON CONTRASEÑAS NO COINCIDENTES
        System.out.println("3. EJEMPLO CON CONTRASEÑAS QUE NO COINCIDEN:");
        System.out.println("POST /api/visualizador/registro");
        System.out.println("Content-Type: application/json");
        System.out.println();
        VisualizadorRegistroDTO dtoContrasenaDif = crearDTOContrasenasNoCoinciden();
        System.out.println(convertirAJSON(dtoContrasenaDif));
        System.out.println();
        System.out.println("RESULTADO ESPERADO: HTTP 400 con error 'contraseñas no coinciden'");
        System.out.println("==================================================================\n");
        
        // COMANDOS CURL DE EJEMPLO
        System.out.println("4. COMANDOS CURL PARA TESTING:");
        System.out.println();
        System.out.println("# Registro exitoso:");
        System.out.println("curl -X POST http://localhost:8080/api/visualizador/registro \\");
        System.out.println("  -H \"Content-Type: application/json\" \\");
        System.out.println("  -d '" + convertirAJSON(dtoValido) + "'");
        System.out.println();
        System.out.println("# Ver todos los registrados:");
        System.out.println("curl http://localhost:8080/api/visualizador/todos");
        System.out.println();
        System.out.println("# Limpiar registros:");
        System.out.println("curl -X DELETE http://localhost:8080/api/visualizador/limpiar");
        System.out.println();
        System.out.println("# Intentar registrar usuario duplicado (ejecutar 2 veces el primer curl)");
        System.out.println("# Debería dar error 'email ya registrado' en el segundo intento");
    }
}