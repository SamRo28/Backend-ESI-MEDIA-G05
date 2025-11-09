package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.dto.AudioUploadDTO;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.service.AudioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para gestión de contenido de audio por gestores
 * Endpoints para subir audios
 */
@RestController
@RequestMapping("/gestor/audio")
@CrossOrigin(origins = "*") // Configuración básica de CORS
public class AudioController {
    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String AUDIO_ID_KEY = "audioId";
    private static final String TITULO_KEY = "titulo";
    private static final String ERROR_TYPE_KEY = "errorType";

    @Autowired
    private AudioService audioService;
    
    /**
     * Endpoint para subir un nuevo archivo de audio
     * POST /gestor/audio/subir
     * 
     * @param audioDTO Datos del audio
     * @param authHeader Token de autorización del gestor
     * @return Respuesta con el audio creado o error
     */
@PostMapping("/subir")
public ResponseEntity<Map<String, Object>> subirAudio(
        @Valid @ModelAttribute AudioUploadDTO audioDTO,
        @RequestHeader("Authorization") String authHeader) {
    
    Map<String, Object> response = new HashMap<>();
    
    try {
        Audio audioGuardado = audioService.subirAudioConToken(audioDTO, authHeader);

        response.put(SUCCESS_KEY, true);
        response.put(MESSAGE_KEY, "Audio subido exitosamente");
        response.put(AUDIO_ID_KEY, audioGuardado.getId());
        response.put(TITULO_KEY, audioGuardado.gettitulo());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        
    } catch (IllegalArgumentException e) {
        // Errores de validación (MIME type, extensión, magic bytes, etc.)
        response.put(SUCCESS_KEY, false);
        response.put(MESSAGE_KEY, "Error de validación: " + e.getMessage());
        response.put(ERROR_TYPE_KEY, "VALIDATION_ERROR");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        
    } catch (IOException e) {
        // Errores procesando el archivo (lectura, magic bytes, etc.)
        response.put(SUCCESS_KEY, false);
        response.put(MESSAGE_KEY, "Error procesando el archivo: " + e.getMessage());
        response.put(ERROR_TYPE_KEY, "FILE_PROCESSING_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        
    } catch (Exception e) {
        // Otros errores no esperados
        response.put(SUCCESS_KEY, false);
        response.put(MESSAGE_KEY, "Error interno del servidor: " + e.getMessage());
        response.put(ERROR_TYPE_KEY, "INTERNAL_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
    
    /**
     * Endpoint para verificar que el servicio está funcionando
     * GET /gestor/audio/estado
     */
    @GetMapping("/estado")
    public ResponseEntity<Map<String, String>> estado() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "AudioController");
        return ResponseEntity.ok(response);
    }
}