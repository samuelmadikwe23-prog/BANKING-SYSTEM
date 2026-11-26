package view;

import controller.LoginController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.User;

import java.util.function.Consumer;

public class LoginView {
    private final LoginController controller = new LoginController();
    private final VBox root = new VBox(12);

    public LoginView(Consumer<User> onSuccess) {
        Label title = new Label("Banking System");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Button loginBtn = new Button("Log in");
        loginBtn.setDefaultButton(true);

        Label message = new Label();
        message.setStyle("-fx-text-fill: #b00020;");

        loginBtn.setOnAction(e -> {
            try {
                User u = controller.login(username.getText().trim(), password.getText());
                onSuccess.accept(u);
            } catch (IllegalArgumentException ex) {
                message.setText(ex.getMessage());
            }
        });

        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(title, username, password, loginBtn, message);
    }

    public Parent getRoot() {
        return root;
    }
}