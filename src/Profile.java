import Definitions.Car;
import Definitions.Rental;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Profile {

    public static Scene createProfileScene(Stage primaryStage, int userId) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        VBox contentBox = new VBox(30);
        contentBox.setPadding(new Insets(20));
        contentBox.setAlignment(Pos.TOP_CENTER);

        VBox userInfoBox = new VBox(10);
        userInfoBox.setAlignment(Pos.TOP_LEFT);
        userInfoBox.getStyleClass().add("user-info");

        Label idLabel = new Label("User ID: " + userId);
        Label usernameLabel = new Label();
        Label emailLabel = new Label();
        Label phoneLabel = new Label();
        Label locationLabel = new Label();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT username,email,phone,location FROM user WHERE id=?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                usernameLabel.setText("Username: " + rs.getString("username"));
                emailLabel.setText("Email: " + rs.getString("email"));
                phoneLabel.setText("Phone: " + rs.getString("phone"));
                locationLabel.setText("Location: " + rs.getString("location"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Button changeInfoBtn = new Button("Change Info");
        Button changePasswordBtn = new Button("Change Password");

        changeInfoBtn.setOnAction(e -> openEditStage(userId));
        changePasswordBtn.setOnAction(e -> openPasswordStage(userId));

        userInfoBox.getChildren().addAll(idLabel, usernameLabel, emailLabel, phoneLabel, locationLabel, changeInfoBtn, changePasswordBtn);

        TableView<Rental> rentalsTable = new TableView<>();
        rentalsTable.setPrefHeight(300);

        TableColumn<Rental, String> carCol = new TableColumn<>("Car");
        carCol.setCellValueFactory(data -> {
            Car car = getCarById(data.getValue().getCarId());
            return new SimpleStringProperty(
                    car != null ? car.getCompany() + " " + car.getType() : "Unknown Car"
            );
        });

        TableColumn<Rental, String> startCol = new TableColumn<>("Start Date");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        TableColumn<Rental, String> endCol = new TableColumn<>("End Date");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        TableColumn<Rental, String> priceCol = new TableColumn<>("Total Price");
        priceCol.setCellValueFactory(data -> {
            LocalDate start = LocalDate.parse(data.getValue().getStartDate());
            LocalDate end = LocalDate.parse(data.getValue().getEndDate());
            long days = ChronoUnit.DAYS.between(start, end);
            if (days <= 1)
                days = 1;
            Car car = getCarById(data.getValue().getCarId());
            double total = (car != null ? car.getPrice_per_day() : 0) * days;
            return new SimpleStringProperty(String.format("$%.2f", total));
        });

        TableColumn<Rental, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> {
            Car car = getCarById(data.getValue().getCarId());
            if (car != null) {
                if ("Available".equalsIgnoreCase(car.getStatus())) return new SimpleStringProperty("Returned");
                if ("Rented".equalsIgnoreCase(car.getStatus())) return new SimpleStringProperty("Rented");
                return new SimpleStringProperty(car.getStatus());
            }
            return new SimpleStringProperty("Unknown");
        });

        TableColumn<Rental, String> paymentCol = new TableColumn<>("Payment Method");
        paymentCol.setCellValueFactory(data -> {
            String method = paymentMethod(data.getValue().getIdRentals());
            return new SimpleStringProperty(method != null ? method : "N/A");
        });

        rentalsTable.getColumns().addAll(carCol, startCol, endCol, priceCol, statusCol, paymentCol);

        rentalsTable.getItems().clear();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id_rentals, CID, start_date, end_date, total_price " +
                             "FROM rentals WHERE UID=?")) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Rental rental = new Rental(
                        rs.getInt("id_rentals"),
                        userId,
                        rs.getInt("CID"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getDouble("total_price")
                );
                rentalsTable.getItems().add(rental);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        rentalsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        rentalsTable.getStyleClass().add("profile-table");


        Button returnBtn = new Button("Return Car");
        returnBtn.getStyleClass().add("rental-buttons");
        returnBtn.setOnAction(e->returnCar(primaryStage, userId, rentalsTable));

        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> {
            Stage oldStage = (Stage) backBtn.getScene().getWindow();
            Stage newStage = new Stage();
            UserInterface ui = new UserInterface(userId);
            try {
                ui.start(newStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            oldStage.close();
        });
        HBox topBar = new HBox(backBtn);
        topBar.setAlignment(Pos.TOP_LEFT);
        root.setTop(topBar);

        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.getChildren().addAll(userInfoBox, rentalsTable, returnBtn);

        root.setCenter(mainLayout);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(Profile.class.getResource("CSS/profile.css").toExternalForm());
        return scene;
    }

    private static void openEditStage(int userId) {
        Stage editStage = new Stage();
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("dialog-root");

        TextField emailField = new TextField();
        emailField.getStyleClass().add("text-field");
        TextField phoneField = new TextField();
        phoneField.getStyleClass().add("text-field");
        TextField locationField = new TextField();
        locationField.getStyleClass().add("text-field");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT email,phone,location FROM user WHERE id=?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone"));
                locationField.setText(rs.getString("location"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");

        Button submitBtn = new Button("Submit");
        submitBtn.setOnAction(e -> {
            String newEmail = emailField.getText().trim();
            String newPhone = phoneField.getText().trim();
            String newLocation = locationField.getText().trim();

            if (!newEmail.contains("@") || !newEmail.endsWith(".com")) {
                errorLabel.setText("Invalid email format");
                return;
            }
            if (!newPhone.matches("\\d{8,10}")) {
                errorLabel.setText("Phone must be 8-10 digits");
                return;
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE user SET email=?, phone=?, location=? WHERE id=?")) {
                stmt.setString(1, newEmail);
                stmt.setString(2, newPhone);
                stmt.setString(3, newLocation);
                stmt.setInt(4, userId);
                stmt.executeUpdate();
                errorLabel.getStyleClass().remove("error-label");
                errorLabel.getStyleClass().add("success-label");
                errorLabel.setText("Info updated successfully!");
            } catch (Exception ex) {
                errorLabel.setText("Error updating info: " + ex.getMessage());
            }
        });

        box.getChildren().addAll(new Label("Edit Info"), emailField, phoneField, locationField, submitBtn, errorLabel);

        editStage.setScene(new Scene(box, 400, 300));
        editStage.setTitle("Edit Info");
        editStage.show();
    }
    private static void openPasswordStage(int userId) {
        Stage passStage = new Stage();
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("dialog-root");

        PasswordField oldPassField = new PasswordField();
        oldPassField.setPromptText("Enter old password");
        oldPassField.getStyleClass().add("password-field");

        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("Enter new password");
        newPassField.getStyleClass().add("password-field");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");

        Button submitBtn = new Button("Change Password");
        submitBtn.setOnAction(e -> {
            String oldPass = oldPassField.getText().trim();
            String newPass = newPassField.getText();

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT password FROM user WHERE id=?")) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String currentPass = rs.getString("password").trim();
                    if (!currentPass.equals(oldPass)) {
                        errorLabel.setText("Old password incorrect");
                        return;
                    }
                }
            } catch (Exception ex) {
                errorLabel.setText("Error checking password: " + ex.getMessage());
                return;
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE user SET password=? WHERE id=?")) {
                stmt.setString(1, newPass);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
                errorLabel.getStyleClass().remove("error-label");
                errorLabel.getStyleClass().add("success-label");
                errorLabel.setText("Password updated successfully!");
            } catch (Exception ex) {
                errorLabel.setText("Error updating password: " + ex.getMessage());
            }
        });

        box.getChildren().addAll(new Label("Change Password"), oldPassField, newPassField, submitBtn, errorLabel);

        passStage.setScene(new Scene(box, 400, 250));
        passStage.setTitle("Change Password");
        passStage.show();
    }
    private static Car getCarById(int carId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT company, type, price_per_day, status FROM car WHERE id_car=?")) {
            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Car(
                        carId,
                        rs.getString("company"),
                        rs.getString("type"),
                        rs.getDouble("price_per_day"),
                        rs.getString("status")
                );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    private static String paymentMethod(int rentalId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT method FROM payment WHERE RID=?")) {
            stmt.setInt(1, rentalId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("method");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    private static void returnCar(Stage rentStage, int userID, TableView<Rental> rentalsTable) {
        Rental selected = rentalsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a rental first.");
            alert.showAndWait();
            return;
        }

        Car car = getCarById(selected.getCarId());
        if (car == null || "Available".equalsIgnoreCase(car.getStatus())) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "This car is already returned.");
            alert.showAndWait();
            return;
        }

        Stage feedbackStage = new Stage();
        feedbackStage.setTitle("Car Feedback");

        Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 3);
        ratingSpinner.setEditable(false);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Write your review here...");
        commentArea.setPrefRowCount(5);
        commentArea.setWrapText(true);
        commentArea.getStyleClass().add("text-area");

        Button submitFeedbackBtn = new Button("Submit Feedback");
        submitFeedbackBtn.getStyleClass().add("rental-buttons");

        Label rate = new Label ("Rate the car (1-5): ");
        Label review = new Label ("Your review: ");
        rate.getStyleClass().add("text-field");
        review.getStyleClass().add("text-field");
        VBox layout = new VBox(10,
                rate, ratingSpinner,
                review, commentArea,
                submitFeedbackBtn
        );
        layout.setPadding(new Insets(15));

        Scene scene = new Scene(layout, 350, 300);
        scene.getStylesheets().add(Profile.class.getResource("CSS/profile.css").toExternalForm());
        feedbackStage.setScene(scene);
        feedbackStage.show();

        submitFeedbackBtn.setOnAction(ev -> {
            int rating = ratingSpinner.getValue();
            String comment = commentArea.getText().trim();

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO feedback (UsID, CarID, rating, comment, date) VALUES (?, ?, ?, ?, ?)"
                );
                stmt.setInt(1, userID);
                stmt.setInt(2, selected.getCarId());
                stmt.setInt(3, rating);
                stmt.setString(4, comment);
                stmt.setString(5, LocalDate.now().toString());
                stmt.executeUpdate();

                PreparedStatement carStmt = conn.prepareStatement(
                        "UPDATE car SET status='Available' WHERE id_car=?"
                );
                carStmt.setInt(1, selected.getCarId());
                carStmt.executeUpdate();

                conn.commit();

                Alert success = new Alert(Alert.AlertType.INFORMATION, "Feedback submitted and car returned!");
                success.showAndWait();

                feedbackStage.close();
                rentStage.close();

                Stage newStage = new Stage();
                UserInterface ui = new UserInterface(userID);
                ui.start(newStage);

            } catch (Exception ex) {
                ex.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR, "Error saving feedback: " + ex.getMessage());
                error.showAndWait();
            }
        });
    }


}

