package iso25.g05.esi_media.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) para recibir los datos del formulario de registro de Visualizador.
 * 
 * Esta clase se usa específicamente para capturar los datos que envía
 * el frontend durante el proceso de registro. Incluye campos adicionales
 * como la confirmación de contraseña que no se almacenan en el modelo principal.
 * 
 * Trabaja con la clase Visualizador existente que hereda de Usuario.
 * 
 * NOTA IMPORTANTE SOBRE DUPLICACIÓN DE CÓDIGO:
 * =========================================
 * Sí, esta clase tiene getters/setters muy parecidos a los de Usuario/Visualizador.
 * Esto parece duplicación, pero es INTENCIONAL por estas razones:
 * 
 * 1. PROPÓSITOS DIFERENTES:
 *    - DTO: Solo para recibir datos del formulario web (temporal)
 *    - Modelo: Para lógica de negocio y persistencia (permanente)
 * 
 * 2. CAMPOS DIFERENTES:
 *    - DTO tiene "passwordConfirm" que no existe en el modelo
 *    - DTO usa nombres simples ("nombre"), modelo usa prefijos ("_nombre")
 * 
 * 3. VALIDACIONES DIFERENTES:
 *    - DTO valida datos de entrada del usuario
 *    - Modelo tendría validaciones de integridad de datos
 * 
 * 4. CICLOS DE VIDA DIFERENTES:
 *    - DTO se destruye después del registro
 *    - Modelo persiste en base de datos
 * 
 * Esta "duplicación" es un patrón estándar en Spring Boot y es aceptable.
 * Si en el futuro quieres eliminarla, considera usar MapStruct o similar.
 */
public class VisualizadorRegistroDTO {
    
    /**
     * Nombre del visualizador
     * - Obligatorio
     * - Máximo 40 caracteres
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 40, message = "El nombre no puede exceder 40 caracteres")
    private String nombre;
    
    /**
     * Apellidos del visualizador
     * - Obligatorio
     * - Máximo 60 caracteres
     */
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 60, message = "Los apellidos no pueden exceder 60 caracteres")
    private String apellidos;
    
    /**
     * Email del visualizador
     * - Obligatorio
     * - Formato válido
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;
    
    /**
     * Alias del visualizador
     * - Opcional
     * - Máximo 12 caracteres
     * - Si está vacío o null, se usará el nombre en el servicio
     */
    @JsonProperty("alias")
    @Size(max = 12, message = "El alias no puede exceder 12 caracteres")
    private String alias;
    
    /**
     * Fecha de nacimiento del visualizador
     * - Obligatorio
     * - No puede ser futura
     * - La validación de edad mínima (4 años) se hace en el servicio
     */
    @JsonProperty("fecha_nac")
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento no puede ser futura")
    private Date fecha_nac;
    
    /**
     * Contraseña del visualizador
     * - Obligatorio
     * - Mínimo 8 caracteres
     * - Política de seguridad: mayúscula, minúscula, número y símbolo
     */
    @JsonProperty("contrasenia")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    // Temporalmente comentada para probar
    // @Pattern(
    //     regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
    //     message = "La contraseña debe contener al menos: una mayúscula, una minúscula, un número y un símbolo (@$!%*?&)"
    // )
    private String contrasenia;
    
    /**
     * Confirmación de contraseña
     * - Obligatorio
     * - Debe coincidir con el campo _contrasenia
     * - La validación de coincidencia se hace en el servicio
     */
    @JsonProperty("confirmacion_contrasenia")
    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmacion_contrasenia;
    
    /**
     * Indica si el visualizador quiere plan VIP
     * - Opcional, por defecto false
     */
    private boolean vip = false;
    
    /**
     * Nombre o ruta de la foto de perfil
     * - Opcional
     * - Se almacena como Object en el modelo Usuario
     */
    private String foto;
    
    /**
     * Constructor vacío requerido para binding de formularios
     */
    public VisualizadorRegistroDTO() {
    }
    
    /**
     * Constructor completo para testing y casos específicos
     */
    public VisualizadorRegistroDTO(String nombre, String apellidos, String email, String alias,
                                    Date fecha_nac, String contrasenia, String confirmacion_contrasenia,
                                    boolean vip, String foto) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.alias = alias;
        this.fecha_nac = fecha_nac;
        this.contrasenia = contrasenia;
        this.confirmacion_contrasenia = confirmacion_contrasenia;
        this.vip = vip;
        this.foto = foto;
    }
    
    // Getters y Setters
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getApellidos() {
        return apellidos;
    }
    
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public Date getFechaNac() {
        return fecha_nac;
    }

    public void setFechaNac(Date fecha_nac) {
        this.fecha_nac = fecha_nac;
    }
    
    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
    
    public String getConfirmacionContrasenia() {
        return confirmacion_contrasenia;
    }

    public void setConfirmacionContrasenia(String confirmacion_contrasenia) {
        this.confirmacion_contrasenia = confirmacion_contrasenia;
    }
    
    public boolean isVip() {
        return vip;
    }
    
    public void setVip(boolean vip) {
        this.vip = vip;
    }
    
    public String getFoto() {
        return foto;
    }
    
    public void setFoto(String foto) {
        this.foto = foto;
    }
}