package iso25.g05.esi_media.service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;

import iso25.g05.esi_media.dto.CrearAdministradorRequest;
import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.Codigorecuperacion;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.AdministradorRepository;
import iso25.g05.esi_media.repository.CodigoRecuperacionRepository;
import iso25.g05.esi_media.repository.ContraseniaRepository;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.repository.VisualizadorRepository;

@Service
public class UserService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CodigoRecuperacionRepository codigorecuperacionRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ContraseniaRepository contraseniaRepository;

    @Autowired
    private VisualizadorRepository visualizadorRepository;

    @Autowired
    private GestorDeContenidoRepository gestorDeContenidoRepository;

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public Usuario login(Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        Optional<Usuario> existingUser = this.usuarioRepository.findByEmail(email);

        if (existingUser.isPresent() && existingUser.get().getContrasenia() != null
                && existingUser.get().getContrasenia().getContraseniaActual().equals(password)) {
            if (!existingUser.get().isTwoFactorAutenticationEnabled()) {
                generateAndSaveToken(existingUser.get());
                return existingUser.get();
            } else {
                return existingUser.get();
            }
        }
        return null;
    }

    private Token generateAndSaveToken(Usuario user) {
        Token token = new Token();
        user.sesionstoken.add(token);
        this.usuarioRepository.save(user);
        return token;
    }

    public String login3Auth(Map<String, String> loginData) {
        String email = loginData.get("email");
        Optional<Usuario> existingUser = this.usuarioRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            Codigorecuperacion cr = emailService.send3FAemail(email, existingUser.get());
            return cr != null ? cr.getId() : null;
        }
        return null;
    }

    public Token confirmLogin3Auth(Map<String, String> loginData) {
        String codigoRecuperacionId = loginData.get("id");
        String code = loginData.get("code");
        Optional<Codigorecuperacion> existingCode = this.codigorecuperacionRepository.findById(codigoRecuperacionId);
        if (existingCode.isPresent() && existingCode.get().getcodigo().equals(code)) {
            Usuario user = existingCode.get().getunnamedUsuario();

            if (!user.isThreeFactorAutenticationEnabled()) {
               user.setThreeFactorAutenticationEnabled(true);
            }

            return generateAndSaveToken(user);
        }
        return null;
    }

    public Administrador crearAdministrador(CrearAdministradorRequest request, String adminActual) {
        verificarPermisosCreacion(adminActual);
        verificarEmailUnico(request.getEmail());

        Contrasenia contrasenia = new Contrasenia(
            null,
            null,
            request.getContrasenia(),
            new java.util.ArrayList<>()
        );

        Administrador nuevoAdmin = new Administrador(
            request.getApellidos(),
            false,
            contrasenia,
            request.getEmail(),
            request.getFoto(),
            request.getNombre(),
            request.getDepartamento()
        );

        return administradorRepository.save(nuevoAdmin);
    }

    private void verificarPermisosCreacion(String adminId) {
        Optional<Administrador> adminActual = administradorRepository.findById(adminId);
        if (adminActual.isEmpty()) {
            throw new RuntimeException("Administrador no encontrado");
        }
    }

    private void verificarEmailUnico(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya existe en el sistema");
        }
        if (administradorRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya existe en el sistema");
        }
    }

    public Usuario confirm2faCode(Map<String, String> data) {
        int code = Integer.parseInt(data.get("code"));
        String email = data.get("email");
        Optional<Usuario> existingUser = this.usuarioRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            String secret = existingUser.get().getSecretkey();
            boolean valid = gAuth.authorize(secret, code);
            if (existingUser.get().isThreeFactorAutenticationEnabled() && valid) {
                generateAndSaveToken(existingUser.get());
            }
            return valid ? existingUser.get() : null;
        }
        return null;
    }

    /**
     * Eliminación idempotente de la contraseña por ID
     */
    public void deletePassword(String contraseniaId) {
        try {
            contraseniaRepository.deleteById(contraseniaId);
        } catch (Exception ignored) {
        }
    }

    /*
    public Usuario updateUser(String id, String tipo, Map<String,Object> u){
        ObjectMapper mapper = new ObjectMapper();

        if(tipo.equals("Administrador")){
            Optional<Administrador> adminOpt = administradorRepository.findById(id);
            if(adminOpt.isPresent()){
                Administrador admin = adminOpt.get();
                Administrador administradorUpdated = mapper.convertValue(u, Administrador.class);
                
                administradorUpdated.setId(admin.getId());
                administradorUpdated.setContrasenia(admin.getContrasenia());
                return administradorRepository.save(administradorUpdated);
            }
        } else if(tipo.equals("Visualizador")){
            Optional<Visualizador> visualizadorOpt = visualizadorRepository.findById(id);
            if(visualizadorOpt.isPresent()){
                Visualizador visualizador = visualizadorOpt.get();
                Visualizador visualizadorUpdated = mapper.convertValue(u, Visualizador.class);
                visualizadorUpdated.setId(visualizador.getId());
                visualizadorUpdated.setContrasenia(visualizador.getContrasenia());
                return visualizadorRepository.save(visualizadorUpdated);
            }
        }
                
        else {
            Optional<GestordeContenido> gestorOpt = gestorDeContenidoRepository.findById(id);
            if(gestorOpt.isPresent()){
                GestordeContenido gestor = gestorOpt.get();
                GestordeContenido gestorUpdated = mapper.convertValue(u, GestordeContenido.class);
                gestorUpdated.setId(gestor.getId());
                gestorUpdated.setContrasenia(gestor.getContrasenia());
                return gestorDeContenidoRepository.save(gestorUpdated);
            }
        }
        return null;


    }*/

    public Usuario updateUser(String id, String tipo, Map<String,Object> u) throws IOException{
        ObjectMapper mapper = new ObjectMapper();

        // Convert the incoming Map into a JsonNode so we can apply it to existing objects
        JsonNode updateNode = mapper.valueToTree(u);

        if(tipo.equals("Administrador")){
            Optional<Administrador> adminOpt = administradorRepository.findById(id);
            if(adminOpt.isPresent()){
                Administrador admin = adminOpt.get();
                // Merge fields from updateNode into the existing admin
                mapper.readerForUpdating(admin).readValue(updateNode);
                return administradorRepository.save(admin);
            }
        } else if(tipo.equals("Visualizador")){
            Optional<Visualizador> visualizadorOpt = visualizadorRepository.findById(id);
            if(visualizadorOpt.isPresent()){
                Visualizador visualizador = visualizadorOpt.get();
                mapper.readerForUpdating(visualizador).readValue(updateNode);
                return visualizadorRepository.save(visualizador);
            }
        } else {
            Optional<GestordeContenido> gestorOpt = gestorDeContenidoRepository.findById(id);
            if(gestorOpt.isPresent()){
                GestordeContenido gestor = gestorOpt.get();
                mapper.readerForUpdating(gestor).readValue(updateNode);
                return gestorDeContenidoRepository.save(gestor);
            }
        }
        return null;
    }
    
}

