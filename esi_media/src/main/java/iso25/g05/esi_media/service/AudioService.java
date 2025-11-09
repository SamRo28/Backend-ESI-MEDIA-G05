package iso25.g05.esi_media.service;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import iso25.g05.esi_media.dto.AudioUploadDTO;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.AudioRepository;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;

/**
 * Servicio para gestión de contenido de audio
 * Incluye validaciones de seguridad y lógica de negocio
 */
@Service
public class AudioService {
    
    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024; // 2MB
    private static final String[] ALLOWED_MIME_TYPES = {"audio/mpeg", "audio/mp3"};
    
    @Autowired
    private AudioRepository audioRepository;
    
    @Autowired
    private GestorDeContenidoRepository gestorRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
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
        GestordeContenido gestor = validarGestorAudio(gestorId);
        
        // 2. Validar archivo con validaciones de seguridad avanzadas
        MultipartFile archivo = audioDTO.getArchivo();
        validarArchivo(archivo);
        
        // 3. Crear entidad Audio
        Audio audio = crearAudioDesdeDTO(audioDTO, archivo, gestorId);
        
        // 4. Guardar en base de datos
        Audio audioGuardado = audioRepository.save(audio);
        
        // 5. Actualizar lista de contenidos del gestor
        gestor.getContenidos_subidos().add(audioGuardado.getId());
        gestorRepository.save(gestor);
        
