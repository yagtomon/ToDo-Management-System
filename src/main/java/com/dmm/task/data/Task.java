package com.dmm.task.data;

import java.time.LocalDate;

// ★★★ 削除されてしまうインポートをすべて記述する ★★★
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
// 以下の1行を追加する
import javax.validation.constraints.NotNull; // 👈 この行を追加

@Entity
@Table(name = "tasks")
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(nullable = true)
    private String text;
    
    @Column(name = "user_login_id", nullable = false)
    private String userLoginId;

    @Column(nullable = false)
    @NotNull(message = "実施日を入力してください")
    private LocalDate date; // 実施日
    
    private boolean done = false; // 完了フラグ

    // --- getter / setter ---
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public String getUserLoginId() {
        return userLoginId;
    }
    public void setUserLoginId(String userLoginId) {
        this.userLoginId = userLoginId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public boolean isDone() {
        return done;
    }
    public void setDone(boolean done) {
        this.done = done;
    }
}