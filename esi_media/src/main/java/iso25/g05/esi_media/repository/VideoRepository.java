package iso25.g05.esi_media.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Video;

@Repository
public interface VideoRepository extends MongoRepository<Video, String> {
    @Query("{ '_resolucion' : ?0 }")
    List<Video> findByResolucion(String resolucion);
}