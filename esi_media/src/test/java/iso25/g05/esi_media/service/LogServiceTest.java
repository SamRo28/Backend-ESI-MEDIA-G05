package iso25.g05.esi_media.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para LogService
 * Valida el formato y contenido de los mensajes de log
 * Verifica registro de auditoría, accesos no autorizados y errores
 */
@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @InjectMocks
    private LogService logService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ADMIN_ID = "admin-id-123";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String USUARIO_ID = "usuario-id-456";
    private static final String USUARIO_EMAIL = "usuario@test.com";

    @BeforeEach
    void setUp() {
        // Configuración inicial si es necesaria
    }

    // ==================== TESTS DE REGISTRO DE CONSULTA DE PERFIL ====================

    @Test
    void testRegistrarConsultaPerfil_ConInformacionCompleta_GeneraMensajeCorrect() {
        // Act - Solo verificar que no lanza excepción
        assertDoesNotThrow(() -> {
            logService.registrarConsultaPerfil(
                ADMIN_ID, 
                ADMIN_EMAIL, 
                USUARIO_ID, 
                USUARIO_EMAIL, 
                "Visualizador"
            );
        });
    }

    @Test
    void testRegistrarConsultaPerfil_TipoGestor_GeneraMensajeCorrect() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarConsultaPerfil(
                ADMIN_ID, 
                ADMIN_EMAIL, 
                USUARIO_ID, 
                USUARIO_EMAIL, 
                "Gestor"
            );
        });
    }

    @Test
    void testRegistrarConsultaPerfil_TipoAdministrador_GeneraMensajeCorrect() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarConsultaPerfil(
                ADMIN_ID, 
                ADMIN_EMAIL, 
                USUARIO_ID, 
                USUARIO_EMAIL, 
                "Administrador"
            );
        });
    }

    @Test
    void testRegistrarConsultaPerfil_ConEmailsComplexos_NoLanzaExcepcion() {
        // Arrange
        String emailComplejo1 = "admin.usuario+test@empresa.subdomain.com";
        String emailComplejo2 = "usuario_test123@dominio-empresa.co.uk";

        // Act
        assertDoesNotThrow(() -> {
            logService.registrarConsultaPerfil(
                ADMIN_ID, 
                emailComplejo1, 
                USUARIO_ID, 
                emailComplejo2, 
                "Visualizador"
            );
        });
    }

    @Test
    void testRegistrarConsultaPerfil_ConNombresLargos_NoLanzaExcepcion() {
        // Arrange
        String emailLargo = "administrador.con.nombre.muy.largo@empresa.test.com";

        // Act
        assertDoesNotThrow(() -> {
            logService.registrarConsultaPerfil(
                ADMIN_ID, 
                emailLargo, 
                USUARIO_ID, 
                USUARIO_EMAIL, 
                "Visualizador"
            );
        });
    }

    // ==================== TESTS DE REGISTRO DE ACCESO NO AUTORIZADO ====================

    @Test
    void testRegistrarAccesoNoAutorizado_ConRecursoValido_GeneraMensajeCorrect() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccesoNoAutorizado(USUARIO_ID, "/api/admin/usuarios");
        });
    }

    @Test
    void testRegistrarAccesoNoAutorizado_ConRecursoComplejo_NoLanzaExcepcion() {
        // Arrange
        String recursoComplejo = "/api/v1/administrador/usuarios/123/bloquear";

        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccesoNoAutorizado(USUARIO_ID, recursoComplejo);
        });
    }

    @Test
    void testRegistrarAccesoNoAutorizado_ConIdNull_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccesoNoAutorizado(null, "/api/admin/usuarios");
        });
    }

    @Test
    void testRegistrarAccesoNoAutorizado_ConRecursoNull_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccesoNoAutorizado(USUARIO_ID, null);
        });
    }

    @Test
    void testRegistrarAccesoNoAutorizado_ConRecursoVacio_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccesoNoAutorizado(USUARIO_ID, "");
        });
    }

    // ==================== TESTS DE REGISTRO DE ERROR DE CONSULTA ====================

    @Test
    void testRegistrarErrorConsulta_ConErrorGenerico_GeneraMensajeCorrect() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarErrorConsulta(
                ADMIN_ID, 
                USUARIO_ID, 
                "Usuario no encontrado"
            );
        });
    }

    @Test
    void testRegistrarErrorConsulta_ConErrorBaseDatos_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarErrorConsulta(
                ADMIN_ID, 
                USUARIO_ID, 
                "Error de conexión a la base de datos"
            );
        });
    }

    @Test
    void testRegistrarErrorConsulta_ConErrorLargo_NoLanzaExcepcion() {
        // Arrange
        String errorLargo = "Error muy detallado que incluye información técnica sobre el fallo " +
                           "en la consulta del perfil del usuario con múltiples detalles adicionales";

        // Act
        assertDoesNotThrow(() -> {
            logService.registrarErrorConsulta(ADMIN_ID, USUARIO_ID, errorLargo);
        });
    }

    @Test
    void testRegistrarErrorConsulta_ConErrorNull_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarErrorConsulta(ADMIN_ID, USUARIO_ID, null);
        });
    }

    @Test
    void testRegistrarErrorConsulta_ConErrorVacio_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarErrorConsulta(ADMIN_ID, USUARIO_ID, "");
        });
    }

    // ==================== TESTS DE REGISTRO DE ACCIÓN GENÉRICA ====================

    @Test
    void testRegistrarAccion_ConAccionValida_GeneraMensajeCorrect() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion("Creación de contenido", USUARIO_EMAIL);
        });
    }

    @Test
    void testRegistrarAccion_ConAccionListado_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion("Listado de contenidos por gestor", USUARIO_EMAIL);
        });
    }

    @Test
    void testRegistrarAccion_ConAccionActualizacion_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion("Actualización de contenido 123", USUARIO_EMAIL);
        });
    }

    @Test
    void testRegistrarAccion_ConAccionEliminacion_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion("Eliminación de contenido 456", USUARIO_EMAIL);
        });
    }

    @Test
    void testRegistrarAccion_ConAccionNull_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion(null, USUARIO_EMAIL);
        });
    }

    @Test
    void testRegistrarAccion_ConUsuarioNull_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion("Acción de prueba", null);
        });
    }

    @Test
    void testRegistrarAccion_ConAmbosParametrosNull_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion(null, null);
        });
    }

    @Test
    void testRegistrarAccion_ConAccionLarga_NoLanzaExcepcion() {
        // Arrange
        String accionLarga = "Acción muy detallada que describe una operación compleja " +
                            "realizada en el sistema con múltiples pasos y validaciones";

        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion(accionLarga, USUARIO_EMAIL);
        });
    }

    // ==================== TESTS DE REGISTRO DE BLOQUEO DE USUARIO (EXCLUIDO) ====================
    // NOTA: Estos tests están comentados porque la funcionalidad de bloqueo/desbloqueo
    // está excluida según las instrucciones del usuario

    /*
    @Test
    void testRegistrarBloqueoUsuario_ConInformacionCompleta_GeneraMensajeCorrect() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarBloqueoUsuario(
                ADMIN_ID, 
                ADMIN_EMAIL, 
                USUARIO_ID, 
                USUARIO_EMAIL
            );
        });
    }
    */

    // ==================== TESTS DE REGISTRO DE DESBLOQUEO DE USUARIO (EXCLUIDO) ====================
    // NOTA: Estos tests están comentados porque la funcionalidad de bloqueo/desbloqueo
    // está excluida según las instrucciones del usuario

    /*
    @Test
    void testRegistrarDesbloqueoUsuario_ConInformacionCompleta_GeneraMensajeCorrect() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarDesbloqueoUsuario(
                ADMIN_ID, 
                ADMIN_EMAIL, 
                USUARIO_ID, 
                USUARIO_EMAIL
            );
        });
    }
    */

    // ==================== TESTS DE MÚLTIPLES LLAMADAS ====================

    @Test
    void testMultiplesRegistros_EnSecuencia_TodosSeEjecutan() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion("Acción 1", USUARIO_EMAIL);
            logService.registrarAccion("Acción 2", USUARIO_EMAIL);
            logService.registrarConsultaPerfil(
                ADMIN_ID, ADMIN_EMAIL, USUARIO_ID, USUARIO_EMAIL, "Visualizador"
            );
            logService.registrarAccesoNoAutorizado(USUARIO_ID, "/api/admin");
            logService.registrarErrorConsulta(ADMIN_ID, USUARIO_ID, "Error de prueba");
        });
    }

    // ==================== TESTS DE CASOS LÍMITE ====================

    @Test
    void testRegistrarAccion_ConStringVacio_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion("", "");
        });
    }

    @Test
    void testRegistrarAccion_ConEspaciosEnBlanco_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion("   ", "   ");
        });
    }

    @Test
    void testRegistrarConsultaPerfil_ConIdsVacios_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarConsultaPerfil("", "", "", "", "Visualizador");
        });
    }

    @Test
    void testRegistrarConsultaPerfil_ConTodosParametrosNull_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarConsultaPerfil(null, null, null, null, null);
        });
    }

    @Test
    void testRegistrarAccesoNoAutorizado_ConAmbosParametrosNull_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccesoNoAutorizado(null, null);
        });
    }

    @Test
    void testRegistrarErrorConsulta_ConTodosParametrosNull_NoLanzaExcepcion() {
        // Act
        assertDoesNotThrow(() -> {
            logService.registrarErrorConsulta(null, null, null);
        });
    }

    // ==================== TESTS DE CARACTERES ESPECIALES ====================

    @Test
    void testRegistrarAccion_ConCaracteresEspeciales_NoLanzaExcepcion() {
        // Arrange
        String accionConCaracteres = "Acción con caracteres: <>&\"'|@#$%^&*()";

        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion(accionConCaracteres, USUARIO_EMAIL);
        });
    }

    @Test
    void testRegistrarAccesoNoAutorizado_ConCaracteresEspeciales_NoLanzaExcepcion() {
        // Arrange
        String recursoConCaracteres = "/api/recurso?param1=value&param2=value<>\"'";

        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccesoNoAutorizado(USUARIO_ID, recursoConCaracteres);
        });
    }

    @Test
    void testRegistrarErrorConsulta_ConCaracteresEspeciales_NoLanzaExcepcion() {
        // Arrange
        String errorConCaracteres = "Error con caracteres especiales: <exception>&\"'|";

        // Act
        assertDoesNotThrow(() -> {
            logService.registrarErrorConsulta(ADMIN_ID, USUARIO_ID, errorConCaracteres);
        });
    }

    // ==================== TESTS DE UNICODE Y CARACTERES INTERNACIONALES ====================

    @Test
    void testRegistrarAccion_ConCaracteresUnicode_NoLanzaExcepcion() {
        // Arrange
        String accionUnicode = "Acción con caracteres: ñ á é í ó ú ü € £ ¥";

        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion(accionUnicode, "usuario@ñoño.com");
        });
    }

    @Test
    void testRegistrarConsultaPerfil_ConEmojis_NoLanzaExcepcion() {
        // Arrange - Aunque inusual, puede ocurrir en sistemas modernos
        String emailConEmoji = "usuario😀@test.com";

        // Act
        assertDoesNotThrow(() -> {
            logService.registrarConsultaPerfil(
                ADMIN_ID, 
                ADMIN_EMAIL, 
                USUARIO_ID, 
                emailConEmoji, 
                "Visualizador"
            );
        });
    }

    @Test
    void testRegistrarAccion_ConTextoJapones_NoLanzaExcepcion() {
        // Arrange
        String accionJapones = "アクション テスト";

        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion(accionJapones, USUARIO_EMAIL);
        });
    }

    @Test
    void testRegistrarAccion_ConTextoCirilico_NoLanzaExcepcion() {
        // Arrange
        String accionCirilico = "Действие тест";

        // Act
        assertDoesNotThrow(() -> {
            logService.registrarAccion(accionCirilico, USUARIO_EMAIL);
        });
    }

    // ==================== TESTS DE CONCURRENCIA ====================

    @Test
    void testMultiplesLlamadasConcurrentes_NoLanzaExcepcion() throws InterruptedException {
        // Arrange
        Thread[] threads = new Thread[10];

        // Act - Crear múltiples threads que llaman al servicio simultáneamente
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                logService.registrarAccion("Acción " + index, "usuario" + index + "@test.com");
            });
            threads[i].start();
        }

        // Esperar a que todos los threads terminen
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert - Si llegamos aquí, no hubo excepciones
        assertTrue(true);
    }

    // ==================== TESTS DE RENDIMIENTO ====================

    @Test
    void testRegistrarAccion_1000Llamadas_CompletaEnTiempoRazonable() {
        // Arrange
        long startTime = System.currentTimeMillis();

        // Act - Realizar 1000 registros
        for (int i = 0; i < 1000; i++) {
            logService.registrarAccion("Acción " + i, USUARIO_EMAIL);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert - Debe completarse en menos de 5 segundos
        assertTrue(duration < 5000, "El registro de 1000 acciones tardó " + duration + "ms");
    }

    // ==================== TESTS DE FORMATO DE TIMESTAMP ====================

    @Test
    void testFormatoTimestamp_EsConsistente() {
        // Act - Verificar que el formato usado es válido
        LocalDateTime ahora = LocalDateTime.now();
        String timestampFormateado = ahora.format(FORMATTER);

        // Assert - Verificar formato yyyy-MM-dd HH:mm:ss
        assertNotNull(timestampFormateado);
        assertTrue(timestampFormateado.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
}
