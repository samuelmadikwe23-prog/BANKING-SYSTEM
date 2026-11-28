package view;

import javafx.scene.Parent;
import javafx.scene.control.Button;

public class Theme {
    private static boolean dark = false;

    public static void apply(Parent root) {
        root.getStyleClass().remove("dark");
        if (dark) {
            root.getStyleClass().add("dark");
        }
    }

    public static Button toggleButton(Parent root) {
        Button b = new Button();
        b.getStyleClass().add("secondary");
        update(b);
        b.setOnAction(e -> {
            dark = !dark;
            apply(root);
            update(b);
        });
        return b;
    }

    private static void update(Button b) {
        b.setText(dark ? "Light mode" : "Night mode");
    }
}