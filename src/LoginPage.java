import Definitions.User;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;


public class LoginPage extends Application {
    private TextField username;
    private PasswordField password;
    @Override
    public void start(Stage primaryStage) {
        if (!DBConnection.testConnection()) {
            System.err.println("Cannot connect to database. Please check settings.");
        }

        Label titleLabel = new Label("Car Management System");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        username = new TextField();
        username.setPromptText("Username");
        username.getStyleClass().add("text-field");
        username.setPrefWidth(250);
        username.setMaxWidth(300);


        password = new PasswordField();
        password.setPromptText("Password");
        password.getStyleClass().add("password-field");
        password.setPrefWidth(250);
        password.setMaxWidth(300);


        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(250);



        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("button");
        loginButton.setOnAction(e -> handleLogin(errorLabel, primaryStage));



        Text AccText = new Text("Don't have an account yet? ");
        AccText.setStyle("-fx-fill: #ffffff;");

        Text signUpT = new Text("Sign Up");
        signUpT.setStyle("-fx-fill: #3498db; -fx-underline: true; -fx-cursor: hand;");

        TextFlow signup = new TextFlow(AccText, signUpT);
        signup.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        signup.setOnMouseClicked(e -> {
            Signup signupPage = new Signup();
            Stage signupStage = new Stage();
            try {
                signupPage.start(signupStage);
            } catch (Exception ex) {
                System.err.println("Couldn't open page: " + ex.getMessage());
            }
            primaryStage.close();
        });


        VBox vbox = new VBox(20, titleLabel, username, password, loginButton, signup);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-padding: 30; -fx-background-color: #000000;");


        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);


        VBox card = new VBox(20, titleLabel, username, password, errorLabel, loginButton, signup);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-padding: 30; " +
                        "-fx-background-color: #1c1c1c; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-radius: 15; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-width: 1;"
        );
        card.setPrefWidth(320);
        card.setMaxWidth(320);
        root.getChildren().add(card);

        Image bgImage = new Image("file:src/Images/login-signupC.jpg");
        BackgroundImage backgroundImage = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        root.setBackground(new Background(backgroundImage));


        Scene scene = new Scene(root, 500, 350);
        scene.getStylesheets().add(getClass().getResource("CSS/login-signup.css").toExternalForm());
        primaryStage.setTitle("Login");
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void handleLogin(Label errorLabel, Stage loginStage) {
        try {
            String enteredUsername = username.getText().trim();
            String enteredPassword = password.getText();

            errorLabel.setText("");

            if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                throw new Exception("Username and password are required.");
            }

            User loggedInUser = UserCheck.validateLogin(enteredUsername, enteredPassword);

            if (loggedInUser != null) {
                errorLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
                errorLabel.setText("Login successful! Welcome " + loggedInUser.getUsername());

                if (loggedInUser.getRole() == 1) {
                    AdminInterface AI = new AdminInterface();
                    Stage AiStage = new Stage();
                    AI.start(AiStage);

                    loginStage.close();
                } else {
                    int userID = loggedInUser.getId();
                    UserInterface UI = new UserInterface(userID);
                    Stage UiStage = new Stage();
                    UI.start(UiStage);

                    loginStage.close();
                }

            } else {
                throw new Exception("Invalid username or password.");
            }

        } catch (Exception ex) {
            errorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
            errorLabel.setText("Login failed: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}