package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import model.User;

public class MainApp extends Application {
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        dao.DatabaseManager.initialise();
        util.DataSeeder.seedIfNeeded();
        stage.setTitle("Banking System");
        showLogin();
        stage.show();
    }

    private void showLogin() {
        LoginView login = new LoginView(this::onLogin);
        stage.setScene(new Scene(login.getRoot(), 400, 350));
    }

    private void onLogin(User user) {
        if ("CUSTOMER".equals(user.role())) {
            CustomerView view = new CustomerView(user.customerId(), this::showLogin);
            stage.setScene(new Scene(view.getRoot(), 650, 600));
        } else {
            // placeholder until the teller screen is built
            TellerView view = new TellerView(this::showLogin);
            stage.setScene(new Scene(view.getRoot(), 450, 650));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}