package controller;

import dao.AccountDAO;
import dao.TransactionDAO;
import model.Account;
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
        a.deposit(amount);                      // the model checks the amount
        accountDAO.updateBalance(a);            // save the new balance
        transactionDAO.record(accountNumber, "DEPOSIT", amount, a.getBalance());
        return a.getBalance();
    }

    public double withdraw(String accountNumber, double amount) {
        Account a = find(accountNumber);
        a.withdraw(amount);                     // the account type decides if it's allowed
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
}
