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
import org.springframework.validation.BindingResult; // ★ 追記
import org.springframework.validation.annotation.Validated; // ★ 追記
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
        return "admin".equals(userDetails.getUsername());
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
     * タスク登録画面を表示します (HTMLに合わせてパスを /main/create/{date} に変更)
     */
    @GetMapping("/main/create/{date}")
    public String showCreateForm(@PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, Model model) {
        Task task = new Task();
        task.setDate(date);
        // ★ 必須: Taskオブジェクトを "task" という名前でモデルに追加する
        model.addAttribute("task", task); 
        // ★ 任意: テンプレートで日付表示に使われていた "date" 属性はもう不要かもしれません
        // model.addAttribute("date", date); 
        return "create";
    }

    /**
     * タスクを登録処理します (HTMLに合わせてパスを /main/create に変更)
     */
    @PostMapping("/main/create")
    // ★修正1: @ValidatedとBindingResultを追加してバリデーションを有効にする
    public String registerTask(
            @Validated @ModelAttribute Task task, 
            BindingResult bindingResult, 
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // ★修正2: バリデーションエラーがあればフォームに戻る
        if (bindingResult.hasErrors()) {
            return "create";
        }
        
        // ログインユーザーIDを設定
        task.setUserLoginId(userDetails.getUsername());
        
        taskRepository.save(task);
        
        return "redirect:/main"; // カレンダー画面にリダイレクト
    }


    /**
     * タスク編集画面を表示します (HTMLに合わせてパスを /main/edit/{id} に変更)
     */
    @GetMapping("/main/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        model.addAttribute("task", task);
        return "edit"; // edit.htmlへ遷移
    }
    
    /**
     * タスクを編集・完了処理します (HTMLに合わせてパスを /main/edit/{id} に変更 & IDをURLから取得)
     */
    @PostMapping("/main/edit/{id}")
    // ★修正: @ValidatedとBindingResultを追加してバリデーションを有効にする
    public String editTask(
        @PathVariable Long id, // ★★★ IDをURLパスから取得 ★★★
        @Validated @ModelAttribute Task updatedTask,
        BindingResult bindingResult, // ★ 追記
        @AuthenticationPrincipal UserDetails userDetails) {
        
        // ★追記: バリデーションエラーがあればフォームに戻る
        if (bindingResult.hasErrors()) {
             // エラーが発生した場合、URLからのIDをセットし直してからeditビューを返す
             updatedTask.setId(id);
             return "edit";
        }
        
        // 1. URLから取得したIDをupdatedTaskにセットする (HTMLにhidden idがないため)
        updatedTask.setId(id);
        
        // 2. DBから既存のタスクを取得し、既存データを保護
        Task existingTask = taskRepository.findById(updatedTask.getId())
            .orElseThrow(() -> new RuntimeException("Task not found with ID: " + updatedTask.getId()));
        
        // 3. フォームから送られた値（nullでないもの）をマージし、既存データを維持する

        // 日付 (date): nullでない場合のみ更新。nullの場合は既存の値を維持する
        if (updatedTask.getDate() != null) {
            existingTask.setDate(updatedTask.getDate());
        }

        // タイトル (title): nullでない場合のみ更新
        if (updatedTask.getTitle() != null) {
            existingTask.setTitle(updatedTask.getTitle());
        }

        // 内容 (text): nullでない場合のみ更新
        if (updatedTask.getText() != null) {
            existingTask.setText(updatedTask.getText());
        }
        
        // 完了フラグ (done): フォームから送られてきた値を優先
        existingTask.setDone(updatedTask.isDone());

        // 4. ユーザーIDを設定し、保存
        existingTask.setUserLoginId(userDetails.getUsername());
        taskRepository.save(existingTask);
        
        return "redirect:/main"; // カレンダー画面へ戻る
    }

    /**
     * タスクを削除します (HTMLに合わせてパスを /main/delete/{id} に変更)
     */
    @PostMapping("/main/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskRepository.deleteById(id);
        return "redirect:/main"; // カレンダー画面へ戻る
    }
}