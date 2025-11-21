package iso25.g05.esi_media.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Valoracion;

@Repository
public interface ValoracionRepository extends MongoRepository<Valoracion, String> {
    Optional<Valoracion> findByVisualizadorIdAndContenidoId(String visualizadorId, String contenidoId);
    List<Valoracion> findByContenidoId(String contenidoId);

    /**
     * Elimina todas las valoraciones realizadas por un visualizador concreto.
     * Se usa al eliminar definitivamente la cuenta del visualizador.
     *
     * @param visualizadorId ID del visualizador
     */
    void deleteByVisualizadorId(String visualizadorId);
}
