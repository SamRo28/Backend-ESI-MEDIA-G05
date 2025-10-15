package iso25.g05.esi_media.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Token;

@Repository
public interface TokenRepository extends MongoRepository<Token, String> {
<<<<<<< HEAD
=======
    @Query("{'_token': ?0}")
>>>>>>> alvaro
    Optional<Token> findByToken(String token);
}
