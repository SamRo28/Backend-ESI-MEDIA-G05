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
        @RequestHeader("Authorization") String authHeader) {  // ← RESTAURAR ESTO
    
    Map<String, Object> response = new HashMap<>();
    
    try {
        // CAMBIAR: Usar método CON token (como VideoController)
        Audio audioGuardado = audioService.subirAudioConToken(audioDTO, authHeader);
        
        response.put("success", true);
        response.put("message", "Audio subido exitosamente");
        response.put("audioId", audioGuardado.getId());
        response.put("titulo", audioGuardado.getTitulo());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        
    } catch (IllegalArgumentException e) {
        response.put("success", false);
        response.put("message", "Error de validación: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        
    } catch (IOException e) {
        response.put("success", false);
        response.put("message", "Error procesando el archivo: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Error interno del servidor: " + e.getMessage());
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