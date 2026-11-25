import model.*;

public class Main {
    public static void main(String[] args) {
        dao.DatabaseManager.initialise();
        Customer c = new Customer("Thabo", "Mokoena", "Plot 123, Gaborone");

        SavingsAccount savings = new SavingsAccount("S001", "Main Mall", c);
        InvestmentAccount invest = new InvestmentAccount("I001", "Main Mall", c, 1000);
        ChequeAccount cheque = new ChequeAccount("C001", "Main Mall", c, "Acme Ltd", "Plot 5, Gaborone");

        savings.deposit(1000);
        invest.deposit(500);
        cheque.deposit(8000, "Salary");

        invest.withdraw(200);
        cheque.withdraw(1500);

        try {
            savings.withdraw(100);
        } catch (UnsupportedOperationException e) {
            System.out.println("Blocked: " + e.getMessage());
        }

        // polymorphism: same call, different behaviour per account
        for (Account a : c.getAccounts()) {
            if (a instanceof InterestBearing ib) {
                ib.payInterest();
            }
        }

        for (Account a : c.getAccounts()) {
            System.out.printf("%s (%s): BWP%.2f%n",
                a.getAccountNumber(), a.getClass().getSimpleName(), a.getBalance());
        }

                    dao.CustomerDAO cdao = new dao.CustomerDAO();
            Customer neo = new Customer("Neo", "Kgosi", "Plot 9, Francistown");
            cdao.save(neo);
            System.out.println("Saved with id " + neo.getId());
            for (Customer x : cdao.findAll()) {
                System.out.println(x.getId() + " " + x.getFirstName() + " " + x.getSurname());
            }
            

                    dao.AccountDAO adao = new dao.AccountDAO();
        Customer ann = new Customer("Ann", "Moyo", "Plot 4, Maun");
        cdao.save(ann);

        SavingsAccount sa = new SavingsAccount("S900", "Main Mall", ann);
        sa.deposit(1000);
        InvestmentAccount ia = new InvestmentAccount("I900", "Main Mall", ann, 800);
        ChequeAccount ca = new ChequeAccount("C900", "Main Mall", ann, "Acme Ltd", "Plot 5, Gaborone");
        ca.deposit(2000);

        adao.save(sa);
        adao.save(ia);
        adao.save(ca);

        ia.withdraw(300);
        adao.updateBalance(ia);

        Customer loaded = cdao.findById(ann.getId());
        for (Account a : adao.findByCustomer(loaded)) {
            System.out.println(a.getAccountNumber() + " " + a.getClass().getSimpleName() + " " + a.getBalance());
        }

    }

}

    