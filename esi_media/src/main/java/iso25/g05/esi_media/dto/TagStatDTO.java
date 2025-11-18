package iso25.g05.esi_media.dto;

/**
 * DTO para las estadísticas de tags en el filtrado avanzado
 */
public class TagStatDTO {
    
    private String tag;
    private String label;
    private long views;
    
    // Constructor vacío
    public TagStatDTO() {}
    
    // Constructor sin label
    public TagStatDTO(String tag, long views) {
        this.tag = tag;
        this.label = tag; // Por defecto, label igual a tag
        this.views = views;
    }
    
    // Constructor completo
    public TagStatDTO(String tag, String label, long views) {
        this.tag = tag;
        this.label = label;
        this.views = views;
    }
    
    // Getters y Setters
    public String getTag() {
        return tag;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public long getViews() {
        return views;
    }
    
    public void setViews(long views) {
        this.views = views;
    }
}