import controller.CustomerController;
import model.Account;
import model.Customer;

public class Main {
    public static void main(String[] args) {
        dao.DatabaseManager.initialise();
        util.DataSeeder.seedIfNeeded();

        CustomerController cc = new CustomerController();
        for (Customer c : cc.getAllCustomers()) {
            System.out.println(c.getId() + " " + c.getFirstName() + " " + c.getSurname());
            for (Account a : cc.getAccounts(c)) {
                System.out.println("    " + a.getAccountNumber() + " "
                        + a.getClass().getSimpleName() + " BWP" + a.getBalance());
            }
        }
    }
}