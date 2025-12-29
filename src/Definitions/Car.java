package Definitions;

public class Car {
    private int id;
    private String company;
    private String type;
    private String color;
    private int year;
    private String status; // Rented/Available
    private double price_per_day;
    private String img_link;

    public Car(String img_link, double price_per_day) {
        this.img_link = img_link;
        this.price_per_day = price_per_day;
    }
    public Car(int carId, String type, String model, double pricePerDay, String status) {
        this.id = carId;
        this.type = type;
        this.company = model;
        this.price_per_day = pricePerDay;
        this.status = status;
    }
    public Car(int id, String company, String type, String color, int year, String status, double price_per_day, String img_link) {
        this.id = id;
        this.company = company;
        this.type = type;
        this.color = color;
        this.year = year;
        this.status = status;
        this.price_per_day = price_per_day;
        this.img_link = img_link;
    }




    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public double getPrice_per_day() {
        return price_per_day;
    }

    public void setPrice_per_day(double price_per_day) {
        this.price_per_day = price_per_day;
    }
    public String getImg_link() {
        return img_link;
    }
    public void setImg_link(String img_link) {
        this.img_link = img_link;
    }

}
