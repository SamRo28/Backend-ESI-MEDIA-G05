package iso25.g05.esi_media.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Lista;
import iso25.g05.esi_media.model.Usuario;

@Repository
public interface ListaRepository extends MongoRepository<Lista, String> {
    
    @Query("{'usuario.$id': ?0}")
    List<Lista> findByUsuarioId(String usuarioId);
    
    List<Lista> findByUsuario(Usuario usuario);
    
    /**
     * Busca todas las listas creadas por un usuario específico
     * 
     * @param creadorId ID del creador/usuario
     * @return Lista de listas del creador
     */
    List<Lista> findByCreadorId(String creadorId);
    
    /**
     * Busca una lista específica por ID y creador
     * Útil para verificar permisos
     * 
     * @param id ID de la lista
     * @param creadorId ID del creador/usuario
     * @return Optional con la lista si existe y pertenece al creador
     */
    java.util.Optional<Lista> findByIdAndCreadorId(String id, String creadorId);
    
    /**
     * Busca una lista pública (visible) por creador y nombre
     * Para validar unicidad de nombres en listas públicas del mismo gestor
     * 
     * @param creadorId ID del creador/usuario
     * @param nombre Nombre de la lista
     * @return Optional con la lista si existe una lista pública con ese nombre del mismo creador
     */
    java.util.Optional<Lista> findByCreadorIdAndNombreAndVisibleIsTrue(String creadorId, String nombre);
    
}
