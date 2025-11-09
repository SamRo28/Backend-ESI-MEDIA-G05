package iso25.g05.esi_media.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento para rastrear intentos de inicio de sesión fallidos por IP
 * y gestionar el bloqueo progresivo (protección contra fuerza bruta).
 */
@Document(collection = "ip_login_attempts")
public class IpLoginAttempt {

    @Id
    private String ipAddress; // La dirección IP será el ID único

    private int failedAttempts;
    private Date lastAttempt;
    private boolean blocked;
    private Date blockedUntil; // null si el bloqueo es permanente
    
    /**
     * Nivel de bloqueo. Cada vez que se bloquea, este número sube.
     * 0 = Sin bloqueos
     * 1 = Bloqueado 15 segundos
     * 2 = Bloqueado 1 minuto
     * 3 = Bloqueado 15 minutos
     * 4+ = Bloqueado permanentemente
     */
    private int blockCount; 

    public static final int MAX_FAILED_ATTEMPTS = 5; // Intentos antes de CADA bloqueo

    public IpLoginAttempt() {
        this.failedAttempts = 0;
        this.blocked = false;
        this.blockCount = 0;
        this.lastAttempt = new Date();
    }

    public IpLoginAttempt(String ipAddress) {
        this();
        this.ipAddress = ipAddress;
    }

    // --- Getters y Setters ---
    // (Asegúrate de tener getters y setters para todos los campos)
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
    public Date getLastAttempt() { return lastAttempt; }
    public void setLastAttempt(Date lastAttempt) { this.lastAttempt = lastAttempt; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public Date getBlockedUntil() { return blockedUntil; }
    public void setBlockedUntil(Date blockedUntil) { this.blockedUntil = blockedUntil; }
    public int getBlockCount() { return blockCount; }
    public void setBlockCount(int blockCount) { this.blockCount = blockCount; }

    // --- Métodos de Lógica ---

    /**
     * Incrementa el contador de intentos fallidos.
     */
    public void incrementFailedAttempts() {
        this.failedAttempts++;
        this.lastAttempt = new Date();
    }

    /**
     * Resetea el contador de intentos y el historial de bloqueo.
     * Se llama SÓLO en un login exitoso.
     */
    public void resetAllOnSuccess() {
        this.failedAttempts = 0;
        this.lastAttempt = new Date();
        this.blocked = false;
        this.blockedUntil = null;
        this.blockCount = 0; // El login exitoso "perdona" los bloqueos pasados
    }

    /**
     * Comprueba si se ha alcanzado el umbral de intentos y aplica el bloqueo progresivo.
     * @return true si se acaba de activar un bloqueo, false si no.
     */
    public boolean checkAndApplyProgressiveBlock() {
        if (this.failedAttempts < MAX_FAILED_ATTEMPTS) {
            return false; // Aún no hay suficientes intentos
        }
        
        // Se ha alcanzado el umbral
        this.blocked = true;
        this.blockCount++; // Escalar al siguiente nivel de bloqueo
        this.failedAttempts = 0; // Resetear intentos para el próximo ciclo
        
        long now = System.currentTimeMillis();
        
        switch (this.blockCount) {
            case 1:
                // Bloqueo 1: 15 segundos
                this.blockedUntil = new Date(now + (15 * 1000));
                break;
            case 2:
                // Bloqueo 2: 1 minuto
                this.blockedUntil = new Date(now + (1 * 60 * 1000));
                break;
            case 3:
                // Bloqueo 3: 15 minutos
                this.blockedUntil = new Date(now + (15 * 60 * 1000));
                break;
            default:
                // Bloqueo 4+: Permanente
                this.blockedUntil = null; // null significa permanente
                break;
        }
        
        return true;
    }

    /**
     * Comprueba si el bloqueo actual sigue activo.
     * Si el tiempo de bloqueo ha pasado, resetea el estado de bloqueo
     * (pero MANTIENE el blockCount para la escalada futura).
     * @return true si la IP sigue bloqueada, false si ya no lo está.
     */
    public boolean isCurrentlyBlocked() {
        if (!this.blocked) {
            return false; // No está bloqueada
        }

        if (this.blockedUntil == null) {
            return true; // Bloqueo permanente
        }

        // Es un bloqueo temporal, comprobar si ha expirado
        if (new Date().after(this.blockedUntil)) {
            // El tiempo de bloqueo ha expirado
            this.blocked = false;
            this.blockedUntil = null;
            this.failedAttempts = 0; // Resetear contador de intentos
            // IMPORTANTE: No reseteamos blockCount.
            return false; // Ya no está bloqueado
        } else {
            // Sigue bloqueado
            return true;
        }
    }
}