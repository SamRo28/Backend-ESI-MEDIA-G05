package iso25.g05.esi_media.services;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.UsuarioRepository;

@Service
public class UserService {
    

    @Autowired
    private UsuarioRepository usuarioRepository;


     public Usuario login(Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        Optional<Usuario> existingUser = this.usuarioRepository.findByEmail(email);

        if (existingUser.isPresent() && existingUser.get().getContrasenia().getContraseniaActual().equals(password) ) {
        	if (!existingUser.get().isTwoFactorAutenticationEnabled()) {
                generateAndSaveToken(existingUser.get());
        	    return existingUser.get();
            }
            else{
                return existingUser.get();
            }
            
        }
        return null;
    }

    private void generateAndSaveToken(Usuario user) {
        Token token = new Token();
        user.sesionstoken.add(token);
        this.usuarioRepository.save(user);
    }

    public Usuario login3Auth(Map<String, String> loginData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
