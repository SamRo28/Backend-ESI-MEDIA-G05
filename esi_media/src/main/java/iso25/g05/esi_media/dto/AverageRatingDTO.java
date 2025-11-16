package iso25.g05.esi_media.dto;

public class AverageRatingDTO {
    private Double averageRating;
    private long ratingsCount;

    public AverageRatingDTO() {}

    public AverageRatingDTO(Double averageRating, long ratingsCount) {
        this.averageRating = averageRating;
        this.ratingsCount = ratingsCount;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public long getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(long ratingsCount) {
        this.ratingsCount = ratingsCount;
    }
}
