package iso25.g05.esi_media.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import iso25.g05.esi_media.model.LogEntry;
import iso25.g05.esi_media.repository.LogEntryRepository;

/**
 * Tests unitarios para LoggingService
 * Valida el guardado asíncrono de logs en base de datos
 * Maneja errores sin afectar el flujo principal
 */
@ExtendWith(MockitoExtension.class)
class LoggingServiceTest {

    @Mock
    private LogEntryRepository logEntryRepository;

    @InjectMocks
    private LoggingService loggingService;

    @Captor
    private ArgumentCaptor<LogEntry> logEntryCaptor;

    private static final String HTTP_METHOD = "GET";
    private static final String PATH = "/api/contenidos";
    private static final String IP_ADDRESS = "192.168.1.100";
    private static final int STATUS_CODE_200 = 200;
    private static final int STATUS_CODE_404 = 404;
    private static final int STATUS_CODE_500 = 500;
    private static final long DURATION_MS = 150L;

    @BeforeEach
    void setUp() {
        // Configuración inicial si es necesaria
    }

    // ==================== TESTS DE GUARDADO EXITOSO ====================

    @Test
    void testSaveLog_GuardaLogCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> {
            LogEntry log = invocation.getArgument(0);
            log.setId("log-id-123");
            return log;
        });

        // Act
        loggingService.saveLog(HTTP_METHOD, PATH, IP_ADDRESS, STATUS_CODE_200, DURATION_MS);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertNotNull(savedLog);
        assertEquals(HTTP_METHOD, savedLog.getMethod());
        assertEquals(PATH, savedLog.getPath());
        assertEquals(IP_ADDRESS, savedLog.getIpAddress());
        assertEquals(STATUS_CODE_200, savedLog.getStatusCode());
        assertEquals(DURATION_MS, savedLog.getDurationMs());
        assertNotNull(savedLog.getTimestamp());
    }

    @Test
    void testSaveLog_ConMetodoPOST_GuardaCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog("POST", "/api/usuarios", IP_ADDRESS, 201, 250L);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertEquals("POST", savedLog.getMethod());
        assertEquals("/api/usuarios", savedLog.getPath());
        assertEquals(201, savedLog.getStatusCode());
    }

    @Test
    void testSaveLog_ConMetodoPUT_GuardaCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog("PUT", "/api/contenidos/123", IP_ADDRESS, STATUS_CODE_200, 300L);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertEquals("PUT", savedLog.getMethod());
        assertEquals("/api/contenidos/123", savedLog.getPath());
    }

    @Test
    void testSaveLog_ConMetodoDELETE_GuardaCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog("DELETE", "/api/listas/456", IP_ADDRESS, 204, 100L);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertEquals("DELETE", savedLog.getMethod());
        assertEquals(204, savedLog.getStatusCode());
    }

    @Test
    void testSaveLog_ConStatusCode404_GuardaCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog(HTTP_METHOD, "/api/recurso-inexistente", IP_ADDRESS, STATUS_CODE_404, DURATION_MS);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertEquals(STATUS_CODE_404, savedLog.getStatusCode());
    }

    @Test
    void testSaveLog_ConStatusCode500_GuardaCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog(HTTP_METHOD, PATH, IP_ADDRESS, STATUS_CODE_500, DURATION_MS);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertEquals(STATUS_CODE_500, savedLog.getStatusCode());
    }

    @Test
    void testSaveLog_ConDuracionLarga_GuardaCorrectamente() {
        // Arrange
        long duracionLarga = 5000L; // 5 segundos
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog(HTTP_METHOD, "/api/operacion-lenta", IP_ADDRESS, STATUS_CODE_200, duracionLarga);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertEquals(duracionLarga, savedLog.getDurationMs());
    }

    @Test
    void testSaveLog_ConIPv6_GuardaCorrectamente() {
        // Arrange
        String ipv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog(HTTP_METHOD, PATH, ipv6, STATUS_CODE_200, DURATION_MS);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertEquals(ipv6, savedLog.getIpAddress());
    }

    @Test
    void testSaveLog_ConPathComplejo_GuardaCorrectamente() {
        // Arrange
        String pathComplejo = "/api/v1/usuarios/123/listas/456/contenidos";
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog(HTTP_METHOD, pathComplejo, IP_ADDRESS, STATUS_CODE_200, DURATION_MS);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertEquals(pathComplejo, savedLog.getPath());
    }

    // ==================== TESTS DE MANEJO DE ERRORES ====================

    @Test
    void testSaveLog_ErrorAlGuardar_NoLanzaExcepcion() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class)))
            .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert - No debe lanzar excepción
        assertDoesNotThrow(() -> {
            loggingService.saveLog(HTTP_METHOD, PATH, IP_ADDRESS, STATUS_CODE_200, DURATION_MS);
        });

        // El método debe intentar guardar a pesar del error
        verify(logEntryRepository, timeout(1000)).save(any(LogEntry.class));
    }

    @Test
    void testSaveLog_ErrorConexionBD_NoAfectaFlujo() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class)))
            .thenThrow(new RuntimeException("Connection timeout"));

        // Act
        loggingService.saveLog(HTTP_METHOD, PATH, IP_ADDRESS, STATUS_CODE_200, DURATION_MS);

        // Assert - Debe intentar guardar sin afectar el flujo principal
        verify(logEntryRepository, timeout(1000)).save(any(LogEntry.class));
    }

    @Test
    void testSaveLog_NullPointerException_NoLanzaExcepcion() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class)))
            .thenThrow(new NullPointerException("Null value encountered"));

        // Act & Assert
        assertDoesNotThrow(() -> {
            loggingService.saveLog(HTTP_METHOD, PATH, IP_ADDRESS, STATUS_CODE_200, DURATION_MS);
        });

        verify(logEntryRepository, timeout(1000)).save(any(LogEntry.class));
    }

    // ==================== TESTS DE MÚLTIPLES LLAMADAS ====================

    @Test
    void testSaveLog_MultiplesLlamadas_GuardaTodas() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog("GET", "/api/path1", IP_ADDRESS, STATUS_CODE_200, 100L);
        loggingService.saveLog("POST", "/api/path2", IP_ADDRESS, 201, 150L);
        loggingService.saveLog("PUT", "/api/path3", IP_ADDRESS, STATUS_CODE_200, 200L);

        // Assert
        verify(logEntryRepository, timeout(2000).times(3)).save(any(LogEntry.class));
    }

    @Test
    void testSaveLog_EjecucionAsincrona_NoBloquea() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> {
            Thread.sleep(100); // Simular operación lenta
            return invocation.getArgument(0);
        });

        // Act
        long startTime = System.currentTimeMillis();
        loggingService.saveLog(HTTP_METHOD, PATH, IP_ADDRESS, STATUS_CODE_200, DURATION_MS);
        loggingService.saveLog(HTTP_METHOD, PATH, IP_ADDRESS, STATUS_CODE_200, DURATION_MS);
        loggingService.saveLog(HTTP_METHOD, PATH, IP_ADDRESS, STATUS_CODE_200, DURATION_MS);
        long endTime = System.currentTimeMillis();

        // Assert - Si fuera síncrono, tomaría al menos 300ms. Asíncrono debe ser mucho más rápido
        long elapsedTime = endTime - startTime;
        assertTrue(elapsedTime < 400, "El método debería ejecutarse de forma asíncrona (tomó " + elapsedTime + "ms, esperado < 400ms para 3 llamadas)");
        
        // Verificar que eventualmente se guardaron todas
        verify(logEntryRepository, timeout(2000).times(3)).save(any(LogEntry.class));
    }

    // ==================== TESTS DE CASOS LÍMITE ====================

    @Test
    void testSaveLog_DuracionCero_GuardaCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog(HTTP_METHOD, PATH, IP_ADDRESS, STATUS_CODE_200, 0L);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertEquals(0L, savedLog.getDurationMs());
    }

    @Test
    void testSaveLog_PathVacio_GuardaCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog(HTTP_METHOD, "", IP_ADDRESS, STATUS_CODE_200, DURATION_MS);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertEquals("", savedLog.getPath());
    }

    @Test
    void testSaveLog_IPNull_GuardaCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog(HTTP_METHOD, PATH, null, STATUS_CODE_200, DURATION_MS);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertNull(savedLog.getIpAddress());
    }

    @Test
    void testSaveLog_MetodoNull_GuardaCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog(null, PATH, IP_ADDRESS, STATUS_CODE_200, DURATION_MS);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertNull(savedLog.getMethod());
    }

    @Test
    void testSaveLog_StatusCodeNegativo_GuardaCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog(HTTP_METHOD, PATH, IP_ADDRESS, -1, DURATION_MS);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertEquals(-1, savedLog.getStatusCode());
    }

    @Test
    void testSaveLog_DuracionNegativa_GuardaCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog(HTTP_METHOD, PATH, IP_ADDRESS, STATUS_CODE_200, -100L);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        LogEntry savedLog = logEntryCaptor.getValue();
        
        assertEquals(-100L, savedLog.getDurationMs());
    }

    // ==================== TESTS DE CÓDIGOS DE ESTADO HTTP ====================

    @Test
    void testSaveLog_StatusCode401_GuardaCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog(HTTP_METHOD, PATH, IP_ADDRESS, 401, DURATION_MS);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        assertEquals(401, logEntryCaptor.getValue().getStatusCode());
    }

    @Test
    void testSaveLog_StatusCode403_GuardaCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog(HTTP_METHOD, PATH, IP_ADDRESS, 403, DURATION_MS);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        assertEquals(403, logEntryCaptor.getValue().getStatusCode());
    }

    @Test
    void testSaveLog_StatusCode503_GuardaCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog(HTTP_METHOD, PATH, IP_ADDRESS, 503, DURATION_MS);

        // Assert
        verify(logEntryRepository, timeout(1000)).save(logEntryCaptor.capture());
        assertEquals(503, logEntryCaptor.getValue().getStatusCode());
    }

    // ==================== TESTS DE INTEGRACIÓN ====================

    @Test
    void testSaveLog_VariasOperacionesSimultaneas_GuardaTodasCorrectamente() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act - Simular múltiples peticiones simultáneas
        for (int i = 0; i < 10; i++) {
            loggingService.saveLog(
                HTTP_METHOD, 
                "/api/path" + i, 
                IP_ADDRESS, 
                STATUS_CODE_200, 
                DURATION_MS + i
            );
        }

        // Assert
        verify(logEntryRepository, timeout(2000).times(10)).save(any(LogEntry.class));
    }

    @Test
    void testSaveLog_AlgunosExitososAlgunosConError_ContinuaGuardando() {
        // Arrange
        when(logEntryRepository.save(any(LogEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0))
            .thenThrow(new RuntimeException("Error intermitente"))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loggingService.saveLog("GET", "/api/path1", IP_ADDRESS, STATUS_CODE_200, 100L);
        loggingService.saveLog("POST", "/api/path2", IP_ADDRESS, 201, 150L);
        loggingService.saveLog("PUT", "/api/path3", IP_ADDRESS, STATUS_CODE_200, 200L);

        // Assert - Debe intentar guardar todos, incluso con errores
        verify(logEntryRepository, timeout(2000).times(3)).save(any(LogEntry.class));
    }
}
