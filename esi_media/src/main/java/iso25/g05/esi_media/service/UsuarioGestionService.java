package iso25.g05.esi_media.service;

import iso25.g05.esi_media.dto.AdministradorGestionDTO;
import iso25.g05.esi_media.dto.GestorGestionDTO;
import iso25.g05.esi_media.dto.VisualizadorGestionDTO;
import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de usuarios por administradores
 * Permite a los administradores ver y modificar datos de otros usuarios
 */
@Service
public class UsuarioGestionService {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private TokenRepository tokenRepository;
    
    /**
     * Verifica si el token pertenece a un administrador válido
     */
    private void validarAdministrador(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token de autorización requerido");
        }
        
        String token = authHeader.substring(7);
        Optional<Token> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Token inválido");
        }
        
        Token tokenObj = tokenOpt.get();
        if (tokenObj.getFechaExpiracion().before(new Date())) {
            throw new RuntimeException("Token expirado");
        }
        
        // Verificar que el usuario es administrador
        Usuario usuario = tokenObj.getUsuario();
        if (usuario == null) {
            throw new RuntimeException("Token inválido - usuario no encontrado");
        }
        
        // Verificar que el usuario es una instancia de Administrador
        if (!(usuario instanceof Administrador)) {
            throw new RuntimeException("Solo los administradores pueden gestionar usuarios");
        }
    }
    
    /**
     * Obtiene todos los visualizadores
     */
    public List<VisualizadorGestionDTO> obtenerVisualizadores(String authHeader) {
        validarAdministrador(authHeader);
        
        Query query = new Query(Criteria.where("_class").is("iso25.g05.esi_media.model.Visualizador"));
        List<Visualizador> visualizadores = mongoTemplate.find(query, Visualizador.class, "users");
        
        return visualizadores.stream()
                .map(this::convertirAVisualizadorDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los gestores de contenido
     */
    public List<GestorGestionDTO> obtenerGestores(String authHeader) {
        validarAdministrador(authHeader);
        
        Query query = new Query(Criteria.where("_class").is("iso25.g05.esi_media.model.GestordeContenido"));
        List<GestordeContenido> gestores = mongoTemplate.find(query, GestordeContenido.class, "users");
        
        return gestores.stream()
                .map(this::convertirAGestorDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los administradores
     */
    public List<AdministradorGestionDTO> obtenerAdministradores(String authHeader) {
        validarAdministrador(authHeader);
        
        Query query = new Query(Criteria.where("_class").is("iso25.g05.esi_media.model.Administrador"));
        List<Administrador> administradores = mongoTemplate.find(query, Administrador.class, "users");
        
        return administradores.stream()
                .map(this::convertirAAdministradorDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene un visualizador por ID
     */
    public VisualizadorGestionDTO obtenerVisualizadorPorId(String id, String authHeader) {
        validarAdministrador(authHeader);
        
        Query query = new Query(Criteria.where("id").is(id)
                .and("_class").is("iso25.g05.esi_media.model.Visualizador"));
        Visualizador visualizador = mongoTemplate.findOne(query, Visualizador.class, "users");
        
        if (visualizador == null) {
            throw new RuntimeException("Visualizador no encontrado");
        }
        
        return convertirAVisualizadorDTO(visualizador);
    }
    
    /**
     * Modifica un visualizador (solo campos permitidos)
     */
    public VisualizadorGestionDTO modificarVisualizador(String id, VisualizadorGestionDTO dto, String authHeader) {
        validarAdministrador(authHeader);
        
        Query query = new Query(Criteria.where("id").is(id)
                .and("_class").is("iso25.g05.esi_media.model.Visualizador"));
        Visualizador visualizador = mongoTemplate.findOne(query, Visualizador.class, "users");
        
        if (visualizador == null) {
            throw new RuntimeException("Visualizador no encontrado");
        }
        
        // Actualizar solo campos modificables
        visualizador.setNombre(dto.getNombre());
        visualizador.setApellidos(dto.getApellidos());
        visualizador.setFoto(dto.getFoto());
        visualizador.setAlias(dto.getAlias());
        visualizador.setFechaNac(dto.getFechanac());
        // email, bloqueado, fecharegistro, vip son solo lectura
        
        mongoTemplate.save(visualizador, "users");
        return convertirAVisualizadorDTO(visualizador);
    }
    
    /**
     * Obtiene un gestor por ID
     */
    public GestorGestionDTO obtenerGestorPorId(String id, String authHeader) {
        validarAdministrador(authHeader);
        
        Query query = new Query(Criteria.where("id").is(id)
                .and("_class").is("iso25.g05.esi_media.model.GestordeContenido"));
        GestordeContenido gestor = mongoTemplate.findOne(query, GestordeContenido.class, "users");
        
        if (gestor == null) {
            throw new RuntimeException("Gestor no encontrado");
        }
        
        return convertirAGestorDTO(gestor);
    }
    
    /**
     * Modifica un gestor de contenido (solo campos permitidos)
     */
    public GestorGestionDTO modificarGestor(String id, GestorGestionDTO dto, String authHeader) {
        validarAdministrador(authHeader);
        
        Query query = new Query(Criteria.where("id").is(id)
                .and("_class").is("iso25.g05.esi_media.model.GestordeContenido"));
        GestordeContenido gestor = mongoTemplate.findOne(query, GestordeContenido.class, "users");
        
        if (gestor == null) {
            throw new RuntimeException("Gestor no encontrado");
        }
        
        // Actualizar solo campos modificables
        gestor.setNombre(dto.getNombre());
        gestor.setApellidos(dto.getApellidos());
        gestor.setFoto(dto.getFoto());
        gestor.setalias(dto.getAlias());
        gestor.setcampoespecializacion(dto.getCampoespecializacion());
        gestor.setdescripcion(dto.getDescripcion());
        // email, bloqueado, fecharegistro, tipocontenidovideooaudio son solo lectura
        
        mongoTemplate.save(gestor, "users");
        return convertirAGestorDTO(gestor);
    }
    
    /**
     * Obtiene un administrador por ID
     */
    public AdministradorGestionDTO obtenerAdministradorPorId(String id, String authHeader) {
        validarAdministrador(authHeader);
        
        Query query = new Query(Criteria.where("id").is(id)
                .and("_class").is("iso25.g05.esi_media.model.Administrador"));
        Administrador administrador = mongoTemplate.findOne(query, Administrador.class, "users");
        
        if (administrador == null) {
            throw new RuntimeException("Administrador no encontrado");
        }
        
        return convertirAAdministradorDTO(administrador);
    }
    
    /**
     * Modifica un administrador (solo campos permitidos)
     */
    public AdministradorGestionDTO modificarAdministrador(String id, AdministradorGestionDTO dto, String authHeader) {
        validarAdministrador(authHeader);
        
        Query query = new Query(Criteria.where("id").is(id)
                .and("_class").is("iso25.g05.esi_media.model.Administrador"));
        Administrador administrador = mongoTemplate.findOne(query, Administrador.class, "users");
        
        if (administrador == null) {
            throw new RuntimeException("Administrador no encontrado");
        }
        
        // Actualizar solo campos modificables
        administrador.setNombre(dto.getNombre());
        administrador.setApellidos(dto.getApellidos());
        administrador.setFoto(dto.getFoto());
        administrador.setDepartamento(dto.getDepartamento());
        // email, bloqueado, fecharegistro son solo lectura
        
        mongoTemplate.save(administrador, "users");
        return convertirAAdministradorDTO(administrador);
    }
    
    // Métodos de conversión
    private VisualizadorGestionDTO convertirAVisualizadorDTO(Visualizador visualizador) {
        VisualizadorGestionDTO dto = new VisualizadorGestionDTO();
        dto.setId(visualizador.getId());
        dto.setNombre(visualizador.getNombre());
        dto.setApellidos(visualizador.getApellidos());
        dto.setEmail(visualizador.getEmail());
        dto.setFoto(visualizador.getFoto());
        dto.setBloqueado(visualizador.isBloqueado());
        dto.setFecharegistro(visualizador.getFechaRegistro());
        dto.setAlias(visualizador.getAlias());
        dto.setFechanac(visualizador.getFechaNac());
        dto.setVip(visualizador.isVip());
        return dto;
    }
    
    private GestorGestionDTO convertirAGestorDTO(GestordeContenido gestor) {
        GestorGestionDTO dto = new GestorGestionDTO();
        dto.setId(gestor.getId());
        dto.setNombre(gestor.getNombre());
        dto.setApellidos(gestor.getApellidos());
        dto.setEmail(gestor.getEmail());
        dto.setFoto(gestor.getFoto());
        dto.setBloqueado(gestor.isBloqueado());
        dto.setFecharegistro(gestor.getFechaRegistro());
        dto.setAlias(gestor.getalias());
        dto.setCampoespecializacion(gestor.getcampoespecializacion());
        dto.setDescripcion(gestor.getdescripcion());
        dto.setTipocontenidovideooaudio(gestor.gettipocontenidovideooaudio());
        return dto;
    }
    
    private AdministradorGestionDTO convertirAAdministradorDTO(Administrador administrador) {
        AdministradorGestionDTO dto = new AdministradorGestionDTO();
        dto.setId(administrador.getId());
        dto.setNombre(administrador.getNombre());
        dto.setApellidos(administrador.getApellidos());
        dto.setEmail(administrador.getEmail());
        dto.setFoto(administrador.getFoto());
        dto.setBloqueado(administrador.isBloqueado());
        dto.setFecharegistro(administrador.getFechaRegistro());
        dto.setDepartamento(administrador.getDepartamento());
        return dto;
    }
}