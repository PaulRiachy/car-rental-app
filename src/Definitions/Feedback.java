package Definitions;

public class Feedback {
    private int id_feedback;
    private int user_id;
    private int car_id;
    private int rating;
    private String comment;
    private String date;
    public Feedback(int id_feedback, int user_id, int car_id, int rating, String comment, String date) {
        this.id_feedback = id_feedback;
        this.user_id = user_id;
        this.car_id = car_id;
        this.rating = rating;
        this.comment = comment;
        this.date = date;
    }
    public int getId_feedback() {
        return id_feedback;
    }
    public void setId_feedback(int id_feedback) {
        this.id_feedback = id_feedback;
    }

    public int getRating() {
        return rating;
    }
    public void setRating(int rating) {
        this.rating = rating;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

}
