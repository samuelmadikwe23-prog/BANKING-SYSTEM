import model.*;

public class main 
[]
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
    }
