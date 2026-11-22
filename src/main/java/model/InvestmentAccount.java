package model;

public class InvestmentAccount extends Account implements InterestBearing {
    private  static final double INTEREST_RATE = 0.05;// 5% monthly
    private  static final double MIN_OPENING_DEPOSIT = 500.00;

    public InvestmentAccount(String accountNumber, String branch, Customer owner, double initialDeposit) {
        super(accountNumber, branch, owner);
        if (initialDeposit < MIN_OPENING_DEPOSIT) {
            throw new IllegalArgumentException("Minimum opening deposit is BWP500");
        }
        deposit(initialDeposit);
    }

    @Override 
    public void withdraw(double amount) {
        if (amount <=0 || amount > getBalance()) {
            throw new IllegalArgumentException("Invalid withdrawal amount");
        }
        setBalance(getBalance() - amount);
    }

    @Override 
    public void payInterest() {
        deposit(getBalance() * INTEREST_RATE);
    }
}