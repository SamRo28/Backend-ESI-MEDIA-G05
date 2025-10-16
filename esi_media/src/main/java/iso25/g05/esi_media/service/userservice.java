package iso25.g05.esi_media.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.dto.CrearAdministradorRequest;
import iso25.g05.esi_media.repository.AdministradorRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;

/**
 * Servicio unificado para gestión de usuarios (login, administradores, etc.)
 * Combina funcionalidades de UserService y userservice
 */
@Service
public class UserService {
    
    @Autowired
    private AdministradorRepository administradorRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private EmailService emailService;

    // ============================================
    // FUNCIONALIDADES DE LOGIN Y AUTENTICACIÓN
    // ============================================
    
    /**
     * Login normal con email y contraseña
     * Si el usuario NO tiene 2FA habilitado, genera un token de sesión
     * @param loginData Map con email y password
     * @return Usuario si las credenciales son correctas, null en caso contrario
     */
    public Usuario login(Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        System.out.println("🔐 Intento de login - Email: " + email);
        
        Optional<Usuario> existingUser = this.usuarioRepository.findByEmail(email);
        
        if (existingUser.isEmpty()) {
            System.out.println("❌ Usuario no encontrado con email: " + email);
            return null;
        }
        
        Usuario user = existingUser.get();
        System.out.println("✅ Usuario encontrado: " + user.getNombre() + " " + user.getApellidos());
        System.out.println("📋 Tipo de usuario (_class): " + user.getClass().getName());
        
        if (user.getContrasenia() == null) {
            System.out.println("⚠️ ADVERTENCIA: El usuario no tiene contraseña configurada");
            return null;
        }
        
        String storedPassword = user.getContrasenia().getContraseniaActual();
        System.out.println("🔑 Contraseña almacenada: " + storedPassword);
        System.out.println("🔑 Contraseña recibida: " + password);
        System.out.println("🔍 ¿Contraseñas coinciden? " + storedPassword.equals(password));

        if (storedPassword.equals(password)) {
            System.out.println("✅ Credenciales correctas!");
            if (!user.isTwoFactorAutenticationEnabled()) {
                System.out.println("🎫 Generando token de sesión (2FA deshabilitado)");
                generateAndSaveToken(user);
                return user;
            } else {
                System.out.println("🔐 2FA habilitado - se requiere segundo factor");
                return user;
            }
        }
        
        System.out.println("❌ Contraseña incorrecta");
        return null;
    }

    /**
     * Genera un token de sesión y lo guarda en el usuario
     */
    private void generateAndSaveToken(Usuario user) {
        Token token = new Token();
        user.sesionstoken.add(token);
        
        System.out.println("🎫 Token generado: " + token.getToken());
        System.out.println("📅 Fecha de expiración: " + token.getFechaExpiracion());
        System.out.println("💾 Guardando usuario con token en MongoDB...");
        
        this.usuarioRepository.save(user);
        
        System.out.println("✅ Usuario guardado con token de sesión");
    }

    /**
     * Login con autenticación de 3 factores
     * Envía un email con el código de verificación
     */
    public void login3Auth(Map<String, String> loginData) {
        String email = loginData.get("email");
        Optional<Usuario> existingUser = this.usuarioRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            emailService.send3FAemail(email, existingUser.get());
        }
    }

    // ============================================
    // FUNCIONALIDADES DE CREACIÓN DE ADMINISTRADORES
    // ============================================
    
    /**
     * Crea un nuevo administrador
     * @param request Datos del administrador a crear
     * @param adminActual ID del administrador que realiza la operación
     * @return El administrador creado
     * @throws RuntimeException Si no tiene permisos o el email ya existe
     */
    public Administrador crearAdministrador(CrearAdministradorRequest request, String adminActual) {
        // 1. Verificar permisos del administrador actual
        verificarPermisosCreacion(adminActual);
        
        // 2. Verificar que el email sea único
        verificarEmailUnico(request.getEmail());
        
        // 3. Crear el nuevo administrador  
        Contrasenia contrasenia = new Contrasenia(
            null, // id
            null, // fecha_expiracion
            request.getContrasenia(), // contrasenia_actual
            new java.util.ArrayList<>() // contrasenias_usadas vacio
        );
        
        Administrador nuevoAdmin = new Administrador(
            request.getApellidos(),
            false, // No bloqueado por defecto
            contrasenia,
            request.getEmail(),
            request.getFoto(),
            request.getNombre(),
            request.getDepartamento()
        );
        
        // 4. Guardar el administrador
        return administradorRepository.save(nuevoAdmin);
    }
    
    /**
     * Verifica si el administrador actual tiene permisos para crear otros administradores
     */
    private void verificarPermisosCreacion(String adminId) {
        Optional<Administrador> adminActual = administradorRepository.findById(adminId);
        
        if (adminActual.isEmpty()) {
            throw new RuntimeException("Administrador no encontrado");
        }
    }
    
    /**
     * Verifica que el email sea único en el sistema
     */
    private void verificarEmailUnico(String email) {
        // Verificar en usuarios
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya existe en el sistema");
        }
        
        // Verificar en administradores
        if (administradorRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya existe en el sistema");
        }
    }
}
