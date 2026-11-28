package view;

import controller.AccountController;
import controller.CustomerController;
import controller.LoginController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import model.Account;
import model.Customer;
import model.CustomerSummary;
import model.RegistrationResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class TellerView {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final StringConverter<CustomerSummary> SUMMARY_CONVERTER =
            new StringConverter<CustomerSummary>() {
                @Override
                public String toString(CustomerSummary c) {
                    return c == null ? "" : c.name() + "  (#" + c.id() + ")";
                }

                @Override
                public CustomerSummary fromString(String s) {
                    return null;
                }
            };

    private final CustomerController customers = new CustomerController();
    private final AccountController accounts = new AccountController();
    private final LoginController logins = new LoginController();

    private final BorderPane root = new BorderPane();
    private final StackPane content = new StackPane();

    private ToggleButton openTab;
    private VBox customersPane, registerPane, openPane, interestPane, staffPane;

    private int pendingCustomerId = 0;
    private RegistrationResult lastRegistered;

    // customers tab
    private final TextField searchField = new TextField();
    private final ComboBox<String> filterBox = new ComboBox<>();
    private final TableView<CustomerSummary> table = new TableView<>();
    private final Label detailLabel = new Label("Select a customer to see their details.");
    private final Label customersMsg = new Label();
    private final Button openForBtn = new Button("Open account");
    private final Button deleteBtn = new Button("Delete customer");

    // register tab
    private final TextField regFirst = new TextField();
    private final TextField regLast = new TextField();
    private final TextField regPhone = new TextField();
    private final TextField regEmail = new TextField();
    private final TextField regId = new TextField();
    private final DatePicker regDob = new DatePicker();
    private final TextField regAddress = new TextField();
    private final Label regMsg = new Label();
    private final VBox regResult = new VBox(10);
    private final Label regSummary = new Label();
    private final Label regCreds = new Label();

    // open account tab
    private final TextField openSearch = new TextField();
    private final ComboBox<CustomerSummary> openCustomerBox = new ComboBox<>();
    private final ComboBox<String> openTypeBox = new ComboBox<>();
    private final Label openRule = new Label();
    private final TextField openDeposit = new TextField();
    private final TextField openEmpName = new TextField();
    private final TextField openEmpAddress = new TextField();
    private final VBox openDepositBox = new VBox(6);
    private final VBox openEmployerBox = new VBox(6);
    private final Label openMsg = new Label();

    // interest tab
    private final Label interestMsg = new Label();

    // staff tab
    private final TextField newTellerName = new TextField();
    private final PasswordField newTellerPass = new PasswordField();
    private final Label addTellerMsg = new Label();
    private final TextField resetName = new TextField();
    private final PasswordField resetPass = new PasswordField();
    private final Label resetMsg = new Label();

    public TellerView(Runnable onLogout) {
        root.getStyleClass().add("gradient-bg");

        customersPane = buildCustomersPane();
        registerPane = buildRegisterPane();
        openPane = buildOpenPane();
        interestPane = buildInterestPane();
        staffPane = buildStaffPane();

        root.setTop(buildTop(onLogout));
        content.setPadding(new Insets(20, 24, 24, 24));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        root.setCenter(scroll);

        showPane(customersPane);
    }

    // ================= top bar =================

    private VBox buildTop(Runnable onLogout) {
        Label logo = new Label("SP");
        logo.getStyleClass().add("logo-chip");
        Label brand = new Label("SwiftPass");
        brand.getStyleClass().add("brand-small");
        Label role = new Label("Teller");
        role.getStyleClass().add("muted");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button theme = Theme.toggleButton(root);
        Button logout = new Button("Log out");
        logout.getStyleClass().add("secondary");
        logout.setOnAction(e -> onLogout.run());
        HBox header = new HBox(12, logo, brand, role, spacer, theme, logout);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup group = new ToggleGroup();
        ToggleButton customersTab = nav("Customers", group, customersPane);
        ToggleButton registerTab = nav("Register", group, registerPane);
        openTab = nav("Open account", group, openPane);
        ToggleButton interestTab = nav("Interest", group, interestPane);
        ToggleButton staffTab = nav("Staff", group, staffPane);
        customersTab.setSelected(true);
        group.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null && old != null) {
                old.setSelected(true);
            }
        });
        HBox navBar = new HBox(8, customersTab, registerTab, openTab, interestTab, staffTab);
        navBar.setPadding(new Insets(14, 24, 0, 24));

        return new VBox(header, navBar);
    }

    private ToggleButton nav(String text, ToggleGroup group, VBox pane) {
        ToggleButton b = new ToggleButton(text);
        b.getStyleClass().add("nav-button");
        b.setToggleGroup(group);
        b.setOnAction(e -> showPane(pane));
        return b;
    }

    private void showPane(VBox pane) {
        for (Label l : new Label[]{customersMsg, regMsg, openMsg, interestMsg, addTellerMsg, resetMsg}) {
            l.setText("");
        }
        content.getChildren().setAll(pane);
        if (pane == customersPane) {
            refreshCustomers();
        }
        if (pane == openPane) {
            if (pendingCustomerId > 0) {
                openSearch.clear();
            }
            loadOpenCustomers();
        }
    }

    // ================= customers tab =================

    private VBox buildCustomersPane() {
        searchField.setPromptText("Search by name, phone or ID number...");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((o, a, b) -> refreshCustomers());
        filterBox.getItems().addAll("All", "With accounts", "Without accounts");
        filterBox.setValue("All");
        filterBox.setOnAction(e -> refreshCustomers());
        HBox filters = new HBox(10, searchField, filterBox);

        table.getColumns().add(column("ID", c -> String.valueOf(c.id())));
        table.getColumns().add(column("Name", CustomerSummary::name));
        table.getColumns().add(column("Phone", CustomerSummary::phone));
        table.getColumns().add(column("Address", CustomerSummary::address));
        table.getColumns().add(column("Accounts", c -> String.valueOf(c.accountCount())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No customers match."));
        table.setPrefHeight(260);
        table.getSelectionModel().selectedItemProperty()
                .addListener((o, a, sel) -> showDetail(sel));

        deleteBtn.getStyleClass().add("danger");
        openForBtn.setDisable(true);
        deleteBtn.setDisable(true);
        openForBtn.setOnAction(e -> {
            CustomerSummary s = table.getSelectionModel().getSelectedItem();
            if (s != null) {
                pendingCustomerId = s.id();
                openTab.fire();
            }
        });
        deleteBtn.setOnAction(e -> deleteSelected());
        HBox actions = new HBox(10, openForBtn, deleteBtn);

        detailLabel.setWrapText(true);
        detailLabel.getStyleClass().add("muted");

        return card(sectionTitle("Customers"), filters, table, detailLabel, actions, customersMsg);
    }

    private void refreshCustomers() {
        String f = filterBox.getValue() == null ? "All" : filterBox.getValue();
        table.setItems(FXCollections.observableArrayList(
                customers.searchCustomers(searchField.getText(), f)));
        showDetail(null);
    }

    private void showDetail(CustomerSummary s) {
        boolean has = s != null;
        openForBtn.setDisable(!has);
        deleteBtn.setDisable(!has);
        if (!has) {
            detailLabel.setText("Select a customer to see their details.");
            return;
        }
        Customer c = customers.getCustomer(s.id());
        StringBuilder sb = new StringBuilder();
        sb.append(s.name())
                .append("\nPhone: ").append(dash(c.getPhone()))
                .append("     Email: ").append(dash(c.getEmail()))
                .append("\nID number: ").append(dash(c.getIdNumber()))
                .append("     Born: ")
                .append(c.getDateOfBirth() == null ? "-" : DATE_FMT.format(c.getDateOfBirth()))
                .append("\n");
        List<String> numbers = customers.getAccountNumbers(s.id());
        if (numbers.isEmpty()) {
            sb.append("\nNo accounts yet.");
        } else {
            sb.append("\nAccounts:");
            for (String n : numbers) {
                sb.append("\n   ").append(accounts.getAccountLabel(n))
                        .append("   ").append(money(accounts.getBalance(n)));
            }
        }
        detailLabel.setText(sb.toString());
    }

    private void deleteSelected() {
        CustomerSummary s = table.getSelectionModel().getSelectedItem();
        if (s == null) {
            return;
        }
        if (confirm("Delete " + s.name() + "?",
                "This permanently deletes the customer, their " + s.accountCount()
                        + " account(s), transaction history and login. It cannot be undone.")) {
            try {
                customers.deleteCustomer(s.id());
                setMessage(customersMsg, s.name() + " was deleted.", true);
                refreshCustomers();
            } catch (RuntimeException ex) {
                setMessage(customersMsg, "Could not delete: " + ex.getMessage(), false);
            }
        }
    }

    // ================= register tab =================

    private VBox buildRegisterPane() {
        regFirst.setPromptText("First name");
        regLast.setPromptText("Surname");
        regPhone.setPromptText("e.g. 71234567");
        regEmail.setPromptText("name@example.com");
        regId.setPromptText("9 digits");
        regAddress.setPromptText("Address");

        regDob.setPromptText("dd/mm/yyyy");
        regDob.setMaxWidth(Double.MAX_VALUE);
        regDob.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate d) {
                return d == null ? "" : DATE_FMT.format(d);
            }

            @Override
            public LocalDate fromString(String s) {
                if (s == null || s.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(s.trim(), DATE_FMT);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(6);
        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(50);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(50);
        form.getColumnConstraints().addAll(left, right);
        form.add(fieldLabel("FIRST NAME"), 0, 0);
        form.add(regFirst, 0, 1);
        form.add(fieldLabel("SURNAME"), 1, 0);
        form.add(regLast, 1, 1);
        form.add(fieldLabel("PHONE NUMBER"), 0, 2);
        form.add(regPhone, 0, 3);
        form.add(fieldLabel("EMAIL"), 1, 2);
        form.add(regEmail, 1, 3);
        form.add(fieldLabel("ID (OMANG) NUMBER"), 0, 4);
        form.add(regId, 0, 5);
        form.add(fieldLabel("DATE OF BIRTH"), 1, 4);
        form.add(regDob, 1, 5);
        form.add(fieldLabel("ADDRESS"), 0, 6, 2, 1);
        form.add(regAddress, 0, 7, 2, 1);

        Button reg = new Button("Register customer");
        reg.setOnAction(e -> register());
        Button clear = new Button("Clear");
        clear.getStyleClass().add("secondary");
        clear.setOnAction(e -> {
            clearRegisterForm();
            regMsg.setText("");
        });
        HBox buttons = new HBox(10, reg, clear);

        regSummary.setWrapText(true);
        regCreds.setWrapText(true);
        Button openNow = new Button("Open an account now");
        Button notNow = new Button("Not now");
        notNow.getStyleClass().add("secondary");
        openNow.setOnAction(e -> {
            pendingCustomerId = lastRegistered.customer().getId();
            hideRegResult();
            openTab.fire();
        });
        notNow.setOnAction(e -> hideRegResult());
        regResult.getChildren().addAll(sectionTitle("Customer registered"), regSummary,
                regCreds, new HBox(10, openNow, notNow));
        regResult.getStyleClass().add("note");
        hideRegResult();

        return card(sectionTitle("Register a new customer"), form, buttons, regMsg, regResult);
    }

    private void register() {
        try {
            LocalDate dob = regDob.getConverter().fromString(regDob.getEditor().getText());
            RegistrationResult r = customers.registerWithLogin(
                    regFirst.getText(), regLast.getText(), regAddress.getText(),
                    regPhone.getText(), regEmail.getText(), regId.getText(), dob);
            lastRegistered = r;
            regSummary.setText(r.customer().getFirstName() + " " + r.customer().getSurname()
                    + " (#" + r.customer().getId() + ") is now registered.");
            regCreds.setText("Login username: " + r.username()
                    + "     Temporary password: " + r.tempPassword()
                    + "\nGive these to the customer. The password cannot be shown again.");
            regResult.setVisible(true);
            regResult.setManaged(true);
            clearRegisterForm();
            regMsg.setText("");
        } catch (IllegalArgumentException ex) {
            hideRegResult();
            setMessage(regMsg, ex.getMessage(), false);
        }
    }

    private void clearRegisterForm() {
        regFirst.clear();
        regLast.clear();
        regPhone.clear();
        regEmail.clear();
        regId.clear();
        regAddress.clear();
        regDob.setValue(null);
        regDob.getEditor().clear();
    }

    private void hideRegResult() {
        regResult.setVisible(false);
        regResult.setManaged(false);
    }

    // ================= open account tab =================

    private VBox buildOpenPane() {
        openSearch.setPromptText("Type a name, phone or ID number to find the customer...");
        openSearch.textProperty().addListener((o, a, b) -> loadOpenCustomers());
        openCustomerBox.setPromptText("Choose customer");
        openCustomerBox.setMaxWidth(Double.MAX_VALUE);
        openCustomerBox.setConverter(SUMMARY_CONVERTER);

        openTypeBox.getItems().addAll("Savings", "Investment", "Cheque");
        openTypeBox.setPromptText("Choose account type");
        openTypeBox.setMaxWidth(Double.MAX_VALUE);
        openTypeBox.setOnAction(e -> updateOpenFields());

        openRule.getStyleClass().add("muted");
        openRule.setWrapText(true);

        openDeposit.setPromptText("e.g. 500");
        openDepositBox.getChildren().addAll(
                fieldLabel("INITIAL DEPOSIT (BWP, MINIMUM 500)"), openDeposit);
        openEmpName.setPromptText("Employer name");
        openEmpAddress.setPromptText("Employer address");
        openEmployerBox.getChildren().addAll(
                fieldLabel("EMPLOYER NAME"), openEmpName,
                fieldLabel("EMPLOYER ADDRESS"), openEmpAddress);
        updateOpenFields();

        Button open = new Button("Open account");
        open.setOnAction(e -> openAccount());
        Button clear = new Button("Clear");
        clear.getStyleClass().add("secondary");
        clear.setOnAction(e -> clearOpenForm());
        HBox buttons = new HBox(10, open, clear);

        Label hint = new Label("The account number is generated automatically.");
        hint.getStyleClass().add("muted");

        return card(sectionTitle("Open an account"),
                fieldLabel("FIND CUSTOMER"), openSearch, openCustomerBox,
                fieldLabel("ACCOUNT TYPE"), openTypeBox, openRule,
                openDepositBox, openEmployerBox, hint, buttons, openMsg);
    }

    private void loadOpenCustomers() {
        CustomerSummary current = openCustomerBox.getValue();
        int keepId = pendingCustomerId > 0 ? pendingCustomerId : (current == null ? 0 : current.id());
        pendingCustomerId = 0;
        List<CustomerSummary> list = customers.searchCustomers(openSearch.getText(), "All");
        openCustomerBox.getItems().setAll(list);
        openCustomerBox.setValue(null);
        for (CustomerSummary s : list) {
            if (s.id() == keepId) {
                openCustomerBox.setValue(s);
                break;
            }
        }
    }

    private void updateOpenFields() {
        String t = openTypeBox.getValue();
        boolean inv = "Investment".equals(t);
        boolean chq = "Cheque".equals(t);
        setShown(openDepositBox, inv);
        setShown(openEmployerBox, chq);
        if ("Savings".equals(t)) {
            openRule.setText("Earns 0.05% interest monthly. Withdrawals are not allowed.");
        } else if (inv) {
            openRule.setText("Earns 5% interest monthly. Minimum opening deposit is BWP 500.");
        } else if (chq) {
            openRule.setText("For salaried customers. Employer details are required. No interest.");
        } else {
            openRule.setText("Choose an account type to see what is needed.");
        }
    }

    private void openAccount() {
        CustomerSummary owner = openCustomerBox.getValue();
        String type = openTypeBox.getValue();
        if (owner == null) {
            setMessage(openMsg, "Search for and choose a customer first.", false);
            return;
        }
        if (type == null) {
            setMessage(openMsg, "Choose an account type.", false);
            return;
        }
        try {
            Customer c = customers.getCustomer(owner.id());
            Account a = switch (type) {
                case "Savings" -> customers.openSavings(c);
                case "Investment" -> customers.openInvestment(c, parseAmount(openDeposit.getText()));
                default -> customers.openCheque(c, openEmpName.getText().trim(),
                        openEmpAddress.getText().trim());
            };
            openTypeBox.setValue(null);
            openDeposit.clear();
            openEmpName.clear();
            openEmpAddress.clear();
            updateOpenFields();
            setMessage(openMsg, type + " account " + a.getAccountNumber()
                    + " opened for " + owner.name() + ".", true);
        } catch (NumberFormatException ex) {
            setMessage(openMsg, "Enter a valid initial deposit, for example 500.", false);
        } catch (IllegalArgumentException ex) {
            setMessage(openMsg, ex.getMessage(), false);
        } catch (RuntimeException ex) {
            setMessage(openMsg, "Could not open the account: " + ex.getMessage(), false);
        }
    }

    private void clearOpenForm() {
        openSearch.clear();
        openCustomerBox.setValue(null);
        openTypeBox.setValue(null);
        openDeposit.clear();
        openEmpName.clear();
        openEmpAddress.clear();
        openMsg.setText("");
        updateOpenFields();
    }

    // ================= interest tab =================

    private VBox buildInterestPane() {
        Label info = new Label("Adds the monthly interest to every savings account (0.05%) "
                + "and every investment account (5%). Cheque accounts earn no interest.");
        info.setWrapText(true);
        info.getStyleClass().add("muted");

        Button run = new Button("Run monthly interest");
        run.setOnAction(e -> {
            if (confirm("Run monthly interest?",
                    "Interest will be added to all savings and investment accounts. "
                            + "Running it twice adds it twice.")) {
                int n = accounts.payInterestToAll();
                setMessage(interestMsg, "Interest paid to " + n + " accounts.", true);
            }
        });
        return card(sectionTitle("Monthly interest"), info, run, interestMsg);
    }

    // ================= staff tab =================

    private VBox buildStaffPane() {
        newTellerName.setPromptText("Username (letters and digits)");
        newTellerPass.setPromptText("Password (at least 6 characters)");
        Button add = new Button("Add teller");
        add.setOnAction(e -> {
            try {
                logins.addTeller(newTellerName.getText(), newTellerPass.getText());
                setMessage(addTellerMsg, "Teller '" + newTellerName.getText().trim().toLowerCase()
                        + "' added.", true);
                newTellerName.clear();
                newTellerPass.clear();
            } catch (IllegalArgumentException ex) {
                setMessage(addTellerMsg, ex.getMessage(), false);
            }
        });
        VBox addCard = card(sectionTitle("Add a teller"), fieldLabel("USERNAME"), newTellerName,
                fieldLabel("PASSWORD"), newTellerPass, add, addTellerMsg);

        resetName.setPromptText("Username of the teller or customer");
        resetPass.setPromptText("New password (at least 6 characters)");
        Button reset = new Button("Reset password");
        reset.setOnAction(e -> {
            try {
                logins.resetPassword(resetName.getText().trim().toLowerCase(), resetPass.getText());
                setMessage(resetMsg, "Password reset for '"
                        + resetName.getText().trim().toLowerCase() + "'.", true);
                resetName.clear();
                resetPass.clear();
            } catch (IllegalArgumentException ex) {
                setMessage(resetMsg, ex.getMessage(), false);
            }
        });
        VBox resetCard = card(sectionTitle("Reset a password"), fieldLabel("USERNAME"), resetName,
                fieldLabel("NEW PASSWORD"), resetPass, reset, resetMsg);

        HBox.setHgrow(addCard, Priority.ALWAYS);
        HBox.setHgrow(resetCard, Priority.ALWAYS);
        addCard.setMaxWidth(Double.MAX_VALUE);
        resetCard.setMaxWidth(Double.MAX_VALUE);
        HBox row = new HBox(16, addCard, resetCard);
        return new VBox(16, row);
    }

    // ================= helpers =================

    private TableColumn<CustomerSummary, String> column(String title,
                                                        Function<CustomerSummary, String> f) {
        TableColumn<CustomerSummary, String> col = new TableColumn<>(title);
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

    private void setShown(Node n, boolean shown) {
        n.setVisible(shown);
        n.setManaged(shown);
    }

    private void setMessage(Label label, String text, boolean success) {
        label.getStyleClass().setAll("label", success ? "success-text" : "error-text");
        label.setWrapText(true);
        label.setText(text);
    }

    private boolean confirm(String title, String text) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.OK, ButtonType.CANCEL);
        a.setTitle(title);
        a.setHeaderText(title);
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    private String dash(String s) {
        return s == null || s.isBlank() ? "-" : s;
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