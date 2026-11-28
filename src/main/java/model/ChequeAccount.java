package model;

public class ChequeAccount extends Account {
    private String employerName;
    private String employerAddress;

    public ChequeAccount(String accountNumber, String branch, Customer owner,
                         String employerName, String employerAddress) {
        super(accountNumber, branch, owner);
        if (employerName == null || employerName.isBlank()
                || employerAddress == null || employerAddress.isBlank()) {
            throw new IllegalArgumentException("Cheque account requires employer details");
        }
        this.employerName = employerName;
        this.employerAddress = employerAddress;
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Enter an amount greater than zero");
        }
        if (amount > getBalance()) {
            throw new IllegalArgumentException(String.format(java.util.Locale.US,
                    "Insufficient funds. Available balance: BWP%.2f", getBalance()));
        }
        setBalance(getBalance() - amount);
    }

    public String getEmployerName() { return employerName; }
    public String getEmployerAddress() { return employerAddress; }
}