package model;

public  abstract class Account {

    private String accountNumber;
    private double balance;
    private String branch;
    private Customer owner;

    public Account(String accountNumber , String branch, Customer owner){
        this.accountNumber = accountNumber;
        this.branch = branch;
        this.owner = owner;
        this.balance = 0;
        owner.addAccount(this); 
    }

    public void deposit(double amount){
        if (amount <=0){
            throw new IllegalArgumentException("Deposit must be positive");
        }
        balance += amount;
    }

    public void deposit(double amount, String note) {
        deposit(amount);
        System.out.println("Deposit note: "+ note);
    }

    public abstract void withdraw(double amount);

    protected void setBalance(double balance) {this.balance = balance;}
    public double getBalance() {return balance;}
    public String getAccountNumber() {return accountNumber; }
    public String getBranch() {return branch;}
    public Customer getOwner() {return owner;}
}