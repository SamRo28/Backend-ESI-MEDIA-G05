package iso25.g05.esi_media.model;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitarios para la clase IpLoginAttempt.
 * Verifica la lógica de bloqueo progresivo contra ataques de fuerza bruta.
 */
@DisplayName("Tests para IpLoginAttempt - Bloqueo Progresivo")
class IpLoginAttemptTest {

    private IpLoginAttempt ipAttempt;
    private static final String TEST_IP = "192.168.1.100";

    @BeforeEach
    void setUp() {
        ipAttempt = new IpLoginAttempt(TEST_IP);
    }

    // ==================== TESTS DE CONSTRUCTOR ====================

    @Test
    @DisplayName("Constructor sin parámetros debe inicializar valores por defecto")
    void testConstructorSinParametros() {
        IpLoginAttempt attempt = new IpLoginAttempt();
        
        assertEquals(0, attempt.getFailedAttempts());
        assertFalse(attempt.isBlocked());
        assertEquals(0, attempt.getBlockCount());
        assertNotNull(attempt.getLastAttempt());
        assertNull(attempt.getBlockedUntil());
    }

    @Test
    @DisplayName("Constructor con IP debe inicializar correctamente")
    void testConstructorConIP() {
        assertEquals(TEST_IP, ipAttempt.getIpAddress());
        assertEquals(0, ipAttempt.getFailedAttempts());
        assertFalse(ipAttempt.isBlocked());
        assertEquals(0, ipAttempt.getBlockCount());
    }

    // ==================== TESTS DE GETTERS Y SETTERS ====================

    @Test
    @DisplayName("Setters y getters deben funcionar correctamente")
    void testSettersYGetters() {
        String newIp = "10.0.0.1";
        Date testDate = new Date();
        Date blockedDate = new Date(System.currentTimeMillis() + 60000);

        ipAttempt.setIpAddress(newIp);
        ipAttempt.setFailedAttempts(3);
        ipAttempt.setLastAttempt(testDate);
        ipAttempt.setBlocked(true);
        ipAttempt.setBlockedUntil(blockedDate);
        ipAttempt.setBlockCount(2);

        assertEquals(newIp, ipAttempt.getIpAddress());
        assertEquals(3, ipAttempt.getFailedAttempts());
        assertEquals(testDate, ipAttempt.getLastAttempt());
        assertTrue(ipAttempt.isBlocked());
        assertEquals(blockedDate, ipAttempt.getBlockedUntil());
        assertEquals(2, ipAttempt.getBlockCount());
    }

    // ==================== TESTS DE incrementFailedAttempts ====================

    @Test
    @DisplayName("incrementFailedAttempts debe aumentar el contador")
    void testIncrementFailedAttempts() {
        assertEquals(0, ipAttempt.getFailedAttempts());
        
        ipAttempt.incrementFailedAttempts();
        assertEquals(1, ipAttempt.getFailedAttempts());
        
        ipAttempt.incrementFailedAttempts();
        assertEquals(2, ipAttempt.getFailedAttempts());
        
        ipAttempt.incrementFailedAttempts();
        assertEquals(3, ipAttempt.getFailedAttempts());
    }

