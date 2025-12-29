import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RentCar {

    public static Scene createRentScene(Stage primaryStage,
                                        int carID,
                                        String imgPath,
                                        String company,
                                        String type,
                                        double pricePerDay,
                                        int userID) {

        BorderPane root = new BorderPane();
        root.getStyleClass().add("rent-root");

        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> {
            Stage oldStage = (Stage) backBtn.getScene().getWindow();
            Stage newStage = new Stage();
            UserInterface ui = new UserInterface(userID);
            try {
                ui.start(newStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            oldStage.close();
        });
        BorderPane.setMargin(backBtn, new Insets(10));
        root.setTop(backBtn);


        HBox mainContent = new HBox();
        mainContent.setPadding(new Insets(30));
        mainContent.setSpacing(40);
        mainContent.setAlignment(Pos.CENTER_LEFT);
        mainContent.setPrefHeight(600);

        ImageView carImg = new ImageView();
        try {
            carImg.setImage(new Image(imgPath, true));
        } catch (Exception ex) {
            System.out.println("Image failed to load: " + ex.getMessage());
        }
        carImg.setFitWidth(650);
        carImg.setFitHeight(450);
        carImg.setPreserveRatio(false);
        VBox imageBox = new VBox(carImg);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        imageBox.setPrefWidth(700);

        VBox detailsBox = new VBox(20);
        detailsBox.setAlignment(Pos.CENTER);
        detailsBox.setPrefWidth(500);

        Label companyLabel = new Label("Company: " + company);
        Label typeLabel = new Label("Type: " + type);
        Label priceLabel = new Label("Price per day: $" + pricePerDay);
        Label ratingLabel = new Label();
        double avg = getAverageRating(carID);
        if (avg > 0) {
            ratingLabel.setText(String.format("Rating: %.1f / 5", avg));
        } else {
            ratingLabel.setText("No ratings yet");
        }

        companyLabel.getStyleClass().add("rent-label");
        typeLabel.getStyleClass().add("rent-label");
        priceLabel.getStyleClass().add("rent-label");
        ratingLabel.getStyleClass().add("rent-label");

        Button rentBtn = new Button("Rent Now");
        rentBtn.getStyleClass().add("rent-button");
        rentBtn.setOnAction(e_-> rent(primaryStage,carID, userID, pricePerDay));

        detailsBox.getChildren().addAll(companyLabel, typeLabel, priceLabel, ratingLabel, rentBtn);

        mainContent.getChildren().addAll(imageBox, detailsBox);



        Label reviewsTitle = new Label("Reviews");
        reviewsTitle.getStyleClass().add("reviews-title");

        VBox reviewsSection = loadReviews(carID);

        VBox general = new VBox(24, mainContent, reviewsTitle, reviewsSection);

        ScrollPane reviewsScroll = new ScrollPane(general);
        reviewsScroll.setFitToWidth(true);
        reviewsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root.setCenter(reviewsScroll);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(RentCar.class.getResource("CSS/rentCar.css").toExternalForm());
        return scene;
    }
    public static void rent(Stage carStage,int carID, int userID, double pricePerDay) {
        Stage rentStage = new Stage();
        rentStage.setTitle("Rent Car");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> {
            rentStage.close();
        });

        HBox topBar = new HBox(backBtn);
        topBar.setAlignment(Pos.TOP_LEFT);
        root.setTop(topBar);

        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        DatePicker endDatePicker = new DatePicker(LocalDate.now().plusDays(1));

        Label startLabel = new Label("Start Date:");
        Label endLabel = new Label("End Date:");

        HBox dateBox = new HBox(15, startLabel, startDatePicker, endLabel, endDatePicker);
        dateBox.setAlignment(Pos.CENTER);

        Label paymentLabel = new Label("Payment Method:");
        ComboBox<String> paymentMethodBox = new ComboBox<>();
        paymentMethodBox.getItems().addAll("Card", "Cash");
        paymentMethodBox.setValue("Cash");

        HBox paymentBox = new HBox(15, paymentLabel, paymentMethodBox);
        paymentBox.setAlignment(Pos.CENTER);

        Label totalPriceLabel = new Label("Total Price: $0.00");
        totalPriceLabel.getStyleClass().add("rent-label");

        Button submitBtn = new Button("Confirm Rental");
        submitBtn.getStyleClass().add("rent-button");
        submitBtn.setDisable(true);


        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();

            if (start != null && end != null && !end.isBefore(start)) {
                long days = ChronoUnit.DAYS.between(start, end);
                if (days < 1) days = 1;
                double total = days * pricePerDay;
                totalPriceLabel.setText(String.format("Total Price: $%.2f", total));
                submitBtn.setDisable(false);
            } else {
                totalPriceLabel.setText("Total Price: Invalid Dates");
                submitBtn.setDisable(true);
            }
        });

        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();

            if (start != null && end != null && !end.isBefore(start)) {
                long days = ChronoUnit.DAYS.between(start, end);
                if (days < 1) days = 1;
                double total = days * pricePerDay;
                totalPriceLabel.setText(String.format("Total Price: $%.2f", total));
                submitBtn.setDisable(false);
            } else {
                totalPriceLabel.setText("Total Price: Invalid Dates");
                submitBtn.setDisable(true);
            }
        });

        submitBtn.setOnAction(e -> {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            long days = ChronoUnit.DAYS.between(start, end);
            if (days < 1) days = 1;
            double total = days * pricePerDay;

            String paymentMethod = paymentMethodBox.getValue();

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                String insertRental = "INSERT INTO rentals (UID, CID, start_date, end_date, total_price) VALUES (?, ?, ?, ?, ?)";
                int rentalId = -1;
                try (PreparedStatement stmt = conn.prepareStatement(insertRental, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, userID);
                    stmt.setInt(2, carID);
                    stmt.setDate(3, java.sql.Date.valueOf(start));
                    stmt.setDate(4, java.sql.Date.valueOf(end));
                    stmt.setDouble(5, total);
                    stmt.executeUpdate();

                    ResultSet keys = stmt.getGeneratedKeys();
                    if (keys.next()) {
                        rentalId = keys.getInt(1);
                    }
                }

                String updateCar = "UPDATE car SET status = 'Rented' WHERE id_car = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateCar)) {
                    stmt.setInt(1, carID);
                    stmt.executeUpdate();
                }

                String insertPayment = "INSERT INTO payment (RID,amount,payment_date,method) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertPayment)) {
                    stmt.setInt(1, rentalId);
                    stmt.setDouble(2, total);
                    stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                    stmt.setString(4, paymentMethod);
                    stmt.executeUpdate();
                }

                conn.commit();

                Alert success = new Alert(Alert.AlertType.INFORMATION, "Rental confirmed successfully!");
                success.showAndWait();

                rentStage.close();
                carStage.close();

                Stage newStage = new Stage();
                UserInterface ui = new UserInterface(userID);
                ui.start(newStage);

            } catch (Exception ex) {
                ex.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR, "Error processing rental: " + ex.getMessage());
                error.showAndWait();
            }
        });
        content.getChildren().addAll(dateBox, paymentBox, totalPriceLabel, submitBtn);
        root.setCenter(content);
        root.getStyleClass().add("rent-root");

        Scene scene = new Scene(root, 600, 300);
        scene.getStylesheets().add(Profile.class.getResource("/CSS/rentcar.css").toExternalForm());
        rentStage.setScene(scene);
        rentStage.show();





    }
    public static double getAverageRating(int carId) {
        double avgRating = 0.0;

        String sql = "SELECT AVG(rating) AS avg_rating FROM feedback WHERE CarID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                avgRating = rs.getDouble("avg_rating");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return avgRating;
    }
    public static VBox loadReviews(int carId) {
        VBox reviewsBox = new VBox(16);
        reviewsBox.setPrefWidth(Double.MAX_VALUE);
        reviewsBox.setAlignment(Pos.TOP_LEFT);

        String sql = "SELECT u.username, f.rating, f.comment, f.date " +
                "FROM feedback f " +
                "JOIN user u ON f.UsID = u.id " +
                "WHERE f.CarID = ? ORDER BY f.date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                int rating = rs.getInt("rating");
                String comment = rs.getString("comment");
                String date = rs.getString("date");

                Label header = new Label(username + " - " + rating + "/5 - " + date);
                header.getStyleClass().add("review-header");

                Label body = new Label(comment);
                body.setWrapText(true);
                body.getStyleClass().add("review-body");

                VBox card = new VBox(8, header, body);
                card.getStyleClass().add("review-card");
                card.setPrefWidth(Double.MAX_VALUE);

                reviewsBox.getChildren().add(card);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reviewsBox;
    }

}