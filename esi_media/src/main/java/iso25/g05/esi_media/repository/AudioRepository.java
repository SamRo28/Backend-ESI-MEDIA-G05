package iso25.g05.esi_media.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Audio;

@Repository
public interface AudioRepository extends MongoRepository<Audio, String> {
    List<Audio> findBy__n_visualizaciones(int n); // keep name conservative
}
