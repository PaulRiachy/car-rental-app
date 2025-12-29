import Definitions.Car;
import Definitions.Rental;
import Definitions.User;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class AdminInterface extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        HBox navBar = new HBox();
        navBar.setPadding(new Insets(10));
        navBar.setSpacing(20);
        Label navTitle = new Label("Admin Dashboard");
        navBar.getChildren().add(navTitle);
        root.setTop(navBar);

        VBox contentBox = new VBox(30);
        contentBox.setPadding(new Insets(20));
        contentBox.setAlignment(Pos.TOP_CENTER);

        TableView<User> usersTable = new TableView<>();

        TableColumn<User, Number> colUserId = new TableColumn<>("ID");
        colUserId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()));

        TableColumn<User, String> colUsername = new TableColumn<>("Username");
        colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));

        TableColumn<User, String> colPassword = new TableColumn<>("Password");
        colPassword.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPassword()));

        TableColumn<User, String> colRole = new TableColumn<>("Role");
        colRole.setCellValueFactory(data -> {
            int role = data.getValue().getRole();
            return new SimpleStringProperty(role == 1 ? "Admin" : "Client");
        });

        TableColumn<User, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));

        TableColumn<User, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

        TableColumn<User, String> colLocation = new TableColumn<>("Location");
        colLocation.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));

        usersTable.getColumns().addAll(colUserId, colUsername, colPassword, colRole, colPhone, colEmail, colLocation);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by username");
        searchField.setMaxWidth(250);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            usersTable.getItems().clear();
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM user WHERE username LIKE '%" + newVal + "%'")) {

                while (rs.next()) {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getInt("role"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("location")
                    );
                    usersTable.getItems().add(user);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        HBox userButtons = new HBox(10);
        Button btnEditUser = new Button("Edit User");
        Button btnDeleteUser = new Button("Delete User");
        btnEditUser.setOnAction(e -> {
            User selectedUser = usersTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                editUser(selectedUser,usersTable);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a user to edit.");
                alert.showAndWait();
            }
        });
        btnDeleteUser.setOnAction(e -> deleteUser(usersTable));
        btnEditUser.getStyleClass().add("button");
        btnDeleteUser.getStyleClass().add("button");

        Button exportUsersBtn = new Button("Export Users");
        exportUsersBtn.setOnAction(e -> {
            Stage stage = (Stage) exportUsersBtn.getScene().getWindow();
            exportUsersToCSV(usersTable, stage);
        });

        userButtons.setAlignment(Pos.CENTER);
        userButtons.getChildren().addAll(btnEditUser, btnDeleteUser,exportUsersBtn);

        Label u = new Label("Users");
        u.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        VBox usersSection = new VBox(10, u, searchField, usersTable, userButtons);


        TableView<Car> carsTable = new TableView<>();

        TableColumn<Car, Number> colCarId = new TableColumn<>("ID");
        colCarId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()));

        TableColumn<Car, String> colCompany = new TableColumn<>("Company");
        colCompany.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCompany()));

        TableColumn<Car, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));

        TableColumn<Car, String> colColor = new TableColumn<>("Color");
        colColor.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getColor()));

        TableColumn<Car, Number> colYear = new TableColumn<>("Year");
        colYear.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getYear()));

        TableColumn<Car, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        TableColumn<Car, Number> colPrice = new TableColumn<>("Price/Day");
        colPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice_per_day()));

        TableColumn<Car, String> colImg = new TableColumn<>("Image Path");
        colImg.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getImg_link()));

        TableColumn<Car, String> colImage = new TableColumn<>("Image");
        colImage.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getImg_link()));
        colImage.setCellFactory(tc -> new TableCell<Car, String>() {
            private final ImageView imageView = new ImageView();
            private final StackPane pane = new StackPane(imageView);

            {
                imageView.setFitWidth(150);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
                pane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String imgPath, boolean empty) {
                super.updateItem(imgPath, empty);
                if (empty || imgPath == null || imgPath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image img = new Image(imgPath, true);
                        imageView.setImage(img);
                        setGraphic(pane);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        carsTable.getColumns().addAll(colCarId, colCompany, colType, colColor, colYear, colStatus, colPrice, colImg, colImage);

        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        carsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        usersTable.setPrefHeight(600);
        usersTable.setPrefWidth(800);
        usersTable.setMaxWidth(Double.MAX_VALUE);
        usersTable.setMaxHeight(Double.MAX_VALUE);

        carsTable.setPrefHeight(600);
        carsTable.setPrefWidth(800);
        carsTable.setMaxWidth(Double.MAX_VALUE);
        carsTable.setMaxHeight(Double.MAX_VALUE);

        ComboBox<String> carFilter = new ComboBox<>();
        carFilter.getItems().addAll("All Cars", "Only Rented", "Only Unrented");
        carFilter.setValue("All Cars");

        carFilter.setOnAction(e -> {
            String selected = carFilter.getValue();
            carsTable.getItems().clear();

            String query = "SELECT * FROM car";
            if (selected.equals("Only Rented")) {
                query += " WHERE status = 'Rented'";
            } else if (selected.equals("Only Unrented")) {
                query += " WHERE status = 'Available'";
            }

            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    Car car = new Car(
                            rs.getInt("id_car"),
                            rs.getString("company"),
                            rs.getString("type"),
                            rs.getString("color"),
                            rs.getInt("year"),
                            rs.getString("status"),
                            rs.getDouble("price_per_day"),
                            rs.getString("img_link")
                    );
                    carsTable.getItems().add(car);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        HBox carButtons = new HBox(10);
        Button btnAddCar = new Button("Add Car");
        Button btnEditCar = new Button("Edit Car");
        Button btnDeleteCar = new Button("Delete Car");
        Button btnExportCars = new Button("Export Cars");



        btnAddCar.setOnAction(e -> addCar(carsTable));
        btnEditCar.setOnAction(e -> {
            Car selectedCar = carsTable.getSelectionModel().getSelectedItem();
            if (selectedCar != null) {
                editCar(selectedCar,carsTable);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a car to edit.");
                alert.showAndWait();
            }
        });
        btnDeleteCar.setOnAction(e -> deleteCar(carsTable));
        btnExportCars.setOnAction(e -> {
            Stage stage = (Stage) btnExportCars.getScene().getWindow();
            exportCarsToCSV(carsTable, stage);
        });
        carButtons.setAlignment(Pos.CENTER);
        carButtons.getChildren().addAll(btnAddCar, btnEditCar, btnDeleteCar,btnExportCars);

        Label c = new Label("Cars");
        c.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox carsSection = new VBox(10, c, carFilter, carsTable, carButtons);

        TableView<Rental> rentalsTable = new TableView<>();

        TableColumn<Rental, Integer> idCol = new TableColumn<>("Rental ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("IdRentals"));

        TableColumn<Rental, Integer> userCol = new TableColumn<>("User ID");
        userCol.setCellValueFactory(new PropertyValueFactory<>("UserId"));

        TableColumn<Rental, Integer> carCol = new TableColumn<>("Car ID");
        carCol.setCellValueFactory(new PropertyValueFactory<>("CarId"));

        TableColumn<Rental, String> startCol = new TableColumn<>("Start Date");
        startCol.setCellValueFactory(new PropertyValueFactory<>("StartDate"));

        TableColumn<Rental, String> endCol = new TableColumn<>("End Date");
        endCol.setCellValueFactory(new PropertyValueFactory<>("EndDate"));

        TableColumn<Rental, Double> priceCol = new TableColumn<>("Total Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("TotalPrice"));

        rentalsTable.getColumns().addAll(idCol, userCol, carCol, startCol, endCol, priceCol);


        rentalsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        rentalsTable.setPrefHeight(600);
        rentalsTable.setPrefWidth(800);
        rentalsTable.setMaxWidth(Double.MAX_VALUE);
        rentalsTable.setMaxHeight(Double.MAX_VALUE);

        contentBox.getChildren().addAll(usersSection, carsSection, rentalsTable);

        contentBox.setFillWidth(true);
        contentBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        contentBox.setPrefHeight(Region.USE_COMPUTED_SIZE);



        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("CSS/adminIF.css").toExternalForm());
        primaryStage.setTitle("Admin Interface");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();


        navBar.getStyleClass().add("nav-bar");
        navTitle.getStyleClass().add("nav-title");
        usersTable.getStyleClass().add("table-view");
        carsTable.getStyleClass().add("table-view");
        btnAddCar.getStyleClass().add("button");
        btnEditCar.getStyleClass().add("button");
        btnDeleteCar.getStyleClass().add("button");

        loadUsers(usersTable);
        loadCars(carsTable);
        loadRentals(rentalsTable);
    }

    private void loadUsers(TableView<User> usersTable) {
        usersTable.getItems().clear();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM user")) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getInt("role"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("location")
                );
                usersTable.getItems().add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCars(TableView<Car> carsTable) {
        carsTable.getItems().clear();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM car")) {

            while (rs.next()) {
                String rawImg = rs.getString("img_link");
                String imgPath = "";

                if (rawImg != null && !rawImg.isEmpty()) {
                    if (rawImg.startsWith("http")) {
                        imgPath = rawImg;
                    } else if (rawImg.startsWith("file:")) {
                        imgPath = rawImg;
                    } else {
                        imgPath = "file:/" + rawImg.replace("\\", "/");
                    }
                }

                Car car = new Car(
                        rs.getInt("id_car"),
                        rs.getString("company"),
                        rs.getString("type"),
                        rs.getString("color"),
                        rs.getInt("year"),
                        rs.getString("status"),
                        rs.getDouble("price_per_day"),
                        imgPath
                );
                carsTable.getItems().add(car);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void loadRentals(TableView<Rental> rentalsTable) {
        rentalsTable.getItems().clear();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM rentals")) {

            while (rs.next()) {
                Rental rental = new Rental(
                        rs.getInt("id_rentals"),
                        rs.getInt("UID"),
                        rs.getInt("CID"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getDouble("total_price")
                );
                rentalsTable.getItems().add(rental);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void editUser(User selected, TableView<User> userTableView) {
        if (selected == null) {
            return;
        }

        Stage editStage = new Stage();
        editStage.setTitle("Edit User");

        VBox form = new VBox(20);
        form.setPadding(new Insets(15));
        form.setAlignment(Pos.CENTER);

        ComboBox<String> role = new ComboBox<>();
        role.getItems().addAll("User", "Admin");
        role.setPromptText("Select Role");
        if(selected.getRole() == 0){
            role.setValue("User");
        }
        else{
            role.setValue("Admin");
        }

        TextField username = new TextField(selected.getUsername());
        username.setPromptText("Username");
        username.getStyleClass().add("text-field");

        TextField password = new TextField(selected.getPassword());
        password.setPromptText("Password");
        password.getStyleClass().add("text-field");

        TextField phone = new TextField(String.valueOf(selected.getPhone()));
        phone.setPromptText("Phone");
        phone.getStyleClass().add("text-field");

        TextField email = new TextField(selected.getEmail());
        email.setPromptText("Email");
        email.getStyleClass().add("text-field");

        TextField location = new TextField(selected.getLocation());
        location.setPromptText("Location");
        location.getStyleClass().add("text-field");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        Button saveBtn = new Button("Update");
        saveBtn.getStyleClass().add("button");
        saveBtn.setOnAction(e -> {
            try {
                String roleT = role.getValue();
                String userT = username.getText().trim();
                String passwordT = password.getText();
                String emailT = email.getText().trim();
                String locationT = location.getText().trim();

                if (roleT == null || userT.isEmpty() || passwordT.isEmpty() ||
                        emailT.isEmpty() || locationT.isEmpty()) {
                    throw new Exception("All fields are required.");
                }

                int phoneT = Integer.parseInt(phone.getText().trim());
                if (phoneT < 10000000 || phoneT > 1000000000) {
                    throw new Exception("Phone nb must be between 8 and 10 digits");
                }
                int rolenb;
                if(roleT.equals("User")){
                    rolenb = 0;
                }
                else{
                    rolenb = 1;
                }
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "UPDATE user SET username=?, password=?, role=?, phone=?, email=?, location=? WHERE id=?")) {
                    ps.setString(1, userT);
                    ps.setString(2, passwordT);
                    ps.setInt(3, rolenb);
                    ps.setInt(4, phoneT);
                    ps.setString(5, emailT);
                    ps.setString(6, locationT);
                    ps.setInt(7, selected.getId());
                    ps.executeUpdate();

                    errorLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    errorLabel.setText("User updated successfully!");
                    loadUsers(userTableView);
                    editStage.close();
                }

            } catch (NumberFormatException nfe) {
                errorLabel.setText("Phone must be numeric.");
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        form.getChildren().addAll(role,username,password,phone,email,location,saveBtn);

        form.getStyleClass().add("root");

        Scene scene = new Scene(form, 350, 400);
        scene.getStylesheets().add(getClass().getResource("CSS/adminIF.css").toExternalForm());
        editStage.setScene(scene);
        editStage.show();
    }
    private void deleteUser(TableView<User> userTableView) {
        User selected = userTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a user to delete.");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete user ID " + selected.getId() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                String deletePayments = "DELETE FROM payment WHERE RID IN (SELECT id_rentals FROM rentals WHERE UID=?)";
                try (PreparedStatement ps = conn.prepareStatement(deletePayments)) {
                    ps.setInt(1, selected.getId());
                    ps.executeUpdate();
                }

                String deleteRentals = "DELETE FROM rentals WHERE UID=?";
                try (PreparedStatement ps = conn.prepareStatement(deleteRentals)) {
                    ps.setInt(1, selected.getId());
                    ps.executeUpdate();
                }

                String deleteFeedback = "DELETE FROM feedback WHERE UsID=?";
                try (PreparedStatement ps = conn.prepareStatement(deleteFeedback)) {
                    ps.setInt(1, selected.getId());
                    ps.executeUpdate();
                }

                String deleteUser = "DELETE FROM user WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(deleteUser)) {
                    ps.setInt(1, selected.getId());
                    ps.executeUpdate();
                }

                conn.commit();

                loadUsers(userTableView);
                Alert success = new Alert(Alert.AlertType.INFORMATION, "User and related feedback deleted successfully!");
                success.showAndWait();

            } catch (SQLException e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR, "Error deleting user: " + e.getMessage());
                error.showAndWait();
            }
        }
    }


    private void addCar(TableView<Car> carsTable) {
        Stage addStage = new Stage();
        addStage.setTitle("Add New Car");

        VBox form = new VBox(10);
        form.setPadding(new Insets(15));
        form.setAlignment(Pos.CENTER);

        ComboBox<String> companyBox = new ComboBox<>();
        companyBox.getItems().addAll("Toyota", "BMW", "Mercedes");
        companyBox.setPromptText("Select Company");

        TextField typeField = new TextField();
        typeField.setPromptText("Type");
        typeField.getStyleClass().add("text-field");

        TextField colorField = new TextField();
        colorField.setPromptText("Color");
        colorField.getStyleClass().add("text-field");

        TextField yearField = new TextField();
        yearField.setPromptText("Year");
        yearField.getStyleClass().add("text-field");

        TextField priceField = new TextField();
        priceField.setPromptText("Price per day");
        priceField.getStyleClass().add("text-field");


        TextField imgField = new TextField();
        imgField.setPromptText("Image path (URL or file)");
        imgField.getStyleClass().add("text-field");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("button");
        saveBtn.setOnAction(e -> {
            try {
                String company = companyBox.getValue();
                String type = typeField.getText().trim();
                String color = colorField.getText().trim();
                String yearStr = yearField.getText().trim();
                String priceStr = priceField.getText().trim();
                String img = imgField.getText().trim();

                if (company == null || type.isEmpty() || color.isEmpty() ||
                        yearStr.isEmpty() || priceStr.isEmpty()) {
                    throw new Exception("All fields are required.");
                }

                int year = Integer.parseInt(yearStr);
                if (year < 1900 || year > 2025) {
                    throw new Exception("Year must be between 1900 and 2025.");
                }

                double price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    throw new Exception("Price must be positive.");
                }

                String status = "Available";

                try (Connection conn = DBConnection.getConnection();
                     Statement stmt = conn.createStatement()) {
                    String sql = String.format(
                            "INSERT INTO car (company, type, color, year, status, price_per_day, img_link) " +
                                    "VALUES ('%s','%s','%s',%d,'%s',%.2f,'%s')",
                            company, type, color, year, status, price, img
                    );
                    stmt.executeUpdate(sql);
                    errorLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    errorLabel.setText("Car added successfully!");
                    loadCars(carsTable);
                    addStage.close();
                }

            } catch (NumberFormatException nfe) {
                errorLabel.setText("Year and Price must be numeric.");
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        form.getChildren().addAll(companyBox, typeField, colorField, yearField,
                priceField, imgField, errorLabel, saveBtn);

        form.getStyleClass().add("root");

        Scene scene = new Scene(form, 350, 400);
        scene.getStylesheets().add(getClass().getResource("CSS/adminIF.css").toExternalForm());
        addStage.setScene(scene);
        addStage.show();
    }
    private void editCar(Car selectedCar, TableView<Car> carsTable) {
        if (selectedCar == null) {
            return;
        }

        Stage editStage = new Stage();
        editStage.setTitle("Edit Car");

        VBox form = new VBox(10);
        form.setPadding(new Insets(15));
        form.setAlignment(Pos.CENTER);

        ComboBox<String> companyBox = new ComboBox<>();
        companyBox.getItems().addAll("Toyota", "BMW", "Mercedes");
        companyBox.setPromptText("Select Company");
        companyBox.setValue(selectedCar.getCompany());

        TextField typeField = new TextField(selectedCar.getType());
        typeField.setPromptText("Type");
        typeField.getStyleClass().add("text-field");

        TextField colorField = new TextField(selectedCar.getColor());
        colorField.setPromptText("Color");
        colorField.getStyleClass().add("text-field");

        TextField yearField = new TextField(String.valueOf(selectedCar.getYear()));
        yearField.setPromptText("Year");
        yearField.getStyleClass().add("text-field");

        TextField priceField = new TextField(String.valueOf(selectedCar.getPrice_per_day()));
        priceField.setPromptText("Price per day");
        priceField.getStyleClass().add("text-field");

        TextField imgField = new TextField(selectedCar.getImg_link());
        imgField.setPromptText("Image path (URL or file)");
        imgField.getStyleClass().add("text-field");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        Button saveBtn = new Button("Update");
        saveBtn.getStyleClass().add("button");
        saveBtn.setOnAction(e -> {
            try {
                String company = companyBox.getValue();
                String type = typeField.getText().trim();
                String color = colorField.getText().trim();
                String yearStr = yearField.getText().trim();
                String priceStr = priceField.getText().trim();
                String img = imgField.getText().trim();

                if (company == null || type.isEmpty() || color.isEmpty() ||
                        yearStr.isEmpty() || priceStr.isEmpty()) {
                    throw new Exception("All fields are required.");
                }

                int year = Integer.parseInt(yearStr);
                if (year < 1900 || year > 2025) {
                    throw new Exception("Year must be between 1900 and 2025.");
                }

                double price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    throw new Exception("Price must be positive.");
                }

                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "UPDATE car SET company=?, type=?, color=?, year=?, price_per_day=?, img_link=? WHERE id_car=?")) {
                    ps.setString(1, company);
                    ps.setString(2, type);
                    ps.setString(3, color);
                    ps.setInt(4, year);
                    ps.setDouble(5, price);
                    ps.setString(6, img);
                    ps.setInt(7, selectedCar.getId());
                    ps.executeUpdate();

                    errorLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    errorLabel.setText("Car updated successfully!");
                    loadCars(carsTable);
                    editStage.close();
                }

            } catch (NumberFormatException nfe) {
                errorLabel.setText("Year and Price must be numeric.");
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        form.getChildren().addAll(companyBox, typeField, colorField, yearField,
                priceField, imgField, errorLabel, saveBtn);

        form.getStyleClass().add("root");

        Scene scene = new Scene(form, 350, 400);
        scene.getStylesheets().add(getClass().getResource("CSS/adminIF.css").toExternalForm());
        editStage.setScene(scene);
        editStage.show();
    }
    private void deleteCar(TableView<Car> carsTable) {
        Car selectedCar = carsTable.getSelectionModel().getSelectedItem();
        if (selectedCar == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a car to delete.");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete car ID " + selectedCar.getId() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                String deletePayments = "DELETE FROM payment WHERE RID IN (SELECT id_rentals FROM rentals WHERE CID=?)";
                try (PreparedStatement ps = conn.prepareStatement(deletePayments)) {
                    ps.setInt(1, selectedCar.getId());
                    ps.executeUpdate();
                }

                String deleteRentals = "DELETE FROM rentals WHERE CID=?";
                try (PreparedStatement ps = conn.prepareStatement(deleteRentals)) {
                    ps.setInt(1, selectedCar.getId());
                    ps.executeUpdate();
                }

                String deleteFeedback = "DELETE FROM feedback WHERE CarID=?";
                try (PreparedStatement ps = conn.prepareStatement(deleteFeedback)) {
                    ps.setInt(1, selectedCar.getId());
                    ps.executeUpdate();
                }

                String deleteCar = "DELETE FROM car WHERE id_car=?";
                try (PreparedStatement ps = conn.prepareStatement(deleteCar)) {
                    ps.setInt(1, selectedCar.getId());
                    ps.executeUpdate();
                }

                conn.commit();

                loadCars(carsTable);
                Alert success = new Alert(Alert.AlertType.INFORMATION, "Car and related records deleted successfully!");
                success.showAndWait();

            } catch (SQLException e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR, "Error deleting car: " + e.getMessage());
                error.showAndWait();
            }
        }
    }

    private void exportCarsToCSV(TableView<Car> carsTable, Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Cars CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("cars_export.csv");

        File file = fileChooser.showSaveDialog(parentStage);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("ID,Company,Type,Color,Year,Status,PricePerDay");
                writer.newLine();

                for (Car car : carsTable.getItems()) {
                    writer.write(car.getId() + "," +
                            car.getCompany() + "," +
                            car.getType() + "," +
                            car.getColor() + "," +
                            car.getYear() + "," +
                            car.getStatus() + "," +
                            car.getPrice_per_day());
                    writer.newLine();
                }

                writer.flush();
                Alert success = new Alert(Alert.AlertType.INFORMATION,
                        "Cars exported successfully to:\n" + file.getAbsolutePath());
                success.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR,
                        "Error exporting cars: " + e.getMessage());
                error.showAndWait();
            }
        }
    }
    private void exportUsersToCSV(TableView<User> usersTable, Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Users CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("users_export.csv");

        File file = fileChooser.showSaveDialog(parentStage);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("ID,Username,Password,Role,Phone,Email,Location");
                writer.newLine();

                for (User user : usersTable.getItems()) {
                    writer.write(user.getId() + "," +
                            user.getUsername() + "," +
                            user.getPassword() + "," +
                            user.getRole() + "," +
                            user.getPhone() + "," +
                            user.getEmail() + "," +
                            user.getLocation());
                    writer.newLine();
                }

                writer.flush();
                Alert success = new Alert(Alert.AlertType.INFORMATION,
                        "Users exported successfully to:\n" + file.getAbsolutePath());
                success.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR,
                        "Error exporting users: " + e.getMessage());
                error.showAndWait();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}