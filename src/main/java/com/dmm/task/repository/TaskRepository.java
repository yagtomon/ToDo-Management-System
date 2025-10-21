package com.dmm.task.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.dmm.task.entity.Task;

// ★★★ 修正箇所: Integer を Long に変更する ★★★
public interface TaskRepository extends CrudRepository<Task, Long> {

    // 既存のカスタムメソッドがあればそのまま残す
    List<Task> findByDateBetween(LocalDate from, LocalDate to);
    List<Task> findByUserLoginIdAndDateBetween(String userLoginId, LocalDate from, LocalDate to);
}