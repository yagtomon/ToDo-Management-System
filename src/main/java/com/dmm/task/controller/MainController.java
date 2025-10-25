package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Calendar; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

        LocalDate now = dateStr == null ? LocalDate.now() : LocalDate.parse(dateStr);
        LocalDate monthFirstDate = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthLastDate = now.with(TemporalAdjusters.lastDayOfMonth());

        model.addAttribute("prev", now.minusMonths(1));
        model.addAttribute("next", now.plusMonths(1));
        model.addAttribute("month", now.format(MONTH_FORMATTER));

        List<List<LocalDate>> matrix = new ArrayList<>();
        List<LocalDate> week = new ArrayList<>();

        LocalDate firstDate = monthFirstDate.minusDays(monthFirstDate.getDayOfWeek().getValue() % 7);
        LocalDate currentDate = firstDate;

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

        if (!week.isEmpty()) {
            while (week.size() < 7) {
                week.add(currentDate);
                currentDate = currentDate.plusDays(1);
            }
            matrix.add(week);
        }

        model.addAttribute("matrix", matrix);

        List<Task> tasks;
        boolean isAdmin = userDetails.getUser().getRoleName().trim().equals("ADMIN"); // ✅ 修正

        if (isAdmin) {
            entityManager.clear();
            tasks = taskRepository.findByDateBetween(monthFirstDate, monthLastDate); // ✅ 修正
        } else {
            String name = userDetails.getName();
            tasks = taskRepository.findByDateBetweenAndName(monthFirstDate, monthLastDate, name);
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
        model.addAttribute("calendars", Calendar.getInstance()); 
        return "create";
    }

    @PostMapping("/main/create")
    public String register(
            Task task,
            @AuthenticationPrincipal AccountUserDetails userDetails) {

        task.setName(userDetails.getName());
        taskRepository.save(task);
        return "redirect:/main";
    }

    @GetMapping("/main/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("calendars", Calendar.getInstance()); 
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + id));
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

    @PostMapping("/main/delete/{id}")
    @Transactional
    public String deleteTask(@PathVariable Integer id) {
        taskRepository.deleteById(id);
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
