package iso25.g05.esi_media.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Token;

@Repository
public interface TokenRepository extends MongoRepository<Token, String> {
    Optional<Token> findBy_token(String token);
}
