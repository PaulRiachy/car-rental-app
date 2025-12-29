import Definitions.Car;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.*;

public class UserInterface extends Application {
    private final int userID;
    public UserInterface(int userID) {
        this.userID = userID;
    }

    private final GridPane carGrid = new GridPane();
    private ScrollPane scrollPane;
    private VBox centerLayout;
    private Scene scene;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        HBox navBar = new HBox();
        navBar.setPadding(new Insets(15));
        navBar.getStyleClass().add("nav-bar");
        navBar.setAlignment(Pos.CENTER);

        Label title = new Label("Car Rental");
        title.getStyleClass().add("nav-title");

        StackPane profileCircle = new StackPane();
        profileCircle.getStyleClass().add("profile-circle");

        ImageView profileImg = new ImageView("file:src/Images/profile.jpg");
        profileImg.setFitWidth(40);
        profileImg.setFitHeight(40);
        profileImg.setPreserveRatio(true);
        profileImg.setClip(new Circle(20, 20, 20));

        profileCircle.getChildren().add(profileImg);
        profileCircle.setOnMouseClicked(e -> {
            Stage profileStage = new Stage();
            Scene profileScene = Profile.createProfileScene(profileStage, userID);
            profileStage.setScene(profileScene);
            profileStage.setTitle("User Profile");
            profileStage.setMaximized(true);
            profileStage.show();

            Stage currentStage = (Stage) profileCircle.getScene().getWindow();
            currentStage.close();
        });

