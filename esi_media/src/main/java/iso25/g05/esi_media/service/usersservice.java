package iso25.g05.esi_media.service;

import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.dto.CrearAdministradorRequest;
import iso25.g05.esi_media.repository.AdministradorRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class usersservice {
    
    @Autowired
    private AdministradorRepository administradorRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
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
        Contrasenia contrasenia = new Contrasenia(request.getContrasenia());
        
        Administrador nuevoAdmin = new Administrador(
            request.getApellidos(),
            false, // No bloqueado por defecto
            contrasenia,
            request.getEmail(),
            null, // Sin foto por defecto
            request.getNombre(),
            request.getDepartamento(),
            Administrador.TipoAdministrador.ADMINISTRADOR // Rol administrador por defecto
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
        
        Administrador admin = adminActual.get();
        if (!admin.esSuperAdministrador() && admin.getTipoAdministrador() != Administrador.TipoAdministrador.ADMINISTRADOR) {
            throw new RuntimeException("No tiene permisos para crear administradores");
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
