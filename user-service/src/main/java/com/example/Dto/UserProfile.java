package com.example.Dto;


import jakarta.persistence.Entity;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    private String name;
    private String email;
    private String phone;
    private String address;
    private Double balance;
}
