package Definitions;

public class Rental {
    private int idRentals;
    private int userId;
    private int carId;
    private String startDate;
    private String endDate;
    private double totalPrice;

    public Rental(int idRentals, int userId, int carId, String startDate, String endDate, double totalPrice) {
        this.idRentals = idRentals;
        this.userId = userId;
        this.carId = carId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
    }

    public int getIdRentals() {
        return idRentals;
    }

    public void setIdRentals(int idRentals) {
        this.idRentals = idRentals;
    }

    public int getUserId() {
        return userId;
    }

    public int getCarId() {
        return carId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}