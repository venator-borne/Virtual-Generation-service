package org.example.virtualgene.domain.DAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.*;

@Data
@Table("account")
public class Account {
    @Id
    private UUID id;
    private final String email;
    private final String name;
    private final String roles;
    @JsonIgnore
    private final String password;
}