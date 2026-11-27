package view;

import controller.AccountController;
import controller.CustomerController;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Transaction;

public class CustomerView {
    private final CustomerController customers = new CustomerController();
    private final AccountController accounts = new AccountController();

    private final VBox root = new VBox(10);
    private final ComboBox<String> accountBox = new ComboBox<>();
    private final Label balanceLabel = new Label("Balance: -");
    private final TextField amountField = new TextField();
    private final TextArea historyArea = new TextArea();
    private final Label message = new Label();

    public CustomerView(int customerId, Runnable onLogout) {
        Label title = new Label("Welcome, " + customers.getCustomerName(customerId));
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        accountBox.getItems().addAll(customers.getAccountNumbers(customerId));
        accountBox.setPromptText("Choose an account");
        accountBox.setOnAction(e -> refresh());

        amountField.setPromptText("Amount (BWP)");

        Button depositBtn = new Button("Deposit");
        Button withdrawBtn = new Button("Withdraw");
        Button logoutBtn = new Button("Log out");
        depositBtn.setOnAction(e -> transact(true));
        withdrawBtn.setOnAction(e -> transact(false));
        logoutBtn.setOnAction(e -> onLogout.run());

        historyArea.setEditable(false);
        historyArea.setPrefRowCount(10);

        HBox buttons = new HBox(10, depositBtn, withdrawBtn, logoutBtn);

        root.setPadding(new Insets(20));
        root.getChildren().addAll(title, accountBox, balanceLabel, amountField, buttons,
                message, new Label("Transaction history"), historyArea);
    }

    private void transact(boolean isDeposit) {
        String account = accountBox.getValue();
        if (account == null) {
            showError("Choose an account first");
            return;
        }
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (isDeposit) {
                accounts.deposit(account, amount);
            } else {
                accounts.withdraw(account, amount);
            }
            showSuccess(isDeposit ? "Deposit successful" : "Withdrawal successful");
            amountField.clear();
            refresh();
        } catch (NumberFormatException ex) {
            showError("Enter a valid amount");
        } catch (IllegalArgumentException | UnsupportedOperationException ex) {
            showError(ex.getMessage());
        }
    }

    private void refresh() {
        String account = accountBox.getValue();
        if (account == null) {
            return;
        }
        balanceLabel.setText(String.format("Balance: BWP%.2f", accounts.getBalance(account)));
        StringBuilder sb = new StringBuilder();
        for (Transaction t : accounts.getHistory(account)) {
            sb.append(String.format("%s  %-10s BWP%.2f  (balance %.2f)%n",
                    t.createdAt().toLocalDate(), t.type(), t.amount(), t.balanceAfter()));
        }
        historyArea.setText(sb.toString());
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