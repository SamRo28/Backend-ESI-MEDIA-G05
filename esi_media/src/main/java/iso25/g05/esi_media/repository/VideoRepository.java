package iso25.g05.esi_media.repository;

<<<<<<< HEAD
=======
import iso25.g05.esi_media.model.Video;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
>>>>>>> alvaro
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Video;

@Repository
public interface VideoRepository extends MongoRepository<Video, String> {
<<<<<<< HEAD
    @Query("{ '_resolucion' : ?0 }")
=======
    @Query("{'_resolucion': ?0}")
>>>>>>> alvaro
    List<Video> findByResolucion(String resolucion);
}