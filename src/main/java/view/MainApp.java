package view;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.User;

public class MainApp extends Application {
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        dao.DatabaseManager.initialise();
        util.DataSeeder.seedIfNeeded();
        stage.setTitle("SwiftPass Banking System");
        showLogin();
        stage.show();
    }

    private Scene styled(Parent root, double width, double height) {
        Theme.apply(root);
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        return scene;
    }

    private void showLogin() {
        LoginView login = new LoginView(this::onLogin);
        stage.setScene(styled(login.getRoot(), 880, 560));
        stage.centerOnScreen();
    }

    private void onLogin(User user) {
        if ("CUSTOMER".equals(user.role())) {
            CustomerView view = new CustomerView(user.customerId(), this::showLogin);
            stage.setScene(styled(view.getRoot(), 820, 700));
        } else {
            TellerView view = new TellerView(this::showLogin);
            stage.setScene(styled(view.getRoot(), 1020, 760));
        }
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}