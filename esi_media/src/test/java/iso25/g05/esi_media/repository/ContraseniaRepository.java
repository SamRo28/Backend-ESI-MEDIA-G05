package iso25.g05.esi_media.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Contrasenia;

/**
 * Repositorio para la gestión de contraseñas.
 * Este repositorio se usa específicamente en pruebas para asegurar
 * que las contraseñas se guarden en la base de datos de pruebas.
 */
@Repository
public interface ContraseniaRepository extends MongoRepository<Contrasenia, String> {
    
}