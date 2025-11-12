package iso25.g05.esi_media.service;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;

import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.Codigorecuperacion;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.IpLoginAttempt;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.AdministradorRepository;
import iso25.g05.esi_media.repository.CodigoRecuperacionRepository;
import iso25.g05.esi_media.repository.ContraseniaComunRepository;
import iso25.g05.esi_media.repository.ContraseniaRepository;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;
import iso25.g05.esi_media.repository.IpLoginAttemptRepository;
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
    
    @Autowired
    private ContraseniaComunRepository contraseniaComunRepository;

    @Autowired
    private IpLoginAttemptRepository ipLoginAttemptRepository;

    // Constante reutilizable para la clave 'email' en maps/respuestas
    private static final String KEY_EMAIL = "email";

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(10);

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    
    public Usuario login(Map<String, String> loginData, String ipAddress) {
        
        IpLoginAttempt attempt = ipLoginAttemptRepository.findById(ipAddress)
                .orElse(new IpLoginAttempt(ipAddress));

        if (attempt.isCurrentlyBlocked()) {
            String message = "Demasiados intentos fallidos. Su IP está bloqueada.";
            if (attempt.getBlockedUntil() == null) {
                message = "Su IP ha sido bloqueada permanentemente.";
            } else {
                long durationSeconds = (attempt.getBlockedUntil().getTime() - new Date().getTime()) / 1000;
                message = "Su IP está bloqueada. Inténtelo de nuevo en " + Math.max(0, durationSeconds) + " segundos.";
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
        
    String email = loginData.get(KEY_EMAIL);
        String password = loginData.get("password");

        Optional<Usuario> existingUser = this.usuarioRepository.findByEmail(email);

        if (existingUser.isPresent() && existingUser.get().getContrasenia() != null
                && encoder.matches(password,existingUser.get().getContrasenia().getContraseniaActual())) {
            
            attempt.resetAllOnSuccess(); 
            ipLoginAttemptRepository.save(attempt);
            if (!existingUser.get().isTwoFactorAutenticationEnabled()) {
                generateAndSaveToken(existingUser.get());
                return existingUser.get();
            } else {
                return existingUser.get();
            }
        }
        

        attempt.incrementFailedAttempts();
        boolean justBlocked = attempt.checkAndApplyProgressiveBlock(); 
        ipLoginAttemptRepository.save(attempt);
        
        if (justBlocked) {
            // El bloqueo se acaba de activar, lanzamos una excepción específica
            String message;
            if (attempt.getBlockedUntil() == null) {
                message = "Demasiados intentos fallidos. Su IP ha sido bloqueada permanentemente.";
            } else {
                long durationSeconds = (attempt.getBlockedUntil().getTime() - new Date().getTime()) / 1000;
                message = "Demasiados intentos fallidos. Su IP ha sido bloqueada por " + Math.max(0, durationSeconds) + " segundos.";
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
        
        return null;
    }

    private Token generateAndSaveToken(Usuario user) {
        Token token = new Token();
        user.setSesionstoken(token);
        this.usuarioRepository.save(user);
        return token;
    }

    public String login3Auth(Map<String, String> loginData) {
    String email = loginData.get(KEY_EMAIL);
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

    public String confirm2faCode(Map<String, String> data) {
        int code = Integer.parseInt(data.get("code"));
    String email = data.get(KEY_EMAIL);
        Optional<Usuario> existingUser = this.usuarioRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            Usuario user = existingUser.get();
            String secret = user.getSecretkey();
            user.setTwoFactorAutenticationEnabled(true);
            boolean valid = gAuth.authorize(secret, code);
            usuarioRepository.save(user);

            if(valid){
                if (existingUser.get().isThreeFactorAutenticationEnabled()) {
                    return "";
                }
                return generateAndSaveToken(existingUser.get()).getToken();
        }
            
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

    public Contrasenia hashearContrasenia(Contrasenia c){
        
        c.setContraseniaActual(encoder.encode(c.getContraseniaActual()));

        return c;
    }

    private String md5Hex(String input) {
        return encoder.encode(input);
    }
    
    /**
     * Valida que el email no existe en el sistema
     * @param email Email a validar
     * @throws RuntimeException si el email ya está registrado o es inválido
     */
    public void validarEmailUnico(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("El email es obligatorio");
        }

        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado en el sistema");
        }
    }
    
    /**
     * Crea y valida una contraseña
     * @param contraseniaTextoPlano La contraseña en texto plano
     * @return La contraseña hasheada y guardada
     * @throws RuntimeException si la contraseña es común o inválida
     */
    public Contrasenia crearYValidarContrasenia(String contraseniaTextoPlano) {
        if (contraseniaTextoPlano == null || contraseniaTextoPlano.trim().isEmpty()) {
            throw new RuntimeException("La contraseña es obligatoria");
        }
        
        // Calcular fecha de expiración (1 año desde ahora)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, 1);
        java.util.Date fechaExpiracion = cal.getTime();
        
        // Crear objeto contraseña
        Contrasenia contrasenia = new Contrasenia(
            null,
            fechaExpiracion,
            contraseniaTextoPlano,
            new java.util.ArrayList<>()
        );
        
        // Validar que no sea una contraseña común
        if (contraseniaComunRepository.existsById(contrasenia.getContraseniaActual())) {
            throw new RuntimeException("La contraseña proporcionada está en la lista de contraseñas comunes");
        }

        // Hashear la contraseña
        contrasenia = hashearContrasenia(contrasenia);
        contrasenia.getContraseniasUsadas().add(contrasenia.getContraseniaActual());
        
        
        
        // Guardar la contraseña en la base de datos
        contrasenia = contraseniaRepository.save(contrasenia);
        
        return contrasenia;
    }

    //  ----------------------------------MÉTODOS DE CAMBIAR CONTRASEÑA------------------------------------------
    public boolean cambiarContrasenia(String email, String contraseniaNueva){
        boolean res = false;
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);

        if(userOpt.isPresent()){
            Usuario user = userOpt.get();


            if (contraseniaComunRepository.existsById(contraseniaNueva)) {
                throw new RuntimeException("La contraseña proporcionada está en la lista de contraseñas comunes");
            }

            Contrasenia c = user.getContrasenia();
            // Actualizar objeto Contrasenia en memoria
            Contrasenia actualizado = comprobarContraseniasAntiguas(c, contraseniaNueva);
            user.setContrasenia(actualizado);
            // Persistir cambios (DBRef + documento de contraseñas)
            contraseniaRepository.save(actualizado);
            usuarioRepository.save(user);
            res = true;

        }
        return res;
    }

    public Contrasenia comprobarContraseniasAntiguas(Contrasenia c, String contraseniaNueva){
        List<String> listaActual = new ArrayList<String>();
        List<String> listaNueva = new ArrayList<String>();

        listaActual = c.getContraseniasUsadas();
        for(int i = 0; i<listaActual.size();i++){
            
            if(encoder.matches(contraseniaNueva, listaActual.get(i))){
                throw new RuntimeException("La contraseña proporcionada ya ha sido usada");
            }
        }
              
        if(listaActual.size()==5){
            for(int i = 1; i<5; i++){                    
                listaNueva.add(listaActual.get(i));     
            }
        }
        else{
            listaNueva.addAll(listaActual);
        }
        

        listaNueva.add(contraseniaNueva);
        c.setContraseniasUsadas(listaNueva);
        c.setContraseniaActual(contraseniaNueva);
        return c;
    }


    public Usuario login(Map<String, String> loginData) {
        // Llamada simple sin lógica de IP
        String email = loginData.get(KEY_EMAIL);
        String password = md5Hex(loginData.get("password"));

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
    
}

