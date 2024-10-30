package org.example.virtualgene.DTO;

import lombok.Data;
import org.example.virtualgene.domain.DAO.Account;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
public class NewAccountDTO {
    private String name;
    private String email;
    private String password;

    public Account convertoAccount(String roles, PasswordEncoder passwordEncoder) {
        return new Account(email, name, roles, passwordEncoder.encode(password));
    }
}
