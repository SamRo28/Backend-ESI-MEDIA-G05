package iso25.g05.esi_media.service;

import iso25.g05.esi_media.dto.AudioUploadDTO;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.model.Gestor_de_Contenido;
import iso25.g05.esi_media.repository.AudioRepository;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;
import java.io.IOException;

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
}