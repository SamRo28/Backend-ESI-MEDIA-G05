package iso25.g05.esi_media.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import iso25.g05.esi_media.model.Visualizador;
import java.util.Optional;
import java.util.List;
import java.util.Date;

/**
 * Repositorio específico para operaciones de Visualizador.
 * 
 * HERENCIA INTELIGENTE:
 * - Extiende MongoRepository<Visualizador, String>
 * - MongoDB automáticamente filtra solo documentos de tipo Visualizador
 * - Los métodos heredados (save, findById, etc.) funcionan automáticamente
 * - Métodos personalizados se enfocan en características específicas del Visualizador
 * 
 * USO RECOMENDADO:
 * - Para operaciones específicas de visualizadores (buscar VIPs, por edad, etc.)
 * - Para lógica de negocio que solo aplica a visualizadores
 * - Usar UsuarioRepository para operaciones generales (buscar por email, etc.)
 */
@Repository
public interface VisualizadorRepository extends MongoRepository<Visualizador, String> {
    
    /**
     * Busca un visualizador específico por email
     * 
     * IMPORTANTE: Aunque UsuarioRepository.findBy_email() puede encontrar cualquier usuario,
     * este método garantiza que el resultado sea específicamente un Visualizador
     * 
     * @param email Email del visualizador
     * @return Optional<Visualizador> (no Optional<Usuario>)
     */
    @Query("{'email': ?0}")
    Optional<Visualizador> findBy_email(String email);
    
    /**
     * Busca todos los visualizadores VIP
     * Útil para campañas de marketing, funciones premium, etc.
     * 
     * @param vip true para buscar VIPs, false para usuarios regulares
     * @return Lista de visualizadores con el estado VIP especificado
     */
    @Query("{'vip': ?0}")
    List<Visualizador> findBy_vip(boolean vip);
    
    /**
     * Busca visualizadores por alias (búsqueda parcial, case-insensitive)
     * Útil para búsqueda de usuarios públicos, menciones, etc.
     * 
     * @param alias Parte del alias a buscar
     * @return Lista de visualizadores que coinciden
     */
    @Query("{'alias': {$regex: ?0, $options: 'i'}}")
    List<Visualizador> findByAliasContainingIgnoreCase(String alias);
    
    /**
     * Busca visualizadores nacidos después de cierta fecha
     * Útil para restricciones de edad, contenido apropiado por edad, etc.
     * 
     * @param fecha Fecha mínima de nacimiento
     * @return Lista de visualizadores nacidos después de la fecha
     */
    @Query("{'fecha_nac': {$gt: ?0}}")
    List<Visualizador> findBy_fecha_nacAfter(Date fecha);
    
    /**
     * Busca visualizadores nacidos antes de cierta fecha
     * Útil para contenido dirigido a ciertos grupos de edad
     * 
     * @param fecha Fecha máxima de nacimiento
     * @return Lista de visualizadores nacidos antes de la fecha
     */
    @Query("{'fecha_nac': {$lt: ?0}}")
    List<Visualizador> findBy_fecha_nacBefore(Date fecha);
    
    /**
     * Cuenta visualizadores VIP
     * Útil para estadísticas de negocio y reportes
     * 
     * @return Número de visualizadores VIP en el sistema
     */
    @Query(value="{'vip': ?0}", count=true)
    long countBy_vip(boolean vip);
}