        return audioGuardado;
    }
    
    /**
     * Valida que el gestor existe y está autorizado para subir audio
     */
    private GestordeContenido validarGestorAudio(String gestorId) {
        Optional<GestordeContenido> gestorOpt = gestorRepository.findById(gestorId);
        
        if (gestorOpt.isEmpty()) {
            throw new IllegalArgumentException("Gestor no encontrado con ID: " + gestorId);
        }

        GestordeContenido gestor = gestorOpt.get(); // Verificar que el gestor puede subir audio
        if (!"audio".equalsIgnoreCase(gestor.gettipocontenidovideooaudio())) {
            throw new IllegalArgumentException("El gestor no está autorizado para subir contenido de audio");
        }
        
        return gestor;
    }
    
    /**
     * Valida el archivo de audio subido con múltiples capas de seguridad:
     * 1. Validación básica (tamaño, nombre)
     * 2. Verificación de MIME type (audio/mpeg, audio/mp3)
     * 3. Verificación de extensión (.mp3)
     * 4. Verificación de magic bytes (cabeceras del archivo)
     */
    private void validarArchivo(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo de audio es obligatorio");
        }
        
        // Validar tamaño
        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo de 2MB");
        }
        
        // Validar que tiene nombre
        String filename = archivo.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("El archivo debe tener un nombre válido");
        }
        
        // Validar MIME type
        String mimeType = archivo.getContentType();
        if (mimeType == null) {
            throw new IllegalArgumentException("No se pudo determinar el tipo MIME del archivo");
        }
        
        boolean mimeValido = false;
        for (String allowedType : ALLOWED_MIME_TYPES) {
            if (allowedType.equals(mimeType)) {
                mimeValido = true;
                break;
            }
        }
        if (!mimeValido) {
            throw new IllegalArgumentException(
                String.format("Tipo MIME no válido: %s. Solo se permiten archivos MP3 (audio/mpeg, audio/mp3)", 
                mimeType));
        }
        
        // Validar extensión del nombre de archivo
        if (!filename.toLowerCase().endsWith(".mp3")) {
            throw new IllegalArgumentException("Extensión de archivo no válida. Se requiere archivo con extensión .mp3");
        }
        
        // Validar magic bytes (cabeceras del archivo)
        validarMagicBytes(archivo);
    }
    
    /**
     * Valida los magic bytes (cabeceras) del archivo MP3
     * Verifica que el archivo sea realmente un MP3 analizando sus primeros bytes
     */
    private void validarMagicBytes(MultipartFile archivo) throws IOException {
        // Leer los primeros 12 bytes para análisis de cabeceras
        byte[] bytes = archivo.getInputStream().readNBytes(12);
        
        if (bytes.length < 4) {
            throw new IllegalArgumentException("Archivo demasiado pequeño para ser un MP3 válido");
        }
        
        String formatoDetectado = detectarFormatoPorMagicBytes(bytes);
        
        if (!"mp3".equals(formatoDetectado)) {
            throw new IllegalArgumentException(
                String.format("Se requiere un archivo MP3 /// (Formato detectado: %s)", 
                formatoDetectado != null ? formatoDetectado : "desconocido"));
        }
    }
    
    /**
     * Detecta el formato de audio basándose en los magic bytes
     * @param bytes Primeros bytes del archivo
     * @return Formato detectado o null si no se reconoce
     */
    private String detectarFormatoPorMagicBytes(byte[] bytes) {
        if (bytes.length < 4) return null;
        
        // Convertir a unsigned bytes para comparaciones
        int[] ubytes = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            ubytes[i] = bytes[i] & 0xFF;
        }
        
        // MP3: ID3 tag al inicio (ID3v2)
        if (ubytes[0] == 0x49 && ubytes[1] == 0x44 && ubytes[2] == 0x33) {
            return "mp3";
        }
        
        // MP3: Frame de audio directo (sincronización MPEG)
        // Primer byte = 0xFF, segundo byte con bits 7,6,5 = 1 (0xE0)
        if (ubytes[0] == 0xFF && (ubytes[1] & 0xE0) == 0xE0) {
            return "mp3";
        }
        
        // WAV: 'RIFF'....'WAVE'
        if (ubytes[0] == 0x52 && ubytes[1] == 0x49 && ubytes[2] == 0x46 && ubytes[3] == 0x46 &&
            bytes.length >= 12 &&
            ubytes[8] == 0x57 && ubytes[9] == 0x41 && ubytes[10] == 0x56 && ubytes[11] == 0x45) {
            return "wav";
        }
        
        // OGG: 'OggS'
        if (ubytes[0] == 0x4F && ubytes[1] == 0x67 && ubytes[2] == 0x67 && ubytes[3] == 0x53) {
            return "ogg";
        }
        
        // M4A/AAC: 'ftyp' en bytes 4-7 (después de tamaño de caja)
        if (bytes.length >= 8 && 
            ubytes[4] == 0x66 && ubytes[5] == 0x74 && ubytes[6] == 0x79 && ubytes[7] == 0x70) {
            return "m4a";
        }
        
        return null; // Formato no reconocido
    }
    
    /**
     * Crea la entidad Audio desde el DTO
     */
    private Audio crearAudioDesdeDTO(AudioUploadDTO dto, MultipartFile archivo, String gestorId) throws IOException {
        Binary audioBinary = new Binary(archivo.getBytes());
        
        return new Audio(
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
            audioBinary,
            archivo.getContentType(),
            archivo.getSize(),
            gestorId
        );
    }
    
    /**
     * Obtiene todos los audios subidos por un gestor específico
     */
    public Iterable<Audio> obtenerAudiosPorGestor(String gestorId) {
        Optional<GestordeContenido> gestorOpt = gestorRepository.findById(gestorId);
        
        if (gestorOpt.isEmpty()) {
            throw new IllegalArgumentException("Gestor no encontrado con ID: " + gestorId);
        }
        
        return audioRepository.findAllById(gestorOpt.get().getContenidos_subidos());
    }
    
    /**
     * Valida el token de autorización y extrae el gestorId
     * @param authHeader Header de autorización
     * @return ID del gestor autenticado
     * @throws IllegalArgumentException Si el token es inválido
     */
    private String validarTokenYObtenerGestorId(String  tokenValue) {

       
       
        if (tokenValue.isEmpty()) {
            throw new IllegalArgumentException("Token vacío");
        }
        
        // 2. Buscar token en la base de datos
        Optional<Usuario> usuarioOpt = usuarioRepository.findBySesionToken(tokenValue);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Token no válido");
        }

        Usuario usuario = usuarioOpt.get();
        
        Optional<Token> tokenOpt = usuario.getSesionstoken().stream()
            .filter(t -> {
                try {
                    String v = t.getToken();
                    if (tokenValue.equals(v)) return true;
                } catch (NoSuchMethodError | AbstractMethodError | Exception ignored) {
                    // ignored
                }
                // Fallback: comparar con toString()
                return tokenValue.equals(String.valueOf(t));
            })
            .findFirst();

        if (tokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Token no válido");
        }

        Token token = tokenOpt.get();

        // 3. Verificar que el token no ha expirado
        if (token.getFechaExpiracion().before(new Date())) {
            throw new IllegalArgumentException("Token expirado");
        }
        
        
        // 5. Verificar que el usuario es un gestor de contenido
        Optional<GestordeContenido> gestorOpt = gestorRepository.findById(usuario.getId());
        
        if (gestorOpt.isEmpty()) {
            throw new IllegalArgumentException("El usuario no es un gestor de contenido");
        }
        
        return usuario.getId();
    }
}