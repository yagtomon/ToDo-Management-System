
package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import java.util.Map;
import java.util.Calendar; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.dmm.task.data.entity.Task;
import com.dmm.task.data.repository.TaskRepository;
import com.dmm.task.service.AccountUserDetails;


@Controller
public class MainController {
	@PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private TaskRepository taskRepository;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月");


    @GetMapping({"/", "/main"})
    public String main(
            @RequestParam(name = "date", required = false) String dateStr,
            @AuthenticationPrincipal AccountUserDetails userDetails,
            Model model) {
        
        model.addAttribute("calendars", Calendar.getInstance()); 

        // 1. 表示する月の初日を取得
        LocalDate now = dateStr == null ? LocalDate.now() : LocalDate.parse(dateStr);
        LocalDate monthFirstDate = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthLastDate = now.with(TemporalAdjusters.lastDayOfMonth());

        // 2. 前月と翌月のLocalDateをModelに設定 (前月・翌月へのリンク用)
        model.addAttribute("prev", now.minusMonths(1));
        model.addAttribute("next", now.plusMonths(1));
        model.addAttribute("month", now.format(MONTH_FORMATTER));

        // 3. カレンダーデータ(List<List<LocalDate>>)の生成
        List<List<LocalDate>> matrix = new ArrayList<>();
        List<LocalDate> week = new ArrayList<>();
        
        // 4. 月の初日を基準に、カレンダーの開始日を決定（日曜日始まり）
        LocalDate firstDate = monthFirstDate.minusDays(monthFirstDate.getDayOfWeek().getValue() % 7);
        LocalDate currentDate = firstDate;
        
        // 5. カレンダーを埋める
while (currentDate.isBefore(monthLastDate) || currentDate.isEqual(monthLastDate) || !week.isEmpty()) {
            
            if (currentDate.isAfter(monthLastDate) && week.isEmpty()) {
                break;
            }

            week.add(currentDate);

            if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                matrix.add(week);
                week = new ArrayList<>();
            }
            currentDate = currentDate.plusDays(1);
        
        }
        // 最終週が土曜日で終わらない場合の処理 (翌月分を埋める)
        if (!week.isEmpty()) {
            while (week.size() < 7) {
                week.add(currentDate);
                currentDate = currentDate.plusDays(1);
            }
            // 💡 修正ステップ1：最後の週をmatrixに確実に追加する
            matrix.add(week); 
        }

        model.addAttribute("matrix", matrix);

        List<Task> tasks;
        boolean isAdmin = userDetails.getUser().getRoleName().trim().equals("ROLE_ADMIN"); 

        if (isAdmin) {
            entityManager.clear(); 
            
            tasks = taskRepository.findByDateBetween(firstDate, currentDate.minusDays(1)); 

        } else {
            String name = userDetails.getName();
            tasks = taskRepository.findByDateBetweenAndName(firstDate, currentDate.minusDays(1), name);
        }
        
        Map<LocalDate, List<Task>> tasksMap = tasks.stream()
            .collect(Collectors.groupingBy(Task::getDate));
        
        model.addAttribute("tasks", tasksMap);

        return "main";
    }


    @GetMapping("/main/create/{date}")
    public String create(@PathVariable String date, Model model) {
        LocalDate localDate = LocalDate.parse(date);
        model.addAttribute("date", localDate);
        model.addAttribute("calendars", java.util.Calendar.getInstance()); 
      
        return "create";
    }

    @PostMapping("/main/create")
    public String register(
            Task task, // Taskエンティティを直接受け取る
            @AuthenticationPrincipal AccountUserDetails userDetails) {
        
        // ユーザー情報をタスクに設定
        task.setName(userDetails.getName()); 
        
        // データベースに保存
        taskRepository.save(task);

        // PRGパターンによりカレンダー画面へリダイレクト
        return "redirect:/main"; 
    }


    @GetMapping("/main/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        
        model.addAttribute("calendars", java.util.Calendar.getInstance()); 
        Task task = taskRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + id));
        model.addAttribute("task", task);
        return "edit";
    }

    @PostMapping("/main/edit/{id}")
    @Transactional 
    public String update(
            @PathVariable Integer id,
            Task task,
            @AuthenticationPrincipal AccountUserDetails userDetails) {

        Task existingTask = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + id));

        existingTask.setTitle(task.getTitle());
        existingTask.setDate(task.getDate());
        existingTask.setText(task.getText());
        existingTask.setDone(task.isDone()); 
        
        taskRepository.save(existingTask);
        entityManager.flush(); 

        entityManager.clear();
 
        return "redirect:/main";
    }
    

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    

    @GetMapping("/accessDeniedPage")
    public String accessDenied() {
        return "login";
    }
}