package iso25.g05.esi_media.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iso25.g05.esi_media.dto.CrearAdministradorRequest;
import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.repository.AdministradorRepository;

@Service
public class AdministradorService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdministradorService.class);
    
    @Autowired
    private AdministradorRepository administradorRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * Crea un administrador sin requerir autenticación (versión simplificada)
     * @param request Datos del administrador a crear
     * @return El administrador creado
     * @throws RuntimeException si hay errores de validación o en el proceso de creación
     */
    public Administrador crearAdministradorSimple(CrearAdministradorRequest request) {
        logger.info("Creando administrador (modo simple): {} {}", request.getNombre(), request.getApellidos());
        
        // Validar que el email no existe
        userService.validarEmailUnico(request.getEmail());
        
        // Crear y validar la contraseña
        Contrasenia contrasenia = userService.crearYValidarContrasenia(request.getContrasenia());
        
        // Crear el objeto Administrador
        Administrador nuevoAdmin = new Administrador(
            request.getApellidos(),
            false, // No está bloqueado inicialmente
            contrasenia,
            request.getEmail(),
            request.getFoto(),
            request.getNombre(),
            request.getDepartamento()
        );
        
        // Guardar y retornar
        Administrador adminGuardado = administradorRepository.save(nuevoAdmin);
        logger.info("Administrador creado exitosamente con ID: {}", adminGuardado.getId());
        
        return adminGuardado;
    }
}
