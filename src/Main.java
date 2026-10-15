import model.*;

public class Main {
    public static void main(String[] args) {
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
    }
}