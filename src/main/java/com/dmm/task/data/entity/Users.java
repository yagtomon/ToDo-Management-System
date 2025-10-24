// src/main/java/com/dmm/task/data/entity/Users.java
package com.dmm.task.data.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;
import lombok.ToString;

@Data
@Entity // データベースの情報を格納する
@ToString(exclude = "password") // パスワードをログ出力から除外
public class Users {
    @Id
    public String userName;
    public String password;
    public String name;
    public String roleName;
}