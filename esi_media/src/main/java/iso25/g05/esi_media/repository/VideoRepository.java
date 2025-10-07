package iso25.g05.esi_media.repository;

import iso25.g05.esi_media.model.Video;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VideoRepository extends MongoRepository<Video, String> {
    List<Video> findBy_resolucion(String resolucion);
}