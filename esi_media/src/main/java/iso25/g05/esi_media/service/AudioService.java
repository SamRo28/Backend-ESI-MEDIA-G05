package iso25.g05.esi_media.service;

import iso25.g05.esi_media.dto.AudioUploadDTO;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.model.Gestor_de_Contenido;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.AudioRepository;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;
import iso25.g05.esi_media.repository.TokenRepository;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;
import java.io.IOException;
import java.util.Date;

/**
 * Servicio para gestión de contenido de audio
 * Incluye validaciones de seguridad y lógica de negocio
 */
@Service
public class AudioService {
    
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final String[] ALLOWED_MIME_TYPES = {"audio/mpeg", "audio/mp3"};
    
    @Autowired
    private AudioRepository audioRepository;
    
    @Autowired
    private GestorDeContenidoRepository gestorRepository;
    
    @Autowired
    private TokenRepository tokenRepository;
    
    /**
     * Sube un nuevo archivo de audio validando el token de autorización
     * @param audioDTO Datos del audio a subir
     * @param authHeader Header de autorización con el token
     * @return Audio guardado
     * @throws IllegalArgumentException Si las validaciones fallan
     * @throws IOException Si hay error procesando el archivo
     */
    public Audio subirAudioConToken(AudioUploadDTO audioDTO, String authHeader) throws IOException {
        // 1. Validar token y extraer gestorId
        String gestorId = validarTokenYObtenerGestorId(authHeader);
        
        // 2. Usar el método existente para subir el audio
        return subirAudio(audioDTO, gestorId);
    }
    
    /**
     * Sube un nuevo archivo de audio
     * @param audioDTO Datos del audio a subir
     * @param gestorId ID del gestor que sube el contenido
     * @return Audio guardado
     * @throws IllegalArgumentException Si las validaciones fallan
     * @throws IOException Si hay error procesando el archivo
     */
    public Audio subirAudio(AudioUploadDTO audioDTO, String gestorId) throws IOException {
        // 1. Validar que el gestor existe y puede subir audio
        Gestor_de_Contenido gestor = validarGestorAudio(gestorId);
        
        // 2. Validar archivo
        MultipartFile archivo = audioDTO.getArchivo();
        validarArchivo(archivo);
        
        // 3. Crear entidad Audio
        Audio audio = crearAudioDesdeDTO(audioDTO, archivo);
        
        // 4. Guardar en base de datos
        Audio audioGuardado = audioRepository.save(audio);
        
        // 5. Actualizar lista de contenidos del gestor
        gestor.getContenidosSubidos().add(audioGuardado.getId());
        gestorRepository.save(gestor);
        
        return audioGuardado;
    }
    
    /**
     * Valida que el gestor existe y está autorizado para subir audio
     */
    private Gestor_de_Contenido validarGestorAudio(String gestorId) {
        Optional<Gestor_de_Contenido> gestorOpt = gestorRepository.findById(gestorId);
        
        if (gestorOpt.isEmpty()) {
            throw new IllegalArgumentException("Gestor no encontrado con ID: " + gestorId);
        }
        
        Gestor_de_Contenido gestor = gestorOpt.get();
        
        // Verificar que el gestor puede subir audio
        if (!"audio".equalsIgnoreCase(gestor.get_tipo_contenido_video_o_audio())) {
            throw new IllegalArgumentException("El gestor no está autorizado para subir contenido de audio");
        }
        
        return gestor;
    }
    
    /**
     * Valida el archivo de audio subido
     */
    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo de audio es obligatorio");
        }
        
        // Validar tamaño
        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo de 2MB");    // Este tamaño podría ajustarse según necesidades
        }
        
        // Validar MIME type
        String mimeType = archivo.getContentType();
        boolean mimeValido = false;
        for (String allowedType : ALLOWED_MIME_TYPES) {
            if (allowedType.equals(mimeType)) {
                mimeValido = true;
                break;
            }
        }
        
        if (!mimeValido) {
            throw new IllegalArgumentException("Tipo de archivo no válido. Solo se permiten archivos MP3");
        }
        
        // Validar extensión del nombre de archivo
        String filename = archivo.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".mp3")) {
            throw new IllegalArgumentException("El archivo debe tener extensión .mp3");
        }
    }
    
    /**
     * Crea la entidad Audio desde el DTO
     */
    private Audio crearAudioDesdeDTO(AudioUploadDTO dto, MultipartFile archivo) throws IOException {
        Binary audioBinary = new Binary(archivo.getBytes());
        
        return new Audio(
            null, // ID será generado por MongoDB
            dto.getTitulo(),
            dto.getDescripcion(),
            dto.getTags(),
            dto.getDuracion(),
            dto.getVip(),
            dto.getVisible(), // El usuario decide la visibilidad
            null, // Fecha estado automático
            dto.getFechaDisponibleHasta(),
            dto.getEdadVisualizacion(),
            dto.getCaratula(),
            0, // Número de visualizaciones inicial
            audioBinary,
            archivo.getContentType(),
            archivo.getSize()
        );
    }
    
    /**
     * Obtiene todos los audios subidos por un gestor específico
     */
    public Iterable<Audio> obtenerAudiosPorGestor(String gestorId) {
        Optional<Gestor_de_Contenido> gestorOpt = gestorRepository.findById(gestorId);
        
        if (gestorOpt.isEmpty()) {
            throw new IllegalArgumentException("Gestor no encontrado con ID: " + gestorId);
        }
        
        return audioRepository.findAllById(gestorOpt.get().getContenidosSubidos());
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
        Usuario usuario = token._usuario;
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