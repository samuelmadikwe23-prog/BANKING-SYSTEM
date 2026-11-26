package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        dao.DatabaseManager.initialise();
        util.DataSeeder.seedIfNeeded();

        LoginView login = new LoginView(user -> {
            // placeholder until the next screens exist
            Label welcome = new Label("Welcome " + user.username() + " (" + user.role() + ")");
            stage.setScene(new Scene(new StackPane(welcome), 600, 400));
        });

        stage.setTitle("Banking System");
        stage.setScene(new Scene(login.getRoot(), 400, 350));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}