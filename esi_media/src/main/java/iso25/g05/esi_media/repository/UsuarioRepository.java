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
    @Query("{'email': ?0}")
    Optional<Usuario> findByEmail(String email);
    
    @Query(value = "{'email': ?0}", exists = true)
    boolean existsByEmail(String email);
}