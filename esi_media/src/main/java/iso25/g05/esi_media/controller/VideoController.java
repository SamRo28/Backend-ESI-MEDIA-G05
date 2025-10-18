package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.dto.VideoUploadDTO;
import iso25.g05.esi_media.model.Video;
import iso25.g05.esi_media.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para gestión de contenido de video por gestores
 * Endpoints para subir videos
 */
@RestController
@RequestMapping("/gestor/video")
@CrossOrigin(origins = "*") // Configuración básica de CORS
public class VideoController {
    
    @Autowired
    private VideoService videoService;
    
    /**
     * Endpoint para subir un nuevo video por URL
     * POST /gestor/video/subir
     * 
     * @param videoDTO Datos del video
     * @param authHeader Token de autorización del gestor
     * @return Respuesta con el video creado o error
     */
    @PostMapping("/subir")
    public ResponseEntity<Map<String, Object>> subirVideo(
            @Valid @RequestBody VideoUploadDTO videoDTO,
            @RequestHeader("Authorization") String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // El service se encarga de validar el token y extraer el gestorId
            Video videoGuardado = videoService.subirVideoConToken(videoDTO, authHeader);
            
            response.put("success", true);
            response.put("message", "Video subido exitosamente");
            response.put("videoId", videoGuardado.getId());
            response.put("titulo", videoGuardado.gettitulo());
            response.put("url", videoGuardado.geturl());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Error de validación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint para verificar que el servicio está funcionando
     * GET /gestor/video/estado
     */
    @GetMapping("/estado")
    public ResponseEntity<Map<String, String>> estado() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "VideoController");
        return ResponseEntity.ok(response);
    }
}