package iso25.g05.esi_media.service;

import iso25.g05.esi_media.model.Visualizador;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que encapsula el resultado de un intento de registro de visualizador.
 * 
 * Permite devolver tanto el éxito/fallo como los mensajes de error específicos
 * para que el controlador pueda informar al usuario qué pasó exactamente.
 */

/**
 * Flujo de trabajo:
 * 1. Usuario envía formulario → Frontend
 * 2. Frontend envía datos → Controlador
 * 3. Controlador llama → VisualizadorService.registrarVisualizador()
 * 4. Servicio procesa y devuelve → RegistroResultado
 * 5. Controlador examina el RegistroResultado:
 * SI exitoso = true:
 *      - Mensaje: "Usuario registrado correctamente"
 *      - Visualizador: datos del usuario creado
 *      - Errores: lista vacía
 *      → Respuesta HTTP 200 con éxito 
 * SI exitoso = false:
 *      - Mensaje: "Registro fallido"
 *      - Visualizador: null
 *      - Errores: ["Email ya existe", "Contraseña muy débil"]
 *      → Respuesta HTTP 400 con errores específicos
 */
public class RegistroResultado {
    
    /**
     * Indica si el registro fue exitoso
     */
    private boolean exitoso;
    
    /**
     * Lista de mensajes de error (vacía si exitoso = true)
     */
    private List<String> errores;
    
    /**
     * El visualizador creado (null si exitoso = false)
     */
    private Visualizador visualizador;
    
    /**
     * Mensaje de éxito o error general
     */
    private String mensaje;
    
    /**
     * Constructor para resultado exitoso
     */
    public RegistroResultado(Visualizador visualizador, String mensaje) {
        this.exitoso = true;
        this.visualizador = visualizador;
        this.mensaje = mensaje;
        this.errores = new ArrayList<>();
    }
    
    /**
     * Constructor para resultado con errores
     */
    public RegistroResultado(List<String> errores, String mensaje) {
        this.exitoso = false;
        
        // Le digo que si me pasaron una lista de errores, úsala. Si me pasaron null, crea una lista vacía"
        // Es como decir: if (errores != null) { this.errores = errores; } else { this.errores = new ArrayList<>(); }
        // ¿Por qué? Para evitar errores de NullPointerException
        this.errores = errores != null ? errores : new ArrayList<>();
        
        this.mensaje = mensaje;
        this.visualizador = null;
    }
    
    /**
     * Constructor para un solo error
     */
    public RegistroResultado(String error, String mensaje) {
        this.exitoso = false;
        this.errores = new ArrayList<>();
        this.errores.add(error);
        this.mensaje = mensaje;
        this.visualizador = null;
    }
    
    // Getters y Setters
    
    public boolean isExitoso() {
        return exitoso;
    }
    
    public List<String> getErrores() {
        return errores;
    }
    
    public Visualizador getVisualizador() {
        return visualizador;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    /**
     * Método helper para agregar errores adicionales después de crear el objeto
     * 
     * A veces creamos un RegistroResultado exitoso, pero luego descubrimos más errores
     * En lugar de crear un objeto nuevo, podemos "convertir" este resultado en fallido
     * 
     * EJEMPLO DE USO:
     * RegistroResultado resultado = new RegistroResultado(visualizador, "Éxito");
     * // Más tarde, descubrimos un problema
     * resultado.agregarError("El email ya está registrado en otro sistema");
     * // Ahora el resultado cambió a fallido automáticamente
     */
    public void agregarError(String error) {
        // Si por alguna razón la lista es null, crear una nueva
        if (this.errores == null) {
            this.errores = new ArrayList<>();
        }
        
        // Agregar el nuevo error a la lista
        this.errores.add(error);
        
        // IMPORTANTE: Cambiar automáticamente el resultado a fallido
        // Porque si hay errores, ya no puede ser exitoso
        this.exitoso = false;
    }
}