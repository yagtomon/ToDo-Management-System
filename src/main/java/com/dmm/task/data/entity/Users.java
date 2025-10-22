package com.dmm.task.data.entity; 

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString(exclude = "password")
public class Users {
    
    @Id
    public String loginId; // DBの列名 'login_id' に対応
    
    public String password; // DBの列名 'password' に対応

}