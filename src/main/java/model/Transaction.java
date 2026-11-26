package model;

import java.time.LocalDateTime;

public record Transaction (String accountNumber, String type, double amount, 
    double balanceAfter, LocalDateTime createdAt){
    
}
