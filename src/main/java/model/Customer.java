package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Customer {
    private int id;
    private String firstName;
    private String surname;
    private String address;
    private String phone;
    private String email;
    private String idNumber;
    private LocalDate dateOfBirth;
    private List<Account> accounts = new ArrayList<>();

    public Customer(String firstName, String surname, String address) {
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
    }

    public Customer(String firstName, String surname, String address,
                    String phone, String email, String idNumber, LocalDate dateOfBirth) {
        this(firstName, surname, address);
        this.phone = phone;
        this.email = email;
        this.idNumber = idNumber;
        this.dateOfBirth = dateOfBirth;
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getIdNumber() { return idNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
}