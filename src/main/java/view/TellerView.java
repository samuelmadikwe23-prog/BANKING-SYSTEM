package view;

import controller.CustomerController;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.Customer;

public class TellerView {
    private final CustomerController controller = new CustomerController();
    private final VBox root = new VBox(8);
    private final ComboBox<Customer> customerBox = new ComboBox<>();
    private final Label message = new Label();

    public TellerView(Runnable onLogout) {
        // --- register customer ---
        TextField first = new TextField();
        first.setPromptText("First name");
        TextField last = new TextField();
        last.setPromptText("Surname");
        TextField address = new TextField();
        address.setPromptText("Address");
        Button registerBtn = new Button("Register customer");
        registerBtn.setOnAction(e -> {
            try {
                Customer c = controller.register(first.getText().trim(),
                        last.getText().trim(), address.getText().trim());
                showSuccess("Registered " + c.getFirstName() + " " + c.getSurname());
                first.clear();
                last.clear();
                address.clear();
                loadCustomers();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });

        // --- open account ---
        customerBox.setPromptText("Choose customer");
        customerBox.setConverter(new StringConverter<>() {
            public String toString(Customer c) {
                return c == null ? "" : c.getId() + " - " + c.getFirstName() + " " + c.getSurname();
            }
            public Customer fromString(String s) { return null; }
        });
        loadCustomers();

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Savings", "Investment", "Cheque");
        typeBox.setPromptText("Account type");
        TextField number = new TextField();
        number.setPromptText("Account number");
        TextField deposit = new TextField();
        deposit.setPromptText("Initial deposit (investment only, min BWP500)");
        TextField empName = new TextField();
        empName.setPromptText("Employer name (cheque only)");
        TextField empAddress = new TextField();
        empAddress.setPromptText("Employer address (cheque only)");

        Button openBtn = new Button("Open account");
        openBtn.setOnAction(e -> {
            Customer owner = customerBox.getValue();
            String type = typeBox.getValue();
            if (owner == null || type == null || number.getText().isBlank()) {
                showError("Choose a customer, an account type and enter an account number");
                return;
            }
            try {
                String num = number.getText().trim();
                switch (type) {
                    case "Savings" -> controller.openSavings(owner, num, "Main Mall");
                    case "Investment" -> controller.openInvestment(owner, num, "Main Mall",
                            Double.parseDouble(deposit.getText().trim()));
                    default -> controller.openCheque(owner, num, "Main Mall",
                            empName.getText().trim(), empAddress.getText().trim());
                }
                showSuccess(type + " account " + num + " opened");
                number.clear();
                deposit.clear();
                empName.clear();
                empAddress.clear();
            } catch (NumberFormatException ex) {
                showError("Enter a valid initial deposit");
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            } catch (RuntimeException ex) {
                showError("Could not open account (does that account number already exist?)");
            }
        });

        Button logoutBtn = new Button("Log out");
        logoutBtn.setOnAction(e -> onLogout.run());

        Label title = new Label("Teller");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label regTitle = new Label("Register a customer");
        regTitle.setStyle("-fx-font-weight: bold;");
        Label openTitle = new Label("Open an account");
        openTitle.setStyle("-fx-font-weight: bold;");

        root.setPadding(new Insets(20));
        root.getChildren().addAll(title, regTitle, first, last, address, registerBtn,
                new Separator(), openTitle, customerBox, typeBox, number, deposit,
                empName, empAddress, openBtn, message, logoutBtn);
    }

    private void loadCustomers() {
        customerBox.getItems().setAll(controller.getAllCustomers());
    }

    private void showError(String text) {
        message.setStyle("-fx-text-fill: #b00020;");
        message.setText(text);
    }

    private void showSuccess(String text) {
        message.setStyle("-fx-text-fill: #1b7f3b;");
        message.setText(text);
    }

    public Parent getRoot() {
        return root;
    }
}