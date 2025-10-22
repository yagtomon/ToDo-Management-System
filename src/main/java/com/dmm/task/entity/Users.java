package com.dmm.task.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "users") // データベースのテーブル名
@Data
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "login_id", unique = true, nullable = false)
    private String loginId;
    
    @Column(nullable = false)
    private String password;

    // 管理者要件のため、権限を識別するカラム (例: role) が必要ですが、
    // シンプルな実装のためここでは省略し、loginIdが"admin"か否かで判断するとします。
    // 実際の開発ではRoleエンティティと関連付けることが望ましいです。
}