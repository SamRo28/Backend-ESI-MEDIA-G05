package iso25.g05.esi_media.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Audio;

@Repository
public interface AudioRepository extends MongoRepository<Audio, String> {
    @Query("{ '_n_visualizaciones' : ?0 }")
    List<Audio> findByVisualizaciones(int n); // Buscar por número de visualizaciones
}
