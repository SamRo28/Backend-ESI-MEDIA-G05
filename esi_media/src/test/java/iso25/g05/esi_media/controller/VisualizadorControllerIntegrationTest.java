package iso25.g05.esi_media.controller;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import iso25.g05.esi_media.config.MongoTestConfig;
import iso25.g05.esi_media.dto.VisualizadorRegistroDTO;
import iso25.g05.esi_media.repository.VisualizadorRepository;
import iso25.g05.esi_media.service.VisualizadorService;

/**
 * Pruebas de integración para el controlador de visualizadores.
 * 
 * Estas pruebas verifican el flujo completo desde la API REST hasta la capa de servicio,
 * sin usar mocks para el servicio, verificando la integración real entre componentes.
 * 
 * Se importa la configuración MongoTestConfig para asegurar que todas las
 * operaciones MongoDB se realicen en la base de datos de pruebas.
 */
@SpringBootTest(classes = {MongoTestConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas de integración del registro de visualizadores")
public class VisualizadorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private VisualizadorService visualizadorService;
    
    @Autowired
    private VisualizadorRepository visualizadorRepository;
    
    // Almacenar los emails de los visualizadores creados para limpiarlos después
    private final java.util.List<String> visualizadoresCreados = new java.util.ArrayList<>();
    
    @BeforeEach
    void setUp() {
        // Configurar ObjectMapper para manejar fechas correctamente
        objectMapper.registerModule(new JavaTimeModule());
        visualizadoresCreados.clear();
    }
    
    @AfterEach
    void tearDown() {
        // Limpiar solo los visualizadores específicos creados en pruebas
        visualizadoresCreados.forEach(email -> {
            visualizadorService.buscarPorEmail(email).ifPresent(v -> 
                visualizadorRepository.delete(v));
        });
    }
    
    /**
     * Método auxiliar para crear un DTO válido para pruebas
     */
    private VisualizadorRegistroDTO crearDTOValido() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -25); // 25 años atrás
        Date fechaNac = cal.getTime();
        
        return new VisualizadorRegistroDTO(
            "Juan",
            "Pérez García",
            "juan.perez@test.com",
            "juanito",
            fechaNac,
            "S3cur3P@ssw0rd!Xz9",
            "S3cur3P@ssw0rd!Xz9",
            false,
            "avatar.jpg"
        );
    }
    
    /**
     * Método auxiliar para crear un DTO con errores de validación
     */
    private VisualizadorRegistroDTO crearDTOInvalido() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -2); // Solo 2 años (demasiado joven)
        Date fechaNac = cal.getTime();
        
        return new VisualizadorRegistroDTO(
            "", // Nombre vacío
            "Apellidos",
            "email-invalido", // Formato de email inválido
            "alias",
            fechaNac,
            "123", // Contraseña demasiado corta
            "456", // No coincide
            false,
            null
        );
    }
    
    /**
     * Test para verificar el registro exitoso de un visualizador
     */
    @Test
    @DisplayName("Registro exitoso devuelve estado HTTP 201 con datos del visualizador")
    void registroExitoso() throws Exception {
        // Arrange
        VisualizadorRegistroDTO dto = crearDTOValido();
        String requestBody = objectMapper.writeValueAsString(dto);
        
        // Registrar el email para limpieza posterior
        visualizadoresCreados.add(dto.getEmail());
        
        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/visualizador/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.exitoso").value(true))
            .andExpect(jsonPath("$.visualizador").exists())
            .andExpect(jsonPath("$.visualizador.nombre").value(dto.getNombre()))
            .andExpect(jsonPath("$.visualizador.email").value(dto.getEmail()))
            .andReturn();
        
        // Verificación adicional: Comprobar que el visualizador existe en el servicio
        assertTrue(visualizadorService.buscarPorEmail(dto.getEmail()).isPresent(),
                  "El visualizador debe existir en el servicio después del registro");
    }
    
    /**
     * Test para verificar el manejo de errores de validación
     */
    @Test
    @DisplayName("Registro con datos inválidos devuelve estado HTTP 400 con errores")
    void registroDatosInvalidos() throws Exception {
        // Arrange
        VisualizadorRegistroDTO dto = crearDTOInvalido();
        String requestBody = objectMapper.writeValueAsString(dto);
        
        // Act & Assert
        mockMvc.perform(post("/api/visualizador/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exitoso").value(false))
            .andExpect(jsonPath("$.errores").isArray())
            .andExpect(jsonPath("$.errores.length()").value(org.hamcrest.Matchers.greaterThan(0)))
            .andReturn();
    }
    
    /**
     * Test para verificar el manejo de email duplicado
     */
    @Test
    @DisplayName("Registro con email duplicado devuelve estado HTTP 400 con error específico")
    void registroEmailDuplicado() throws Exception {
        // Arrange - Primero registramos un visualizador
        VisualizadorRegistroDTO primerDto = crearDTOValido();
        String primerRequestBody = objectMapper.writeValueAsString(primerDto);
        
        // Registrar el email para limpieza posterior
        visualizadoresCreados.add(primerDto.getEmail());
        
        mockMvc.perform(post("/api/visualizador/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(primerRequestBody))
            .andExpect(status().isCreated());
        
        // Ahora intentamos registrar otro con el mismo email
        VisualizadorRegistroDTO segundoDto = crearDTOValido(); // Mismo email
        segundoDto.setNombre("Otro Nombre"); // Cambiar otros datos
        String segundoRequestBody = objectMapper.writeValueAsString(segundoDto);
        
        // Act & Assert
        mockMvc.perform(post("/api/visualizador/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(segundoRequestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exitoso").value(false))
            .andExpect(jsonPath("$.errores[0]").value(org.hamcrest.Matchers.containsString("email ya está registrado")))
            .andReturn();
    }
    
    /**
     * Test para verificar la obtención de todos los visualizadores
     */
    @Test
    @DisplayName("Obtener todos los visualizadores devuelve lista correcta")
    void obtenerTodosVisualizadores() throws Exception {
        // Arrange - Registrar dos visualizadores
        VisualizadorRegistroDTO dto1 = crearDTOValido();
        String requestBody1 = objectMapper.writeValueAsString(dto1);
        
        // Registrar los emails para limpieza posterior
        visualizadoresCreados.add(dto1.getEmail());
        
        mockMvc.perform(post("/api/visualizador/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody1))
            .andExpect(status().isCreated());
        
        VisualizadorRegistroDTO dto2 = crearDTOValido();
        dto2.setEmail("otro@test.com"); // Email diferente
        dto2.setNombre("Otro Usuario");
        String requestBody2 = objectMapper.writeValueAsString(dto2);
        
        // Registrar el segundo email para limpieza posterior
        visualizadoresCreados.add(dto2.getEmail());

        mockMvc.perform(post("/api/visualizador/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody2))
            .andExpect(status().isCreated());
        
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/visualizador/todos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.visualizadores").isArray())
            // No verificamos el número exacto porque puede haber visualizadores previos
            .andExpect(jsonPath("$.visualizadores.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
            // La siguiente validación ya no es necesaria porque el backend devuelve correctamente los datos
            // Solo verificamos que el campo total exista
            .andExpect(jsonPath("$.total").exists())
            .andReturn();
            
        // Opcionalmente podemos verificar manualmente que total coincida con la longitud del array
        String responseContent = result.getResponse().getContentAsString();
        org.json.JSONObject jsonResponse = new org.json.JSONObject(responseContent);
        int total = jsonResponse.getInt("total");
        int arrayLength = jsonResponse.getJSONArray("visualizadores").length();
        org.junit.jupiter.api.Assertions.assertEquals(total, arrayLength, 
            "El valor de 'total' debería ser igual a la longitud del array 'visualizadores'");
    }
    
    /**
     * Test para verificar la eliminación individual de visualizadores
     */
    @Test
    @DisplayName("Eliminar visualizador funciona correctamente")
    void eliminarVisualizador() throws Exception {
        // Arrange - Registrar un visualizador
        VisualizadorRegistroDTO dto = crearDTOValido();
        String requestBody = objectMapper.writeValueAsString(dto);
        
        visualizadoresCreados.add(dto.getEmail());
        
        mockMvc.perform(post("/api/visualizador/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isCreated());
        
        // Verificar que el visualizador existe
        assertTrue(visualizadorService.buscarPorEmail(dto.getEmail()).isPresent(),
                  "El visualizador debe existir en el servicio después del registro");
        
        // Obtener el ID del visualizador creado
        String visualizadorId = visualizadorService.buscarPorEmail(dto.getEmail()).get().getId();
        
        // Act - Eliminar el visualizador específico
        mockMvc.perform(delete("/api/visualizador/eliminar/" + visualizadorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exitoso").value(true))
            .andReturn();
        
        // Assert - Verificar que se eliminó correctamente
        assertFalse(visualizadorService.buscarPorEmail(dto.getEmail()).isPresent(),
                  "El visualizador no debe existir después de eliminarlo");
    }
}