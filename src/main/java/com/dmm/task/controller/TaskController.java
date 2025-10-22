package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; 
import org.springframework.validation.annotation.Validated; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dmm.task.entity.Task;
import com.dmm.task.repository.TaskRepository;

@Controller
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    // 現在の認証ユーザーが管理者かチェックするヘルパーメソッド
    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * カレンダー画面を表示します (URL: / または /main)
     */
    @GetMapping({"/", "/main"})
    public String index(
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        // 閲覧対象月を決定 (パラメータがない場合は現在月)
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        LocalDate firstDayOfMonth = targetDate.withDayOfMonth(1);
        LocalDate lastDayOfMonth = targetDate.withDayOfMonth(targetDate.lengthOfMonth());
        List<Task> tasks;
        if (isAdmin(userDetails)) {
            tasks = taskRepository.findByDateBetween(firstDayOfMonth, lastDayOfMonth);
        } else {
            tasks = taskRepository.findByUserLoginIdAndDateBetween(
                userDetails.getUsername(), firstDayOfMonth, lastDayOfMonth
            );
        }
        
        Map<LocalDate, List<Task>> tasksMap = new HashMap<>();
        for (Task task : tasks) {
            tasksMap.computeIfAbsent(task.getDate(), k -> new ArrayList<>()).add(task);
        }
        
        String month = targetDate.format(DateTimeFormatter.ofPattern("yyyy年M月"));
        model.addAttribute("month", month);
        LocalDate prev = firstDayOfMonth.minusMonths(1);
        LocalDate next = firstDayOfMonth.plusMonths(1);
        model.addAttribute("prev", prev);
        model.addAttribute("next", next);
        model.addAttribute("tasks", tasksMap);
        
        LocalDate calendarStart = firstDayOfMonth;
        while (calendarStart.getDayOfWeek() != DayOfWeek.SUNDAY) {
             calendarStart = calendarStart.minusDays(1);
        }
        
        List<List<LocalDate>> matrix = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            List<LocalDate> week = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                week.add(calendarStart.plusDays(i * 7 + j));
            }
            matrix.add(week);
        }
        model.addAttribute("matrix", matrix);
        model.addAttribute("loginUser", userDetails.getUsername());

        return "main"; // main.htmlへ遷移
    }

    /**
     * ログイン画面を表示します
     */
    @GetMapping("/login")
    public String login() {
        return "login"; // login.htmlへ遷移
    }

    /**
     * タスク登録画面を表示します
     */
    @GetMapping("/main/create/{date}")
    public String showCreateForm(
        @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, 
        Model model) {
        
        Task task = new Task();
        task.setDate(date);
        model.addAttribute("task", task);
        
        return "create";
    }

    /**
     * タスクを登録処理します
     */
    @PostMapping("/main/create")
    public String registerTask(
            @Validated @ModelAttribute Task task,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (bindingResult.hasErrors()) {
            return "create";
        }
        
        task.setUserLoginId(userDetails.getUsername());
        
        taskRepository.save(task);
        
        return "redirect:/main"; // カレンダー画面にリダイレクト
    }

    // ----------------------------------------------------------------------
    // ★★★★ 以下のメソッドを編集フォーム表示用に修正 ★★★★
    // ----------------------------------------------------------------------
    /**
     * タスク編集画面を表示します (GET /main/edit/{id})
     */
    @GetMapping("/main/edit/{id}")
    public String showEditForm(
        @PathVariable Long id, 
        Model model) {
        
        // IDに基づいてDBから既存のタスクを取得
        Task existingTask = taskRepository.findById(id)
             .orElseThrow(() -> new RuntimeException("Task not found with ID: " + id));
        
        // フォームに表示するためモデルに追加
        model.addAttribute("task", existingTask);
        
        return "edit"; // edit.htmlへ遷移
    }
    
    // ----------------------------------------------------------------------
    // ★★★★ 以下のメソッドを更新処理用に修正・統合 ★★★★
    // ----------------------------------------------------------------------
    /**
     * タスクを編集・完了処理します (POST /main/edit/{id})
     */
    @PostMapping("/main/edit/{id}")
    public String updateTask(
        @PathVariable Long id, 
        @Validated @ModelAttribute("task") Task updatedTask,
        BindingResult bindingResult, 
        @AuthenticationPrincipal UserDetails userDetails) {
        
        // 1. バリデーションエラーがあればフォームに戻る
        if (bindingResult.hasErrors()) {
            updatedTask.setId(id);
            return "edit";
        }
        
     // 2. IDとユーザーIDを設定し、フォームのデータをそのまま保存
        // ★★★ 既存の複雑なマージロジックを削除し、この2行に置き換える ★★★
        updatedTask.setId(id); 
        updatedTask.setUserLoginId(userDetails.getUsername());
        
        taskRepository.save(updatedTask); // IDがあれば更新として動作する
        
        return "redirect:/main"; // カレンダー画面へ戻る
    }

    /**
     * タスクを削除します
     */
    @PostMapping("/main/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskRepository.deleteById(id);
        return "redirect:/main"; // カレンダー画面へ戻る
    }
}