package iso25.g05.esi_media.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.TokenRepository;

@Service
public class TokenForValoracionService {

    private final TokenRepository tokenRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public TokenForValoracionService(TokenRepository tokenRepository, MongoTemplate mongoTemplate) {
        this.tokenRepository = tokenRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Resuelve el id del usuario a partir del header Authorization.
     * Acepta tanto 'Bearer <token>' como el token en crudo.
     * Retorna null si no existe o no puede resolverse.
     */
    public String resolveUsuarioIdFromAuth(String authHeader) {
        String token = extractToken(authHeader);
        if (token == null) return null;

        // Intentamos resolver el token en la colección `tokens`
        Optional<Token> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isPresent()) {
            String uid = getUserIdFromToken(tokenOpt.get());
            if (uid != null) return uid;
        }

        // Fallback: buscar token en el campo embebido `sesionstoken` dentro de users
        return findUserIdInUsersBySessionToken(token);
    }

    /** Extrae el token desde el header Authorization. Acepta 'Bearer <token>' o token crudo. */
    private String extractToken(String authHeader) {
        if (authHeader == null) return null;
        if (authHeader.startsWith("Bearer ")) return authHeader.substring(7);
        return authHeader;
    }

    /**
     * Valida un objeto Token y devuelve el usuario asociado (id) si válido.
     * Devuelve null si el token está expirado o no tiene usuario asociado.
     */
    private String getUserIdFromToken(Token t) {
        if (t == null) return null;
        if (isTokenExpired(t)) return null;
        if (t.getUsuario() == null) return null;
        return t.getUsuario().getId();
    }

    /**
     * Busca en la colección `users` un documento cuya `sesionstoken.token` coincida.
     * Si se encuentra, valida la sesión embebida y devuelve el id del usuario.
     */
    private String findUserIdInUsersBySessionToken(String token) {
        try {
            Query q = new Query(Criteria.where("sesionstoken.token").is(token));
            Usuario u = mongoTemplate.findOne(q, Usuario.class, "users");
            if (u == null) return null;

            if (u.getSesionstoken() != null) {
                Token st = u.getSesionstoken();
                if (isTokenExpired(st)) return null;
            }

            return u.getId();
        } catch (Exception ex) {
            // noop - si falla el fallback, devolvemos null y el controlador responderá 401
            return null;
        }
    }

    /** Comprueba expirada lógica común para Token */
    private boolean isTokenExpired(Token t) {
        if (t == null) return true;
        try {
            if (t.isExpirado()) return true;
            if (t.getFechaExpiracion() != null && t.getFechaExpiracion().before(new java.util.Date())) return true;
        } catch (Exception ignore) {
            // Ignorar excepciones al comprobar la fecha de expiración
        }
        return false;
    }
}