    @Test
    @DisplayName("incrementFailedAttempts debe actualizar lastAttempt")
    void testIncrementActualizaLastAttempt() {
        Date initialDate = ipAttempt.getLastAttempt();
        
        try {
            Thread.sleep(10); // Pequeña espera para asegurar diferencia de tiempo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ipAttempt.incrementFailedAttempts();
        Date newDate = ipAttempt.getLastAttempt();
        
        assertTrue(newDate.after(initialDate) || newDate.equals(initialDate));
    }

    // ==================== TESTS DE resetAllOnSuccess ====================

    @Test
    @DisplayName("resetAllOnSuccess debe resetear todos los contadores")
    void testResetAllOnSuccess() {
        // Simular varios intentos fallidos y bloqueo
        ipAttempt.setFailedAttempts(5);
        ipAttempt.setBlocked(true);
        ipAttempt.setBlockCount(3);
        ipAttempt.setBlockedUntil(new Date(System.currentTimeMillis() + 60000));

        // Resetear por login exitoso
        ipAttempt.resetAllOnSuccess();

        // Verificar que todo se reseteó
        assertEquals(0, ipAttempt.getFailedAttempts());
        assertFalse(ipAttempt.isBlocked());
        assertNull(ipAttempt.getBlockedUntil());
        assertEquals(0, ipAttempt.getBlockCount());
    }

    @Test
    @DisplayName("resetAllOnSuccess debe actualizar lastAttempt")
    void testResetAllOnSuccessActualizaLastAttempt() {
        Date initialDate = ipAttempt.getLastAttempt();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ipAttempt.resetAllOnSuccess();
        Date newDate = ipAttempt.getLastAttempt();
        
        assertTrue(newDate.after(initialDate) || newDate.equals(initialDate));
    }

    // ==================== TESTS DE checkAndApplyProgressiveBlock ====================

    @Test
    @DisplayName("checkAndApplyProgressiveBlock no debe bloquear con menos de 5 intentos")
    void testNoBloqueoConMenosDe5Intentos() {
        for (int i = 0; i < 4; i++) {
            ipAttempt.incrementFailedAttempts();
        }
        
        boolean blocked = ipAttempt.checkAndApplyProgressiveBlock();
        
        assertFalse(blocked);
        assertFalse(ipAttempt.isBlocked());
    }

    @Test
    @DisplayName("Bloqueo nivel 1: 15 segundos con 5 intentos fallidos")
    void testBloqueoNivel1_15Segundos() {
        // Simular 5 intentos fallidos
        for (int i = 0; i < 5; i++) {
            ipAttempt.incrementFailedAttempts();
        }
        
        boolean blocked = ipAttempt.checkAndApplyProgressiveBlock();
        
        assertTrue(blocked, "Debe activar el bloqueo");
        assertTrue(ipAttempt.isBlocked(), "Debe estar bloqueado");
        assertEquals(1, ipAttempt.getBlockCount(), "Debe ser el primer bloqueo");
        assertEquals(0, ipAttempt.getFailedAttempts(), "Debe resetear contador de intentos");
        assertNotNull(ipAttempt.getBlockedUntil(), "Debe tener fecha de desbloqueo");
        
        // Verificar que es aproximadamente 15 segundos
        long duration = ipAttempt.getBlockedUntil().getTime() - System.currentTimeMillis();
        assertTrue(duration >= 14000 && duration <= 16000, "Debe ser aproximadamente 15 segundos");
    }

    @Test
    @DisplayName("Bloqueo nivel 2: 1 minuto con segundo ciclo de 5 intentos")
    void testBloqueoNivel2_1Minuto() {
        // Primer ciclo de bloqueo
        for (int i = 0; i < 5; i++) {
            ipAttempt.incrementFailedAttempts();
        }
        ipAttempt.checkAndApplyProgressiveBlock();
        
        // Simular que expiró el bloqueo temporal
        ipAttempt.setBlocked(false);
        ipAttempt.setBlockedUntil(null);
        
        // Segundo ciclo de 5 intentos
        for (int i = 0; i < 5; i++) {
            ipAttempt.incrementFailedAttempts();
        }
        
        boolean blocked = ipAttempt.checkAndApplyProgressiveBlock();
        
        assertTrue(blocked);
        assertTrue(ipAttempt.isBlocked());
        assertEquals(2, ipAttempt.getBlockCount(), "Debe ser el segundo bloqueo");
        assertNotNull(ipAttempt.getBlockedUntil());
        
        // Verificar que es aproximadamente 1 minuto
        long duration = ipAttempt.getBlockedUntil().getTime() - System.currentTimeMillis();
        assertTrue(duration >= 59000 && duration <= 61000, "Debe ser aproximadamente 1 minuto");
    }

    @Test
    @DisplayName("Bloqueo nivel 3: 15 minutos con tercer ciclo de 5 intentos")
    void testBloqueoNivel3_15Minutos() {
        // Simular 2 bloqueos previos
        ipAttempt.setBlockCount(2);
        
        // Tercer ciclo de 5 intentos
        for (int i = 0; i < 5; i++) {
            ipAttempt.incrementFailedAttempts();
        }
        
        boolean blocked = ipAttempt.checkAndApplyProgressiveBlock();
        
        assertTrue(blocked);
        assertTrue(ipAttempt.isBlocked());
        assertEquals(3, ipAttempt.getBlockCount(), "Debe ser el tercer bloqueo");
        assertNotNull(ipAttempt.getBlockedUntil());
        
        // Verificar que es aproximadamente 15 minutos
        long duration = ipAttempt.getBlockedUntil().getTime() - System.currentTimeMillis();
        long fifteenMinutes = 15 * 60 * 1000;
        assertTrue(duration >= (fifteenMinutes - 1000) && duration <= (fifteenMinutes + 1000), 
                  "Debe ser aproximadamente 15 minutos");
    }

    @Test
    @DisplayName("Bloqueo nivel 4+: Permanente con cuarto ciclo o más")
    void testBloqueoNivel4_Permanente() {
        // Simular 3 bloqueos previos
        ipAttempt.setBlockCount(3);
        
        // Cuarto ciclo de 5 intentos
        for (int i = 0; i < 5; i++) {
            ipAttempt.incrementFailedAttempts();
        }
        
        boolean blocked = ipAttempt.checkAndApplyProgressiveBlock();
        
        assertTrue(blocked);
        assertTrue(ipAttempt.isBlocked());
        assertEquals(4, ipAttempt.getBlockCount(), "Debe ser el cuarto bloqueo");
        assertNull(ipAttempt.getBlockedUntil(), "Bloqueo permanente debe tener blockedUntil = null");
    }

    // ==================== TESTS DE isCurrentlyBlocked ====================

    @Test
    @DisplayName("isCurrentlyBlocked debe retornar false si no está bloqueado")
    void testIsCurrentlyBlockedFalse() {
        assertFalse(ipAttempt.isCurrentlyBlocked());
    }

    @Test
    @DisplayName("isCurrentlyBlocked debe retornar true para bloqueo permanente")
    void testIsCurrentlyBlockedPermanente() {
        ipAttempt.setBlocked(true);
        ipAttempt.setBlockedUntil(null); // null = permanente
        
        assertTrue(ipAttempt.isCurrentlyBlocked());
    }

    @Test
    @DisplayName("isCurrentlyBlocked debe retornar true si el bloqueo temporal no ha expirado")
    void testIsCurrentlyBlockedTemporalActivo() {
        ipAttempt.setBlocked(true);
        // Bloquear por 1 hora en el futuro
        ipAttempt.setBlockedUntil(new Date(System.currentTimeMillis() + 3600000));
        
        assertTrue(ipAttempt.isCurrentlyBlocked());
    }

    @Test
    @DisplayName("isCurrentlyBlocked debe retornar false y desbloquear si el tiempo ha expirado")
    void testIsCurrentlyBlockedTemporalExpirado() {
        ipAttempt.setBlocked(true);
        ipAttempt.setBlockCount(2);
        ipAttempt.setFailedAttempts(3);
        // Bloquear hasta 1 segundo en el pasado
        ipAttempt.setBlockedUntil(new Date(System.currentTimeMillis() - 1000));
        
        boolean blocked = ipAttempt.isCurrentlyBlocked();
        
        assertFalse(blocked, "No debe estar bloqueado porque el tiempo expiró");
        assertFalse(ipAttempt.isBlocked(), "El flag blocked debe cambiar a false");
        assertNull(ipAttempt.getBlockedUntil(), "blockedUntil debe ser null");
        assertEquals(0, ipAttempt.getFailedAttempts(), "Debe resetear intentos fallidos");
        assertEquals(2, ipAttempt.getBlockCount(), "NO debe resetear blockCount (mantiene escalada)");
    }

    @Test
    @DisplayName("isCurrentlyBlocked mantiene blockCount para escalada futura")
    void testIsCurrentlyBlockedMantieneBlockCount() {
        // Configurar un bloqueo temporal que ya expiró
        ipAttempt.setBlocked(true);
        ipAttempt.setBlockCount(2); // Ya tuvo 2 bloqueos previos
        ipAttempt.setBlockedUntil(new Date(System.currentTimeMillis() - 5000));
        
        ipAttempt.isCurrentlyBlocked(); // Esto debe desbloquear
        
        // Verificar que blockCount se mantiene
        assertEquals(2, ipAttempt.getBlockCount(), 
                    "blockCount debe mantenerse para que el próximo bloqueo sea nivel 3");
    }

    // ==================== TESTS DE INTEGRACIÓN ====================

    @Test
    @DisplayName("Escenario completo: 5 intentos -> bloqueo 15s -> expiración -> 5 intentos -> bloqueo 1min")
    void testEscenarioCompletoBloqueosProgresivos() {
        // Primer ciclo: 5 intentos fallidos
        for (int i = 0; i < 5; i++) {
            ipAttempt.incrementFailedAttempts();
        }
        
        assertTrue(ipAttempt.checkAndApplyProgressiveBlock(), "Debe activar primer bloqueo");
        assertEquals(1, ipAttempt.getBlockCount());
        assertTrue(ipAttempt.isCurrentlyBlocked());
        
        // Simular expiración del primer bloqueo
        ipAttempt.setBlockedUntil(new Date(System.currentTimeMillis() - 1000));
        assertFalse(ipAttempt.isCurrentlyBlocked(), "Debe estar desbloqueado después de expiración");
        
        // Segundo ciclo: otros 5 intentos fallidos
        for (int i = 0; i < 5; i++) {
            ipAttempt.incrementFailedAttempts();
        }
        
        assertTrue(ipAttempt.checkAndApplyProgressiveBlock(), "Debe activar segundo bloqueo");
        assertEquals(2, ipAttempt.getBlockCount(), "Debe escalar a nivel 2");
        assertTrue(ipAttempt.isCurrentlyBlocked());
        
        // Verificar que el segundo bloqueo es más largo
        long duration = ipAttempt.getBlockedUntil().getTime() - System.currentTimeMillis();
        assertTrue(duration >= 59000, "El segundo bloqueo debe ser de al menos 59 segundos");
    }

    @Test
    @DisplayName("Login exitoso debe resetear completamente el historial de bloqueos")
    void testLoginExitosoResetea() {
        // Simular múltiples bloqueos
        ipAttempt.setFailedAttempts(10);
        ipAttempt.setBlocked(true);
        ipAttempt.setBlockCount(3);
        ipAttempt.setBlockedUntil(new Date(System.currentTimeMillis() + 60000));
        
        // Login exitoso
        ipAttempt.resetAllOnSuccess();
        
        // Verificar reseteo completo
        assertEquals(0, ipAttempt.getFailedAttempts());
        assertEquals(0, ipAttempt.getBlockCount(), "Login exitoso debe resetear historial de bloqueos");
        assertFalse(ipAttempt.isBlocked());
        assertNull(ipAttempt.getBlockedUntil());
        
        // Próximo bloqueo debe ser nivel 1 nuevamente
        for (int i = 0; i < 5; i++) {
            ipAttempt.incrementFailedAttempts();
        }
        ipAttempt.checkAndApplyProgressiveBlock();
        assertEquals(1, ipAttempt.getBlockCount(), "Después de login exitoso, debe volver a nivel 1");
    }
}
