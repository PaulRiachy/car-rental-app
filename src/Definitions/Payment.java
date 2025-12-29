package Definitions;

public class Payment {
    private int idPayment;
    private int idRental;
    private double amount;
    private String paymentDate;
    private String method;

    public Payment(int idPayment, int idRental, double amount, String paymentDate, String method) {
        this.idPayment = idPayment;
        this.idRental = idRental;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.method = method;
    }


    public int getIdPayment() {
        return idPayment;
    }
    public void setIdPayment(int idPayment) {
        this.idPayment = idPayment;
    }
    public int getIdRental() {
        return idRental;
    }
    public void setIdRental(int idRental) {
        this.idRental = idRental;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public String getPaymentDate() {
        return paymentDate;
    }
    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
}
