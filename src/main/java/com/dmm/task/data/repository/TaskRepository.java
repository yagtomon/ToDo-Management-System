
package com.dmm.task.data.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.dmm.task.data.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    // 特定期間の全ユーザーのタスクを取得 (管理者用)
    @Query("select a from tasks a where a.date between :from and :to")
    List<Task> findByDateBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // 特定期間の特定ユーザーのタスクを取得 (一般ユーザー用)
    @Query("select a from tasks a where a.date between :from and :to and name = :name")
    List<Task> findByDateBetweenAndName(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("name") String name);
}