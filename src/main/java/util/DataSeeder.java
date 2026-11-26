package util;

import controller.AccountController;
import controller.CustomerController;
import controller.LoginController;
import dao.AccountDAO;
import model.Customer;

public class DataSeeder {

    public static void seedIfNeeded() {
        // if S1001 already exists, the sample data is already in place
        if (new AccountDAO().findByNumber("S1001") != null) {
            return;
        }

        CustomerController cc = new CustomerController();
        AccountController ac = new AccountController();
        LoginController lc = new LoginController();

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
            Customer c = cc.register(people[i][0], people[i][1], people[i][2]);
            int n = 1001 + i;

            // everyone gets a savings account
            cc.openSavings(c, "S" + n, "Main Mall");
            ac.deposit("S" + n, 1000 + i * 250);

            // every second customer also gets an investment account
            if (i % 2 == 0) {
                cc.openInvestment(c, "I" + n, "Main Mall", 500 + i * 100);
            }

            // every third customer also gets a cheque account
            if (i % 3 == 0) {
                cc.openCheque(c, "C" + n, "Main Mall", "Acme Ltd", "Plot 5, Gaborone");
                ac.deposit("C" + n, 5000 + i * 300);
                ac.withdraw("C" + n, 750);
            }

            // each customer can log in with their first name and password pass123
            lc.createUser(people[i][0].toLowerCase(), "pass123", "CUSTOMER", c.getId());
        }
    }
}