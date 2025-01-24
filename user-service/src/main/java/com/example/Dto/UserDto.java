package com.example.Dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class UserDto {

//    @NotNull
//    @Size(min = 2, max = 50)
    private String name;
    private String email;
    private String phone;
    private String kycId;
    private String address;
}
