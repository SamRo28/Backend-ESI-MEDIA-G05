package iso25.g05.esi_media.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iso25.g05.esi_media.dto.VideoUploadDTO;
import iso25.g05.esi_media.model.Video;
import iso25.g05.esi_media.service.VideoService;
import jakarta.validation.Valid;

/**
 * Controlador REST para gestión de contenido de video por gestores
 * Endpoints para subir videos
 */
@RestController
@RequestMapping("/gestor/video")
public class VideoController {
    
    @Autowired
    private VideoService videoService;

    private String SCS = "success";
    private  String MSG = "message";
    
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
            @CookieValue(value = "SESSION_TOKEN", required = false) String token) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (token == null || token.isBlank()) {
            response.put(SCS, false);
            response.put(MSG, "No autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            // El service se encarga de validar el token y extraer el gestorId
            Video videoGuardado = videoService.subirVideoConToken(videoDTO, token);
            
            response.put(SCS, true);
            response.put(MSG, "Video subido exitosamente");
            response.put("videoId", videoGuardado.getId());
            response.put("titulo", videoGuardado.gettitulo());
            response.put("url", videoGuardado.geturl());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            response.put(SCS, false);
            response.put(MSG, "Error de validación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            response.put(SCS, false);
            response.put(MSG, "Error interno del servidor");
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