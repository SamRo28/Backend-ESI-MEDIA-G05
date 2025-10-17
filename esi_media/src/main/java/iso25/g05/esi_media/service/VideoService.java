package iso25.g05.esi_media.service;

import iso25.g05.esi_media.dto.VideoUploadDTO;
import iso25.g05.esi_media.model.Video;
import iso25.g05.esi_media.model.Gestor_de_Contenido;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.VideoRepository;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;
import iso25.g05.esi_media.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Date;

/**
 * Servicio para gestión de contenido de video
 * Incluye validaciones de URL y lógica de negocio
 */
@Service
public class VideoService {
    
    @Autowired
    private VideoRepository videoRepository;
    
    @Autowired
    private GestorDeContenidoRepository gestorRepository;
    
    @Autowired
    private TokenRepository tokenRepository;
    
    /**
     * Sube un nuevo video por URL validando el token de autorización
     * @param videoDTO Datos del video a subir
     * @param authHeader Header de autorización con el token
     * @return Video guardado
     * @throws IllegalArgumentException Si las validaciones fallan
     */
    public Video subirVideoConToken(VideoUploadDTO videoDTO, String authHeader) {
        // 1. Validar token y extraer gestorId
        String gestorId = validarTokenYObtenerGestorId(authHeader);
        
        // 2. Usar el método existente para subir el video
        return subirVideo(videoDTO, gestorId);
    }
    
    /**
     * Sube un nuevo video por URL
     * @param videoDTO Datos del video a subir
     * @param gestorId ID del gestor que sube el contenido
     * @return Video guardado
     * @throws IllegalArgumentException Si las validaciones fallan
     */
    public Video subirVideo(VideoUploadDTO videoDTO, String gestorId) {
        // 1. Validar que el gestor existe y puede subir video
        Gestor_de_Contenido gestor = validarGestorVideo(gestorId);
        
        // 2. Validar URL
        validarUrl(videoDTO.getUrl());
        
        // 3. Crear entidad Video
        Video video = crearVideoDesdeDTO(videoDTO, gestorId);
        
        // 4. Guardar en base de datos
        Video videoGuardado = videoRepository.save(video);
        
        // 5. Actualizar lista de contenidos del gestor
        gestor.getContenidos_subidos().add(videoGuardado.getId());
        gestorRepository.save(gestor);
        
        return videoGuardado;
    }
    
    /**
     * Valida que el gestor existe y está autorizado para subir video
     */
    private Gestor_de_Contenido validarGestorVideo(String gestorId) {
        Optional<Gestor_de_Contenido> gestorOpt = gestorRepository.findById(gestorId);
        
        if (gestorOpt.isEmpty()) {
            throw new IllegalArgumentException("Gestor no encontrado con ID: " + gestorId);
        }
        
        Gestor_de_Contenido gestor = gestorOpt.get();
        
        // Verificar que el gestor puede subir video
        if (!"video".equalsIgnoreCase(gestor.getTipo_contenido_video_o_audio())) {
            throw new IllegalArgumentException("El gestor no está autorizado para subir contenido de video");
        }
        
        return gestor;
    }
    
    /**
     * Valida que la URL es accesible y tiene formato correcto
     */
    private void validarUrl(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL del video es obligatoria");
        }
        
        try {
            URL url = new URL(urlString);
            
            // Verificar que el protocolo es HTTP o HTTPS
            String protocol = url.getProtocol();
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
                throw new IllegalArgumentException("La URL debe usar protocolo HTTP o HTTPS");  // En el futuro podría soportar solo HTTPS
            }
            
            // Verificar que tiene host válido
            if (url.getHost() == null || url.getHost().trim().isEmpty()) {
                throw new IllegalArgumentException("La URL debe tener un host válido");
            }
            
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("La URL no tiene un formato válido: " + e.getMessage());
        }
    }
    
    /**
     * Crea la entidad Video desde el DTO
     */
    private Video crearVideoDesdeDTO(VideoUploadDTO dto, String gestorId) {
        return new Video(
            null, // ID será generado por MongoDB
            dto.getTitulo(),
            dto.getDescripcion(),
            dto.getTags(),
            dto.getDuracion(),
            dto.getVip(),
            dto.getVisible(),
            null,
            dto.getFechaDisponibleHasta(),
            dto.getEdadVisualizacion(),
            dto.getCaratula(),
            0, // Número de visualizaciones inicial
            dto.getUrl(),
            dto.getResolucion(),
            gestorId
        );
    }
    
    /**
     * Obtiene todos los videos subidos por un gestor específico
     */
    public Iterable<Video> obtenerVideosPorGestor(String gestorId) {
        Optional<Gestor_de_Contenido> gestorOpt = gestorRepository.findById(gestorId);
        
        if (gestorOpt.isEmpty()) {
            throw new IllegalArgumentException("Gestor no encontrado con ID: " + gestorId);
        }
        
        return videoRepository.findAllById(gestorOpt.get().getContenidos_subidos());
    }
    
    /**
     * Valida el token de autorización y extrae el gestorId
     * @param authHeader Header de autorización "Bearer token"
     * @return ID del gestor autenticado
     * @throws IllegalArgumentException Si el token es inválido
     */
    private String validarTokenYObtenerGestorId(String authHeader) {
        // 1. Extraer token del header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Header de autorización inválido");
        }
        
        String tokenValue = authHeader.replace("Bearer ", "").trim();
        if (tokenValue.isEmpty()) {
            throw new IllegalArgumentException("Token vacío");
        }
        
        // 2. Buscar token en la base de datos
        Optional<Token> tokenOpt = tokenRepository.findByToken(tokenValue);
        if (tokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Token no válido");
        }
        
        Token token = tokenOpt.get();
        
        // 3. Verificar que el token no ha expirado
        if (token.getFechaExpiracion().before(new Date())) {
            throw new IllegalArgumentException("Token expirado");
        }
        
        // 4. Obtener el usuario asociado al token
        Usuario usuario = token.getUsuario();
        if (usuario == null) {
            throw new IllegalArgumentException("Token sin usuario asociado");
        }
        
        // 5. Verificar que el usuario es un gestor de contenido
        Optional<Gestor_de_Contenido> gestorOpt = gestorRepository.findById(usuario.getId());
        if (gestorOpt.isEmpty()) {
            throw new IllegalArgumentException("El usuario no es un gestor de contenido");
        }
        
        return usuario.getId();
    }
}