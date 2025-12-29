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

public class Signup extends Application {
    private TextField username;
    private PasswordField password;
    private PasswordField confirmPassword;
    private TextField phoneNumber;
    private TextField emailField;
    private TextField locationField;
    private Label errorLabel;
    private VBox root;
    @Override
    public void start(Stage primaryStage) {



        Label titleLabel = new Label("Create Account");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");


        username = new TextField();
        username.setPromptText("Username");
        username.getStyleClass().add("text-field");
        username.setPrefWidth(250);

        emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-background-color: #262626; -fx-text-fill: #ffffff; "
                + "-fx-prompt-text-fill: #aaaaaa; -fx-border-color: #333333;");
        emailField.setPrefWidth(250);

        password = new PasswordField();
        password.setPromptText("Password");
        password.getStyleClass().add("password-field");
        password.setPrefWidth(250);


        confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm Password");
        password.getStyleClass().add("password-field");
        confirmPassword.setPrefWidth(250);

        phoneNumber = new TextField();
        phoneNumber.setPromptText("Phone Number");
        phoneNumber.getStyleClass().add("text-field");
        phoneNumber.setPrefWidth(250);

        locationField = new TextField();
        locationField.setPromptText("Location");
        locationField.getStyleClass().add("text-field");
        locationField.setPrefWidth(250);


        Button signupButton = new Button("Sign Up");
        signupButton.getStyleClass().add("button");
        Text AccText = new Text("Already have an account? ");
        AccText.setStyle("-fx-fill: #ffffff;");

        Text loginT = new Text("Log In!");
        loginT.setStyle("-fx-fill: #3498db; -fx-underline: true; -fx-cursor: hand;");

        TextFlow log = new TextFlow(AccText, loginT);
        log.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        log.setOnMouseClicked(e -> {
            LoginPage lp = new LoginPage();
            Stage loginStage = new Stage();
            try {
                lp.start(loginStage);
            } catch (Exception ex) {
                System.err.println("Couldn't open page: " + ex.getMessage());
            }
            primaryStage.close();
        });

        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);

        VBox card = new VBox(20, titleLabel, username, emailField,password, confirmPassword,phoneNumber, locationField,errorLabel ,signupButton,log);
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

        root = new VBox(card);
        root.setAlignment(Pos.CENTER);


        signupButton.setOnAction(e -> handleSignup());


        Image bgImage = new Image("file:src/Images/login-signupC.jpg");
        BackgroundImage backgroundImage = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        root.setBackground(new Background(backgroundImage));


        Scene scene = new Scene(root, 500, 400);
        scene.getStylesheets().add(getClass().getResource("CSS/login-signup.css").toExternalForm());
        primaryStage.setTitle("Sign Up");
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private void handleSignup() {
        try {
            String user = username.getText().trim();
            String pass = password.getText();
            String confirm = confirmPassword.getText();
            String phone = phoneNumber.getText().trim();
            String email = emailField.getText().trim();
            String location = locationField.getText().trim();

            errorLabel.setText("");

            if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty() || phone.isEmpty()) {
                throw new Exception("All fields are required.");
            }
            if (pass.length() < 6) {
                throw new Exception("Password must be at least 6 characters.");
            }
            if (!pass.equals(confirm)) {
                throw new Exception("Passwords do not match.");
            }
            if (!phone.matches("\\d{8,15}")) {
                throw new Exception("Phone number must be digits only (8–15 characters).");
            }
            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
                throw new Exception("Invalid email format.");
            }

            if (UserCheck.usernameExists(user)) {
                throw new Exception("Username already exists.");
            }

            User newUser = new User(0, user, pass, 0, phone, email, location);

            if (UserCheck.insertUser(newUser)) {
                errorLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
                errorLabel.setText("Signup successful! Redirecting to login...");

                javafx.animation.PauseTransition pause =
                        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                pause.setOnFinished(ev -> {
                    try {
                        LoginPage loginPage = new LoginPage();
                        Stage stage = (Stage) root.getScene().getWindow();
                        stage.setMaximized(true);
                        loginPage.start(stage);
                    } catch (Exception ex) {
                        System.err.println("Redirect failed: " + ex.getMessage());
                    }
                });
                pause.play();
            } else {
                throw new Exception("Failed to insert user.");
            }

        } catch (Exception ex) {
            errorLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
            errorLabel.setText(ex.getMessage());
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

}

