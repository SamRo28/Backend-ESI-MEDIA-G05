package iso25.g05.esi_media.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.IpLoginAttempt;

/**
 * Repositorio para la gestión de intentos de login y bloqueo de IPs.
 */
@Repository
public interface IpLoginAttemptRepository extends MongoRepository<IpLoginAttempt, String> {
    // findById(ipAddress) es suficiente para nuestras necesidades
}