package util;

import controller.AccountController;
import controller.CustomerController;
import controller.LoginController;
import dao.UserDAO;
import model.Account;
import model.Customer;

import java.time.LocalDate;

public class DataSeeder {

    public static void seedIfNeeded() {
        LoginController lc = new LoginController();
        lc.createUser("teller2", "pass123", "TELLER", null);

        // if teller1 exists, the sample data is already in place
        if (new UserDAO().findByUsername("teller1") != null) {
            return;
        }

        CustomerController cc = new CustomerController();
        AccountController ac = new AccountController();

        lc.createUser("teller1", "pass123", "TELLER", null);

        String[][] people = {
                {"Thabo", "Mokoena", "Plot 123, Gaborone"},
                {"Neo", "Kgosi", "Plot 9, Francistown"},
                {"Ann", "Moyo", "Plot 4, Maun"},
                {"Lesego", "Phiri", "Plot 7, Gaborone"},
                {"Kabo", "Seretse", "Block 8, Gaborone"},
                {"Mpho", "Dube", "Plot 21, Palapye"},
                {"Tshepo", "Molefe", "Plot 15, Serowe"},
                {"Boitumelo", "Ntsima", "Plot 3, Lobatse"},
                {"Kagiso", "Mmusi", "Block 5, Selebi-Phikwe"},
                {"Onkabetse", "Sebina", "Plot 12, Kasane"}
        };

        for (int i = 0; i < people.length; i++) {
            String first = people[i][0];
            String last = people[i][1];
            String phone = String.format("7123%04d", i + 1);
            String email = first.toLowerCase() + "." + last.toLowerCase() + "@example.com";
            String idNumber = "10000000" + i;
            LocalDate dob = LocalDate.of(1985 + i, 3 + (i % 9), 10 + i);

            Customer c = cc.register(first, last, people[i][2], phone, email, idNumber, dob);

            // everyone gets a savings account
            Account savings = cc.openSavings(c);
            ac.deposit(savings.getAccountNumber(), 1000 + i * 250);

            // every second customer also gets an investment account
            if (i % 2 == 0) {
                cc.openInvestment(c, 500 + i * 100);
            }

            // every third customer also gets a cheque account
            if (i % 3 == 0) {
                Account cheque = cc.openCheque(c, "Acme Ltd", "Plot 5, Gaborone");
                ac.deposit(cheque.getAccountNumber(), 5000 + i * 300);
                ac.withdraw(cheque.getAccountNumber(), 750);
            }

            // each customer logs in with their first name and password pass123
            lc.createUser(first.toLowerCase(), "pass123", "CUSTOMER", c.getId());
        }
    }
}