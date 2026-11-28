package integration;

import controller.AccountController;
import controller.CustomerController;
import controller.LoginController;
import dao.AccountDAO;
import dao.DatabaseManager;
import model.Account;
import model.Customer;
import model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BankingIntegrationTest {

    static CustomerController customers;
    static AccountController accounts;
    static LoginController login;

    @BeforeAll
    static void setUp() {
        DatabaseManager.initialise();
        customers = new CustomerController();
        accounts = new AccountController();
        login = new LoginController();
    }

    private static String uniqueId() {
        return String.valueOf(System.nanoTime() % 1_000_000);
    }

    @Test
    void fullFlow_login_deposit_withdraw_persistsToDatabase() {
        String id = uniqueId();
        String username = "it" + id;
        String savings = "TS" + id;
        String investment = "TI" + id;

        Customer c = customers.register("Test", "User", "Plot 1, Gaborone");
        login.createUser(username, "secret", "CUSTOMER", c.getId());
        customers.openSavings(c, savings, "Main Mall");
        customers.openInvestment(c, investment, "Main Mall", 600);

        // 1. log in
        User u = login.login(username, "secret");
        assertEquals("CUSTOMER", u.role());
        assertEquals(c.getId(), u.customerId());

        // 2. deposit and withdraw through the controller
        accounts.deposit(savings, 1000);
        assertEquals(1000.0, accounts.getBalance(savings), 0.001);
        accounts.withdraw(investment, 100);
        assertEquals(500.0, accounts.getBalance(investment), 0.001);

        // 3. read straight from the database, bypassing the controller
        Account reloaded = new AccountDAO().findByNumber(investment);
        assertEquals(500.0, reloaded.getBalance(), 0.001);

        // 4. the transaction was recorded
        assertEquals(1, accounts.getHistory(savings).size());
        assertEquals("DEPOSIT", accounts.getHistory(savings).get(0).type());
    }

    @Test
    void savingsWithdrawal_isBlocked_andBalanceUnchanged() {
        String id = uniqueId();
        Customer c = customers.register("Test", "Savings", "Plot 2, Gaborone");
        customers.openSavings(c, "TS" + id, "Main Mall");
        accounts.deposit("TS" + id, 500);

        assertThrows(UnsupportedOperationException.class,
                () -> accounts.withdraw("TS" + id, 100));
        assertEquals(500.0, accounts.getBalance("TS" + id), 0.001);
    }

    @Test
    void investmentBelowMinimumDeposit_isRejected() {
        Customer c = customers.register("Test", "Invest", "Plot 3, Gaborone");
        assertThrows(IllegalArgumentException.class,
                () -> customers.openInvestment(c, "TI" + uniqueId(), "Main Mall", 100));
    }

    @Test
    void chequeWithoutEmployerDetails_isRejected() {
        Customer c = customers.register("Test", "Cheque", "Plot 4, Gaborone");
        assertThrows(IllegalArgumentException.class,
                () -> customers.openCheque(c, "TC" + uniqueId(), "Main Mall", "", ""));
    }

    @Test
    void negativeDeposit_isRejected() {
        String id = uniqueId();
        Customer c = customers.register("Test", "Negative", "Plot 5, Gaborone");
        customers.openSavings(c, "TS" + id, "Main Mall");
        assertThrows(IllegalArgumentException.class,
                () -> accounts.deposit("TS" + id, -50));
    }

    @Test
    void wrongPassword_isRejected() {
        String username = "it" + uniqueId();
        login.createUser(username, "secret", "CUSTOMER", null);
        assertThrows(IllegalArgumentException.class,
                () -> login.login(username, "wrong"));
    }
}