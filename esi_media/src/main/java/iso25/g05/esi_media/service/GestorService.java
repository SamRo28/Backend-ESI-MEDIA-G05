package iso25.g05.esi_media.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iso25.g05.esi_media.dto.CrearGestorRequest;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;

@Service
public class GestorService {
    
    private static final Logger logger = LoggerFactory.getLogger(GestorService.class);
    
    @Autowired
    private GestorDeContenidoRepository gestorDeContenidoRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * Crea un nuevo gestor de contenido con los datos proporcionados
     * @param request Datos del gestor a crear
     * @return El gestor creado
     * @throws RuntimeException si hay errores de validación o en el proceso de creación
     */
    public GestordeContenido crearGestor(CrearGestorRequest request) {
        logger.info("Creando gestor de contenido: {} {} - Alias: {}", 
                   request.getNombre(), request.getApellidos(), request.getAlias());
        
        // Validar que el email no existe
        userService.validarEmailUnico(request.getEmail());
        
        // Crear y validar la contraseña
        Contrasenia contrasenia = userService.crearYValidarContrasenia(request.getContrasenia());
        
        // Crear el objeto GestordeContenido
        GestordeContenido nuevoGestor = new GestordeContenido(
            request.getApellidos(),
            false, // No está bloqueado inicialmente
            contrasenia,
            request.getEmail(),
            request.getFoto(),
            request.getNombre()
        );
        
        // Establecer campos específicos del gestor
        nuevoGestor.setalias(request.getAlias());
        nuevoGestor.setdescripcion(request.getDescripcion());
        nuevoGestor.setcampoespecializacion(request.getEspecialidad());
        nuevoGestor.settipocontenidovideooaudio(request.getTipoContenido());
        
        // Guardar y retornar
        GestordeContenido gestorGuardado = gestorDeContenidoRepository.save(nuevoGestor);
        logger.info("Gestor de contenido creado exitosamente con ID: {}", gestorGuardado.getId());
        
        return gestorGuardado;
    }
}
