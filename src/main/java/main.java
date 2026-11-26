import model.*;

public class main {
    public static void main(String[] args) {
        dao.DatabaseManager.initialise();
        controller.AccountController ctrl = new controller.AccountController();

        System.out.println("Deposit: " + ctrl.deposit("S900", 250));
        System.out.println("Withdraw: " + ctrl.withdraw("I900", 100));

        try {
            ctrl.withdraw("S900", 10);
        } catch (UnsupportedOperationException e) {
            System.out.println("Blocked: " + e.getMessage());
        }

        for (Transaction t : ctrl.getHistory("I900")) {
            System.out.println(t);
        }

                controller.CustomerController cc = new controller.CustomerController();
        Customer t = cc.register("Lesego", "Phiri", "Plot 7, Gaborone");
        String n = String.valueOf(System.currentTimeMillis() % 100000);
        cc.openSavings(t, "S" + n, "Main Mall");
        cc.openInvestment(t, "I" + n, "Main Mall", 600);
        cc.openCheque(t, "C" + n, "Main Mall", "Acme Ltd", "Plot 5, Gaborone");
        for (Account a : cc.getAccounts(t)) {
            System.out.println(a.getAccountNumber() + " " + a.getClass().getSimpleName()
                    + " " + a.getBalance());
        }

                controller.LoginController lc = new controller.LoginController();
        lc.createUser("teller1", "pass123", "TELLER", null);

        System.out.println("Login ok, role: " + lc.login("teller1", "pass123").role());
        try {
            lc.login("teller1", "wrong");
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }

    
}