package iso25.g05.esi_media.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Valoracion;

@Repository
public interface ValoracionRepository extends MongoRepository<Valoracion, String> {
    Optional<Valoracion> findByVisualizadorIdAndContenidoId(String visualizadorId, String contenidoId);
}
