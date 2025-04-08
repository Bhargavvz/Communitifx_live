package com.cofix.cofixBackend.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(schema = "${cofix.schema.name}", name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyUser {

    @Id
    private String email;
    private String name;
    private String password;
    @Column(name = "nick_name")
    private String nickName;
    @Column(name = "phone_number")
    private String phoneNumber;
    private String country;
    private String gender;
    private String address;
    @Column(name = "create_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime createDate;

    public MyUser(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.createDate = LocalDateTime.now();
    }

    public String getFormattedDate() {
        if (createDate != null) {
            return createDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
        }
        return "";
    }

}
