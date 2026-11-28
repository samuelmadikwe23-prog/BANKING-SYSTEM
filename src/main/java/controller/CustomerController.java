package controller;

import dao.AccountDAO;
import dao.CustomerDAO;
import dao.TransactionDAO;
import dao.UserDAO;
import model.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomerController {
    private static final String BRANCH = "Main Mall";

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final UserDAO userDAO = new UserDAO();
    private final LoginController loginController = new LoginController();
    private final Random random = new Random();

    // ---------- customers ----------

    // simple version (used by the tests)
    public Customer register(String firstName, String surname, String address) {
        if (firstName.isBlank() || surname.isBlank() || address.isBlank()) {
            throw new IllegalArgumentException("All customer details are required");
        }
        Customer c = new Customer(firstName.trim(), surname.trim(), address.trim());
        customerDAO.save(c);
        return c;
    }

    // full version with contact details and checks
    public Customer register(String firstName, String surname, String address,
                             String phone, String email, String idNumber, LocalDate dob) {
        if (firstName.isBlank() || surname.isBlank() || address.isBlank()) {
            throw new IllegalArgumentException("First name, surname and address are required");
        }
        String p = phone == null ? "" : phone.replaceAll("[\\s-]", "");
        if (!p.matches("\\+?[0-9]{7,15}")) {
            throw new IllegalArgumentException("Enter a valid phone number, for example 71234567");
        }
        String e = email == null ? "" : email.trim();
        if (!e.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("Enter a valid email address");
        }
        String id = idNumber == null ? "" : idNumber.trim();
        if (!id.matches("[0-9]{9}")) {
            throw new IllegalArgumentException("ID (Omang) number must be 9 digits");
        }
        if (dob == null) {
            throw new IllegalArgumentException("Enter the date of birth as dd/mm/yyyy");
        }
        if (Period.between(dob, LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("The customer must be at least 18 years old");
        }
        for (Customer other : customerDAO.findAll()) {
            if (id.equals(other.getIdNumber())) {
                throw new IllegalArgumentException("A customer with that ID number is already registered");
            }
        }
        Customer c = new Customer(firstName.trim(), surname.trim(), address.trim(), p, e, id, dob);
        customerDAO.save(c);
        return c;
    }

    // registers the customer and creates a login with a temporary password
    public RegistrationResult registerWithLogin(String firstName, String surname, String address,
                                                String phone, String email, String idNumber,
                                                LocalDate dob) {
        Customer c = register(firstName, surname, address, phone, email, idNumber, dob);
        String username = firstName.trim().toLowerCase().replaceAll("[^a-z0-9]", "") + c.getId();
        String tempPassword = String.format("%06d", random.nextInt(1_000_000));
        loginController.createUser(username, tempPassword, "CUSTOMER", c.getId());
        return new RegistrationResult(c, username, tempPassword);
    }

    public Customer getCustomer(int customerId) {
        return customerDAO.findById(customerId);
    }

    // text matches the name, phone or ID number; filter = "All", "With accounts", "Without accounts"
    public List<CustomerSummary> searchCustomers(String text, String filter) {
        String q = text == null ? "" : text.trim().toLowerCase();
        List<CustomerSummary> result = new ArrayList<>();
        for (Customer c : customerDAO.findAll()) {
            String name = c.getFirstName() + " " + c.getSurname();
            boolean matches = q.isEmpty()
                    || name.toLowerCase().contains(q)
                    || (c.getPhone() != null && c.getPhone().contains(q))
                    || (c.getIdNumber() != null && c.getIdNumber().contains(q));
            if (!matches) {
                continue;
            }
            int count = accountDAO.findByCustomer(c).size();
            if ("With accounts".equals(filter) && count == 0) {
                continue;
            }
            if ("Without accounts".equals(filter) && count > 0) {
                continue;
            }
            result.add(new CustomerSummary(c.getId(), name,
                    c.getPhone() == null ? "-" : c.getPhone(), c.getAddress(), count));
        }
        return result;
    }

    // removes the customer's transactions, accounts, login and then the customer
    public void deleteCustomer(int customerId) {
        Customer c = customerDAO.findById(customerId);
        if (c == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        for (Account a : accountDAO.findByCustomer(c)) {
            transactionDAO.deleteByAccount(a.getAccountNumber());
            accountDAO.delete(a.getAccountNumber());
        }
        userDAO.deleteByCustomer(customerId);
        customerDAO.delete(customerId);
    }

    // ---------- opening accounts (numbers are generated) ----------

    public Account openSavings(Customer owner) {
        return openSavings(owner, newAccountNumber('1'), BRANCH);
    }

    public Account openInvestment(Customer owner, double initialDeposit) {
        Account a = openInvestment(owner, newAccountNumber('2'), BRANCH, initialDeposit);
        transactionDAO.record(a.getAccountNumber(), "DEPOSIT", initialDeposit, a.getBalance());
        return a;
    }

    public Account openCheque(Customer owner, String employerName, String employerAddress) {
        return openCheque(owner, newAccountNumber('3'), BRANCH, employerName, employerAddress);
    }

    // versions where the number is supplied (used by the tests)

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

    // first digit shows the type: 1 savings, 2 investment, 3 cheque
    private String newAccountNumber(char typeDigit) {
        String number;
        do {
            number = typeDigit + String.format("%09d", random.nextInt(1_000_000_000));
        } while (accountDAO.findByNumber(number) != null);
        return number;
    }

    // ---------- reading ----------

    public List<Account> getAccounts(Customer c) {
        Customer fresh = customerDAO.findById(c.getId());
        return accountDAO.findByCustomer(fresh);
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }

    public String getCustomerName(int customerId) {
        Customer c = customerDAO.findById(customerId);
        return c.getFirstName() + " " + c.getSurname();
    }

    public List<String> getAccountNumbers(int customerId) {
        Customer c = customerDAO.findById(customerId);
        List<String> numbers = new ArrayList<>();
        for (Account a : accountDAO.findByCustomer(c)) {
            numbers.add(a.getAccountNumber());
        }
        return numbers;
    }
}