        Button modeToggle = new Button("Light Mode");
        modeToggle.getStyleClass().add("mode-toggle");
        modeToggle.setOnAction(e -> {
            if (scene.getStylesheets().contains(getClass().getResource("CSS/userIF.css").toExternalForm())) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource("CSS/userIF-light.css").toExternalForm());
                modeToggle.setText("Dark Mode");
            } else {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource("CSS/userIF.css").toExternalForm());
                modeToggle.setText("Light Mode");
            }
        });

        HBox rightControls = new HBox(10, modeToggle,profileCircle);
        rightControls.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        navBar.getChildren().addAll(title, spacer, rightControls);
        root.setTop(navBar);
        StackPane hero = new StackPane();
        hero.getStyleClass().add("hero");

        ImageView heroImage = new ImageView("file:src/Images/login-signupC.jpg");
        heroImage.setFitWidth(1510);
        heroImage.setFitHeight(700);
        heroImage.setPreserveRatio(false);
        heroImage.getStyleClass().add("hero-image");

        VBox heroOverlay = new VBox(15);
        heroOverlay.setAlignment(Pos.CENTER);

        Label heroText = new Label("Start renting a car now!");
        heroText.getStyleClass().add("hero-text");

        Button rentBtn = new Button("Rent Now");
        rentBtn.getStyleClass().add("button");
        rentBtn.setOnAction(e -> smoothScrollTo(carGrid));

        heroOverlay.getChildren().addAll(heroText, rentBtn);
        hero.getChildren().addAll(heroImage, heroOverlay);

        HBox searchSection = new HBox(15);
        searchSection.setAlignment(Pos.CENTER);
        searchSection.setPadding(new Insets(20));
        searchSection.getStyleClass().add("search");

        TextField searchField = new TextField();
        searchField.setPromptText("Search by text");

        ComboBox<String> companyFilter = new ComboBox<>();
        companyFilter.getItems().addAll("All","Toyota", "BMW", "Mercedes");
        companyFilter.setPromptText("Filter by company");

        ComboBox<String> colorFilter = new ComboBox<>();
        colorFilter.getItems().addAll("Black", "White", "Red","Grey","Blue","Orange","Yellow");
        colorFilter.setPromptText("Filter by color");

        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filterCars(newVal, companyFilter.getValue(), colorFilter.getValue()));

        companyFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                filterCars(searchField.getText(), newVal, colorFilter.getValue()));

        colorFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                filterCars(searchField.getText(), companyFilter.getValue(), newVal));

        searchSection.getChildren().addAll(searchField, companyFilter, colorFilter);

        carGrid.setPadding(new Insets(30));
        carGrid.setHgap(30);
        carGrid.setVgap(30);
        carGrid.setAlignment(Pos.TOP_CENTER);
        carGrid.getStyleClass().add("car-grid");

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();
        col1.setPercentWidth(33);
        col2.setPercentWidth(33);
        col3.setPercentWidth(33);
        carGrid.getColumnConstraints().addAll(col1, col2, col3);

        loadCars();

        centerLayout = new VBox(30, hero, searchSection, carGrid);
        centerLayout.setAlignment(Pos.TOP_CENTER);
        centerLayout.getStyleClass().add("content");

        scrollPane = new ScrollPane(centerLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setPannable(true);

        root.setCenter(scrollPane);

        scene = new Scene(root, 1200, 900);
        scene.getStylesheets().add(getClass().getResource("CSS/userIF.css").toExternalForm());

        primaryStage.setTitle("Car Rental UI");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }



    private void loadCars() {
        carGrid.getChildren().clear();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id_car,company, type, img_link, price_per_day " +
                             "FROM car WHERE status = 'Available'")) {

            int index = 0;
            while (rs.next()) {
                String rawImg = rs.getString("img_link");
                String imgPath = (rawImg != null && !rawImg.isEmpty())
                        ? (rawImg.startsWith("http") || rawImg.startsWith("file:") ? rawImg : "file:/" + rawImg.replace("\\", "/"))
                        : "";
                int carID = rs.getInt("id_car");
                String company = rs.getString("company");
                String type = rs.getString("type");
                double price = rs.getDouble("price_per_day");

                String carName = company + " " + type;

                VBox card = new VBox(12);
                card.setAlignment(Pos.CENTER);
                card.getStyleClass().add("car-card");
                card.setMaxWidth(Double.MAX_VALUE);
                GridPane.setFillWidth(card, true);

                ImageView carImg = new ImageView(imgPath);
                carImg.setFitWidth(320);
                carImg.setFitHeight(180);
                carImg.setPreserveRatio(false);

                Label nameLabel = new Label(carName);
                nameLabel.getStyleClass().add("car-name");

                Label priceLabel = new Label("$" + price + " / day");
                priceLabel.getStyleClass().add("price-label");

                Label ratingLabel = new Label();
                ratingLabel.getStyleClass().add("price-label");
                double avg = getAverageRating(carID);
                if (avg > 0) {
                    ratingLabel.setText(String.format("Rating: %.1f / 5", avg));
                } else {
                    ratingLabel.setText("No ratings yet");
                }

                Button rentBtn = new Button("Rent");
                rentBtn.getStyleClass().add("button");

                rentBtn.setOnAction(e -> {
                    Stage oldStage = (Stage) rentBtn.getScene().getWindow();

                    Stage newStage = new Stage();
                    Scene rentScene = RentCar.createRentScene(
                            newStage,
                            carID,
                            imgPath,
                            company,
                            type,
                            price,
                            userID
                    );
                    newStage.setScene(rentScene);
                    newStage.setTitle("Rent Car");
                    newStage.setMaximized(true);
                    newStage.show();
                    oldStage.close();
                });

                card.getChildren().addAll(carImg, nameLabel, priceLabel,ratingLabel,rentBtn);
                carGrid.add(card, index % 3, index / 3);
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void filterCars(String searchText, String company, String color) {
        carGrid.getChildren().clear();

        String sql = "SELECT id_car, company, type, color, year, status, img_link, price_per_day " +
                "FROM car WHERE status = 'Available'";

        if (searchText != null && !searchText.isEmpty()) {
            sql += " AND type LIKE ?";
        }
        if (company != null && !company.equals("All")) {
            sql += " AND company = ?";
        }
        if (color != null && !color.isEmpty()) {
            sql += " AND color = ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (searchText != null && !searchText.isEmpty()) {
                stmt.setString(paramIndex++, "%" + searchText + "%"); // search by model/type
            }
            if (company != null && !company.equals("All")) {
                stmt.setString(paramIndex++, company);
            }
            if (color != null && !color.isEmpty()) {
                stmt.setString(paramIndex++, color);
            }

            ResultSet rs = stmt.executeQuery();
            int index = 0;

            while (rs.next()) {
                String rawImg = rs.getString("img_link");
                String imgPath = (rawImg != null && !rawImg.isEmpty())
                        ? (rawImg.startsWith("http") || rawImg.startsWith("file:") ? rawImg : "file:/" + rawImg.replace("\\", "/"))
                        : "";

                int carID = rs.getInt("id_car");
                String companyName = rs.getString("company");
                String type = rs.getString("type");
                double price = rs.getDouble("price_per_day");

                String carName = companyName + " " + type;

                VBox card = new VBox(12);
                card.setAlignment(Pos.CENTER);
                card.getStyleClass().add("car-card");
                card.setMaxWidth(Double.MAX_VALUE);
                GridPane.setFillWidth(card, true);

                ImageView carImg = new ImageView(imgPath);
                carImg.setFitWidth(320);
                carImg.setFitHeight(180);
                carImg.setPreserveRatio(false);

                Label nameLabel = new Label(carName);
                nameLabel.getStyleClass().add("car-name");

                Label priceLabel = new Label("$" + price + " / day");
                priceLabel.getStyleClass().add("price-label");

                Label ratingLabel = new Label();
                ratingLabel.getStyleClass().add("price-label");
                double avg = getAverageRating(carID);
                if (avg > 0) {
                    ratingLabel.setText(String.format("Rating: %.1f / 5", avg));
                } else {
                    ratingLabel.setText("No ratings yet");
                }

                Button rentBtn = new Button("Rent");
                rentBtn.getStyleClass().add("button");

                rentBtn.setOnAction(e -> {
                    Stage oldStage = (Stage) rentBtn.getScene().getWindow();
                    Stage newStage = new Stage();
                    Scene rentScene = RentCar.createRentScene(
                            newStage,
                            carID,
                            imgPath,
                            companyName,
                            type,
                            price,
                            userID
                    );
                    newStage.setScene(rentScene);
                    newStage.setTitle("Rent Car");
                    newStage.setMaximized(true);
                    newStage.show();
                    oldStage.close();
                });

                card.getChildren().addAll(carImg, nameLabel, priceLabel, ratingLabel ,rentBtn);
                carGrid.add(card, index % 3, index / 3);
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void smoothScrollTo(javafx.scene.Node target) {
        if (scrollPane == null || centerLayout == null || target == null) return;

        double contentHeight = centerLayout.getBoundsInLocal().getHeight();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double y = target.getBoundsInParent().getMinY();

        double max = Math.max(contentHeight - viewportHeight, 1);
        double vTarget = Math.min(y / max, 1.0);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(scrollPane.vvalueProperty(), scrollPane.getVvalue())),
                new KeyFrame(Duration.millis(400), new javafx.animation.KeyValue(scrollPane.vvalueProperty(), vTarget))
        );
        timeline.play();
    }
    private double getAverageRating(int carId) {
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


    public static void main(String[] args) {
        launch(args);
    }
}