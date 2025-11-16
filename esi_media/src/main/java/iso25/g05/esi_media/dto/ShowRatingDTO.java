package iso25.g05.esi_media.dto;

public class ShowRatingDTO {

    private Double myRating; // null si no valoró, o el valor si ya valoró

    public ShowRatingDTO() {}

    public ShowRatingDTO(Double myRating) {
        this.myRating = myRating;
    }

    public Double getMyRating() {
        return myRating;
    }

    public void setMyRating(Double myRating) {
        this.myRating = myRating;
    }
}
