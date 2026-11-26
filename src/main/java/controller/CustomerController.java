package controller;

import dao.AccountDAO;
import dao.CustomerDAO;
import model.*;

import java.util.List;

public class CustomerController {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    public Customer register(String firstName, String surname, String address) {
        if (firstName.isBlank() || surname.isBlank() || address.isBlank()) {
            throw new IllegalArgumentException("All customer details are required");
        }
        Customer c = new Customer(firstName, surname, address);
        customerDAO.save(c);
        return c;
    }

    public Account openSavings(Customer owner, String accountNumber, String branch) {
        Account a = new SavingsAccount(accountNumber, branch, owner);
        accountDAO.save(a);
        return a;
    }

    public Account openInvestment(Customer owner, String accountNumber, String branch,
                                  double initialDeposit) {
        Account a = new InvestmentAccount(accountNumber, branch, owner, initialDeposit);
        accountDAO.save(a);
        return a;
    }

    public Account openCheque(Customer owner, String accountNumber, String branch,
                              String employerName, String employerAddress) {
        Account a = new ChequeAccount(accountNumber, branch, owner, employerName, employerAddress);
        accountDAO.save(a);
        return a;
    }

    public List<Account> getAccounts(Customer c) {
        Customer fresh = customerDAO.findById(c.getId());
        return accountDAO.findByCustomer(fresh);
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }
}