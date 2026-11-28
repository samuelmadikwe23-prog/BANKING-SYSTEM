package view;

import controller.LoginController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.User;

import java.util.function.Consumer;

public class LoginView {
    private final LoginController controller = new LoginController();
    private final HBox root = new HBox();

    public LoginView(Consumer<User> onSuccess) {
        root.getChildren().addAll(buildBrandPanel(), buildFormPanel(onSuccess));
    }

    private VBox buildBrandPanel() {
        Rectangle shape = new Rectangle(72, 72);
        shape.setArcWidth(24);
        shape.setArcHeight(24);
        shape.setFill(Color.WHITE);
        Label mark = new Label("SP");
        mark.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #4361EE;");
        StackPane logo = new StackPane(shape, mark);
        logo.setMaxSize(72, 72);

        Label name = new Label("SwiftPass");
        name.getStyleClass().add("brand-title");
        Label tagline = new Label("Banking that keeps up with you.");
        tagline.getStyleClass().add("brand-tagline");

        VBox box = new VBox(16, logo, name, tagline);
        box.getStyleClass().add("brand-panel");
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(360);
        box.setMinWidth(360);
        return box;
    }

    private VBox buildFormPanel(Consumer<User> onSuccess) {
        Label welcome = new Label("Welcome back");
        welcome.getStyleClass().add("title");
        Label sub = new Label("Log in to your SwiftPass account");
        sub.getStyleClass().add("subtitle");

        Label userLabel = new Label("USERNAME");
        userLabel.getStyleClass().add("field-label");
        TextField username = new TextField();
        username.setPromptText("e.g. thabo");

        Label passLabel = new Label("PASSWORD");
        passLabel.getStyleClass().add("field-label");
        PasswordField password = new PasswordField();
        password.setPromptText("Your password");
        TextField visible = new TextField();
        visible.setPromptText("Your password");
        visible.setVisible(false);
        visible.setManaged(false);
        visible.textProperty().bindBidirectional(password.textProperty());

        CheckBox show = new CheckBox("Show password");
        show.selectedProperty().addListener((obs, was, now) -> {
            visible.setVisible(now);
            visible.setManaged(now);
            password.setVisible(!now);
            password.setManaged(!now);
        });

        Button loginBtn = new Button("Log in");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setDefaultButton(true);

        Label message = new Label();
        message.setWrapText(true);

        Hyperlink forgot = new Hyperlink("Forgot your password?");
        forgot.setOnAction(e -> {
            message.getStyleClass().setAll("label", "muted");
            message.setText("Customers: ask a teller at the branch. "
                    + "Tellers: ask another teller to reset it for you.");
        });

        loginBtn.setOnAction(e -> {
            try {
                User u = controller.login(username.getText().trim().toLowerCase(), password.getText());
                onSuccess.accept(u);
            } catch (IllegalArgumentException ex) {
                message.getStyleClass().setAll("label", "error-text");
                message.setText(ex.getMessage());
            }
        });

        VBox.setMargin(userLabel, new Insets(18, 0, 0, 0));
        VBox.setMargin(loginBtn, new Insets(8, 0, 0, 0));

        VBox card = new VBox(8, welcome, sub, userLabel, username, passLabel,
                password, visible, show, loginBtn, message, forgot);
        card.getStyleClass().add("card");
        card.setMaxWidth(380);
        card.setPadding(new Insets(32));

        VBox side = new VBox(card);
        side.setAlignment(Pos.CENTER);
        side.setPadding(new Insets(30));
        HBox.setHgrow(side, Priority.ALWAYS);
        return side;
    }

    public Parent getRoot() {
        return root;
    }
}