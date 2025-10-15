package iso25.g05.esi_media.repository;

import iso25.g05.esi_media.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repositorio base para gestionar todos los tipos de Usuario en MongoDB.
 * 
 * HERENCIA EN MONGODB:
 * - Maneja Usuario, Visualizador, Administrador, Gestor en la misma colección "users"
 * - MongoDB automáticamente filtra por tipo usando el campo "_class"
 * - Permite consultas polimórficas (buscar cualquier tipo de usuario)
 * 
 * VENTAJAS:
 * - Un solo repositorio para operaciones comunes (buscar por email, ID, etc.)
 * - Consultas eficientes con índices compartidos
 * - Mantenimiento simplificado
 */
@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
<<<<<<< HEAD
    
    /**
     * Busca un usuario por email (cualquier tipo: Visualizador, Admin, Gestor)
     * 
     * @param email Email del usuario a buscar
     * @return Optional con el usuario encontrado o vacío si no existe
     */
    Optional<Usuario> findByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el email dado
     * Útil para validaciones de unicidad antes de crear nuevos usuarios
     * 
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca usuarios por nombre (búsqueda parcial, case-insensitive)
     * Útil para funciones de búsqueda y autocompletado
     * 
     * @param nombre Parte del nombre a buscar
     * @return Lista de usuarios que coinciden
     */
    @Query("{'nombre': {$regex: ?0, $options: 'i'}}")
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);
    
    /**
     * Busca usuarios por estado de bloqueo
     * Útil para administración y moderación
     * 
     * @param bloqueado true para usuarios bloqueados, false para activos
     * @return Lista de usuarios con el estado especificado
     */
    List<Usuario> findByBloqueado(boolean bloqueado);
    
    /**
     * Cuenta total de usuarios registrados (todos los tipos)
     * Útil para estadísticas y dashboards administrativos
     * 
     * @return Número total de usuarios en el sistema
     */
    long count();
=======
    @Query("{'email': ?0}")
    Optional<Usuario> findByEmail(String email);
    
    @Query(value = "{'email': ?0}", exists = true)
    boolean existsByEmail(String email);
>>>>>>> alvaro
}