package iso25.g05.esi_media.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Visualizador;

@Repository
public interface VisualizadorRepository extends MongoRepository<Visualizador, String> {

}
