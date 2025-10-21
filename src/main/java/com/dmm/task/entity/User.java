package com.dmm.task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "users") // データベースのテーブル名
@Data
public class User {
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