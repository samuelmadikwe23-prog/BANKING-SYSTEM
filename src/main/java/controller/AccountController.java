package controller;

import dao.AccountDAO;
import dao.TransactionDAO;
import model.Account;
import model.InterestBearing;
import model.SavingsAccount;
import model.Transaction;

import java.util.List;

public class AccountController {
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private Account find(String accountNumber) {
        Account a = accountDAO.findByNumber(accountNumber);
        if (a == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        return a;
    }

    public double deposit(String accountNumber, double amount) {
        Account a = find(accountNumber);
        a.deposit(amount);
        accountDAO.updateBalance(a);
        transactionDAO.record(accountNumber, "DEPOSIT", amount, a.getBalance());
        return a.getBalance();
    }

    public double withdraw(String accountNumber, double amount) {
        Account a = find(accountNumber);
        a.withdraw(amount);
        accountDAO.updateBalance(a);
        transactionDAO.record(accountNumber, "WITHDRAWAL", amount, a.getBalance());
        return a.getBalance();
    }

    public double getBalance(String accountNumber) {
        return find(accountNumber).getBalance();
    }

    public List<Transaction> getHistory(String accountNumber) {
        return transactionDAO.findByAccount(accountNumber);
    }

    public boolean canWithdraw(String accountNumber) {
        return !(find(accountNumber) instanceof SavingsAccount);
    }

    public String getAccountType(String accountNumber) {
        return find(accountNumber).getClass().getSimpleName().replace("Account", "");
    }

    public String getAccountLabel(String accountNumber) {
        return getAccountType(accountNumber) + " - " + accountNumber;
    }

    // Stands in for the Scheduler actor: pays interest to every interest-bearing account
    public int payInterestToAll() {
        int paid = 0;
        for (Account a : accountDAO.findAll()) {
            if (a instanceof InterestBearing ib) {
                double before = a.getBalance();
                ib.payInterest();
                accountDAO.updateBalance(a);
                transactionDAO.record(a.getAccountNumber(), "INTEREST",
                        a.getBalance() - before, a.getBalance());
                paid++;
            }
        }
        return paid;
    }
}