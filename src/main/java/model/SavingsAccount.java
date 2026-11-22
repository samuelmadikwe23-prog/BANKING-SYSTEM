package model;

public class SavingsAccount extends Account implements InterestBearing {
    private static final double INTEREST_RATE = 0.0005;//0.05% MONTLY

    public SavingsAccount(String accountNumber, String branch, Customer owner) {
        super (accountNumber,branch,owner);
    }

    @Override 
    public void withdraw(double amount) {
        throw new UnsupportedOperationException("Savings accounts do not allow withdrawals");
    }

    @Override 
    public void payInterest() {
        double interest = getBalance() * INTEREST_RATE;
        deposit(interest);
    }
}