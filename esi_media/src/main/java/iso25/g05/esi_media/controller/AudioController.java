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
 * Controlador REST para gestión de contenido de audio
 * Endpoints para subir y consultar audios
 */
@RestController
@RequestMapping("/api/contenido/audio")
@CrossOrigin(origins = "*") // Configuración básica de CORS
public class AudioController {
    
    @Autowired
    private AudioService audioService;
    
    /**
     * Endpoint para subir un nuevo archivo de audio
     * POST /api/contenido/audio/subir
     * 
     * @param audioDTO Datos del audio (form-data con archivo)
     * @param gestorId ID del gestor que sube el contenido
     * @return Respuesta con el audio creado o error
     */
    @PostMapping("/subir")
    public ResponseEntity<Map<String, Object>> subirAudio(
            @Valid @ModelAttribute AudioUploadDTO audioDTO,
            @RequestParam String gestorId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Audio audioGuardado = audioService.subirAudio(audioDTO, gestorId);
            
            response.put("success", true);
            response.put("message", "Audio subido exitosamente");
            response.put("audioId", audioGuardado.getId());
            response.put("titulo", audioGuardado.get_titulo());
            
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
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint para obtener todos los audios de un gestor
     * GET /api/audio/mis-contenidos
     * Header: Authorization: Bearer token123
     * 
     * @return Lista de audios del gestor
     */
    @GetMapping("/mis-contenidos")
    public ResponseEntity<Map<String, Object>> obtenerMisContenidos(@RequestHeader("Authorization") String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extraer el token del header
            String token = authHeader.replace("Bearer ", "");
            
            Iterable<Audio> audios = audioService.obtenerAudiosPorToken(token);
            
            response.put("success", true);
            response.put("audios", audios);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint para verificar que el servicio está funcionando
     * GET /api/contenido/audio/estado
     */
    @GetMapping("/estado")
    public ResponseEntity<Map<String, String>> estado() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "AudioController");
        return ResponseEntity.ok(response);
    }
}