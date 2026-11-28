package view;

import controller.AccountController;
import controller.CustomerController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import model.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomerView {
    private final CustomerController customers = new CustomerController();
    private final AccountController accounts = new AccountController();
    private final Map<String, String> labels = new HashMap<>();

    private final BorderPane root = new BorderPane();
    private final StackPane content = new StackPane();
    private final ComboBox<String> accountBox = new ComboBox<>();

    private final Label balanceLabel = new Label("-");
    private final Label typeLabel = new Label();
    private final Label numberLabel = new Label();

    private final TextField depositField = new TextField();
    private final Label depositMsg = new Label();
    private final Button depositBtn = new Button("Deposit");

    private final TextField withdrawField = new TextField();
    private final Label withdrawNote = new Label();
    private final Label withdrawMsg = new Label();
    private final Button withdrawBtn = new Button("Withdraw");

    private final TableView<Transaction> table = new TableView<>();

    private final VBox overviewPane;
    private final VBox depositPane;
    private final VBox withdrawPane;
    private final VBox historyPane;

    public CustomerView(int customerId, Runnable onLogout) {
        // ----- header -----
        Label logo = new Label("SP");
        logo.getStyleClass().add("logo-chip");
        Label brand = new Label("SwiftPass");
        brand.getStyleClass().add("brand-small");
        Label who = new Label(customers.getCustomerName(customerId));
        who.getStyleClass().add("muted");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button logout = new Button("Log out");
        logout.getStyleClass().add("secondary");
        logout.setOnAction(e -> onLogout.run());
        HBox header = new HBox(12, logo, brand, spacer, who, logout);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);

        // ----- account picker -----
        for (String n : customers.getAccountNumbers(customerId)) {
            labels.put(n, accounts.getAccountLabel(n));
            accountBox.getItems().add(n);
        }
        accountBox.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String n) {
                return n == null ? "" : labels.getOrDefault(n, n);
            }

            @Override
            public String fromString(String s) {
                return null;
            }
        });
        accountBox.setMaxWidth(Double.MAX_VALUE);
        accountBox.setPromptText(accountBox.getItems().isEmpty()
                ? "You have no accounts yet - please visit a teller" : "Choose an account");

        // ----- panes -----
        typeLabel.getStyleClass().add("section-title");
        numberLabel.getStyleClass().add("muted");
        balanceLabel.getStyleClass().add("balance");
        Label balCaption = new Label("Current balance");
        balCaption.getStyleClass().add("muted");
        overviewPane = card(balCaption, balanceLabel, typeLabel, numberLabel);

        depositField.setPromptText("0.00");
        depositBtn.setOnAction(e -> deposit());
        depositPane = card(sectionTitle("Deposit money"), fieldLabel("AMOUNT (BWP)"),
                depositField, depositBtn, depositMsg);

        withdrawField.setPromptText("0.00");
        withdrawNote.getStyleClass().add("muted");
        withdrawNote.setWrapText(true);
        withdrawBtn.setOnAction(e -> withdraw());
        withdrawPane = card(sectionTitle("Withdraw money"), fieldLabel("AMOUNT (BWP)"),
                withdrawField, withdrawNote, withdrawBtn, withdrawMsg);

        table.getColumns().add(column("Date", t -> t.createdAt().toLocalDate().toString()));
        table.getColumns().add(column("Type", Transaction::type));
        table.getColumns().add(column("Amount", t -> money(t.amount())));
        table.getColumns().add(column("Balance after", t -> money(t.balanceAfter())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No transactions yet."));
        VBox.setVgrow(table, Priority.ALWAYS);
        historyPane = card(sectionTitle("Transaction history"), table);
        VBox.setVgrow(historyPane, Priority.ALWAYS);

        // ----- tabs -----
        ToggleGroup group = new ToggleGroup();
        ToggleButton overviewTab = nav("Overview", group, overviewPane);
        ToggleButton depositTab = nav("Deposit", group, depositPane);
        ToggleButton withdrawTab = nav("Withdraw", group, withdrawPane);
        ToggleButton historyTab = nav("History", group, historyPane);
        overviewTab.setSelected(true);
        group.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null && old != null) {
                old.setSelected(true);
            }
        });
        HBox navBar = new HBox(8, overviewTab, depositTab, withdrawTab, historyTab);

        VBox.setVgrow(content, Priority.ALWAYS);
        VBox body = new VBox(16, fieldLabel("ACCOUNT"), accountBox, navBar, content);
        body.setPadding(new Insets(20, 24, 24, 24));

        root.setTop(header);
        root.setCenter(body);

        accountBox.setOnAction(e -> {
            clearMessages();
            refresh();
        });
        showPane(overviewPane);
        if (!accountBox.getItems().isEmpty()) {
            accountBox.getSelectionModel().selectFirst();
        }
    }

    // ----- actions -----

    private void deposit() {
        String acct = accountBox.getValue();
        try {
            double amount = parseAmount(depositField.getText());
            accounts.deposit(acct, amount);
            depositField.clear();
            setMessage(depositMsg, "Deposited " + money(amount) + ". New balance: "
                    + money(accounts.getBalance(acct)), true);
            refresh();
        } catch (NumberFormatException ex) {
            setMessage(depositMsg, "Enter a valid amount, for example 250 or 250.50", false);
        } catch (IllegalArgumentException | UnsupportedOperationException ex) {
            setMessage(depositMsg, ex.getMessage(), false);
        }
    }

    private void withdraw() {
        String acct = accountBox.getValue();
        try {
            double amount = parseAmount(withdrawField.getText());
            accounts.withdraw(acct, amount);
            withdrawField.clear();
            setMessage(withdrawMsg, "Withdrew " + money(amount) + ". New balance: "
                    + money(accounts.getBalance(acct)), true);
            refresh();
        } catch (NumberFormatException ex) {
            setMessage(withdrawMsg, "Enter a valid amount, for example 250 or 250.50", false);
        } catch (IllegalArgumentException | UnsupportedOperationException ex) {
            setMessage(withdrawMsg, ex.getMessage(), false);
        }
    }

    private void refresh() {
        String acct = accountBox.getValue();
        boolean has = acct != null;
        depositBtn.setDisable(!has);
        depositField.setDisable(!has);
        if (!has) {
            balanceLabel.setText("-");
            typeLabel.setText("");
            numberLabel.setText("");
            withdrawBtn.setDisable(true);
            withdrawField.setDisable(true);
            withdrawNote.setText("");
            table.setItems(FXCollections.observableArrayList());
            return;
        }
        balanceLabel.setText(money(accounts.getBalance(acct)));
        typeLabel.setText(accounts.getAccountType(acct) + " account");
        numberLabel.setText("Account number " + acct);

        boolean can = accounts.canWithdraw(acct);
        withdrawBtn.setDisable(!can);
        withdrawField.setDisable(!can);
        withdrawNote.setText(can ? "" : "Savings accounts do not allow withdrawals.");

        List<Transaction> list = new ArrayList<>(accounts.getHistory(acct));
        Collections.reverse(list);
        table.setItems(FXCollections.observableArrayList(list));
    }

    private void showPane(VBox pane) {
        clearMessages();
        content.getChildren().setAll(pane);
    }

    private void clearMessages() {
        depositMsg.setText("");
        withdrawMsg.setText("");
    }

    // ----- helpers -----

    private ToggleButton nav(String text, ToggleGroup group, VBox pane) {
        ToggleButton b = new ToggleButton(text);
        b.getStyleClass().add("nav-button");
        b.setToggleGroup(group);
        b.setOnAction(e -> showPane(pane));
        return b;
    }

    private TableColumn<Transaction, String> column(String title,
                                                    java.util.function.Function<Transaction, String> f) {
        TableColumn<Transaction, String> col = new TableColumn<>(title);
        col.setCellValueFactory(d -> new SimpleStringProperty(f.apply(d.getValue())));
        return col;
    }

    private VBox card(Node... children) {
        VBox box = new VBox(12, children);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(24));
        return box;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("section-title");
        return l;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("field-label");
        return l;
    }

    private void setMessage(Label label, String text, boolean success) {
        label.getStyleClass().setAll("label", success ? "success-text" : "error-text");
        label.setWrapText(true);
        label.setText(text);
    }

    private double parseAmount(String text) {
        return Double.parseDouble(text.trim().replace(',', '.'));
    }

    private String money(double v) {
        return String.format(Locale.US, "BWP %,.2f", v);
    }

    public Parent getRoot() {
        return root;
    }
}