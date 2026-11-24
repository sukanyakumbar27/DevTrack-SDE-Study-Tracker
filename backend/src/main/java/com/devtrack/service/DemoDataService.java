package com.devtrack.service;

import com.devtrack.entity.*;
import com.devtrack.entity.Enums.*;
import com.devtrack.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Creates the Java topic checklist for every new user, and optional demo data so charts are not empty. */
@Service
public class DemoDataService {
    private static final List<String> JAVA_TOPICS = List.of(
            "Java Fundamentals", "Data Types and Operators", "Conditional Statements", "Loops", "Methods",
            "Arrays", "Strings", "OOP", "Collections Framework", "Exception Handling", "Multithreading", "JDBC");

    private final JavaTopicRepository javaTopics;
    private final StudyTaskRepository tasks;
    private final StudySessionRepository sessions;
    private final DsaProblemRepository problems;
    private final ProjectRepository projects;
    private final ProjectTaskRepository projectTasks;
    private final NoteRepository notes;

    public DemoDataService(JavaTopicRepository javaTopics, StudyTaskRepository tasks, StudySessionRepository sessions,
                           DsaProblemRepository problems, ProjectRepository projects,
                           ProjectTaskRepository projectTasks, NoteRepository notes) {
        this.javaTopics = javaTopics;
        this.tasks = tasks;
        this.sessions = sessions;
        this.problems = problems;
        this.projects = projects;
        this.projectTasks = projectTasks;
        this.notes = notes;
    }

    public void seedJavaTopics(User user) {
        List<JavaTopic> list = new ArrayList<>();
        for (int i = 0; i < JAVA_TOPICS.size(); i++) {
            JavaTopic t = new JavaTopic();
            t.setUser(user);
            t.setName(JAVA_TOPICS.get(i));
            t.setSortOrder(i);
            list.add(t);
        }
        javaTopics.saveAll(list);
    }

    public void seedSampleData(User user) {
        LocalDate today = LocalDate.now();

        // Java progress: first three topics done
        List<JavaTopic> topics = javaTopics.findByUserIdOrderBySortOrderAsc(user.getId());
        for (int i = 0; i < 3 && i < topics.size(); i++) {
            topics.get(i).setCompleted(true);
            topics.get(i).setCompletedAt(LocalDateTime.now().minusDays(10 - i));
        }
        topics.get(0).setNotes("JVM, JRE and JDK are different things. Java is compiled to bytecode.");
        javaTopics.saveAll(topics);

        // Tasks over the last 10 days (older days mostly completed, today partly)
        String[][] templates = {
                {"Solve 3 array problems", "DSA", "HIGH", "90"},
                {"Read OOP chapter", "Java", "MEDIUM", "60"},
                {"Revise SQL joins", "DBMS", "MEDIUM", "45"},
                {"Build a REST endpoint", "Spring Boot", "HIGH", "75"}};
        for (int d = 9; d >= 0; d--) {
            for (int k = 0; k < templates.length; k++) {
                if (d > 0 && (d + k) % 5 == 0) continue; // skip some, so days differ
                boolean completed = d > 0 ? (d + k) % 4 != 0 : k < 2;
                StudyTask t = new StudyTask();
                t.setUser(user);
                t.setTitle(templates[k][0]);
                t.setSubject(templates[k][1]);
                t.setPriority(Priority.valueOf(templates[k][2]));
                t.setEstimatedMinutes(Integer.parseInt(templates[k][3]));
                t.setScheduledDate(today.minusDays(d));
                if (completed) {
                    t.setStatus(TaskStatus.COMPLETED);
                    t.setCompletedAt(today.minusDays(d).atTime(18, 0));
                    t.setActualMinutes(Integer.parseInt(templates[k][3]));
                } else if (d == 0 && k == 2) {
                    t.setStatus(TaskStatus.IN_PROGRESS);
                }
                tasks.save(t);

                if (completed) {
                    StudySession s = new StudySession();
                    s.setUser(user);
                    s.setTask(t);
                    s.setSubject(t.getSubject());
                    s.setMinutes(t.getActualMinutes());
                    s.setSessionDate(t.getScheduledDate());
                    sessions.save(s);
                }
            }
        }
        // A few upcoming tasks for the calendar
        for (int d = 1; d <= 3; d++) {
            StudyTask t = new StudyTask();
            t.setUser(user);
            t.setTitle("Practice " + (d == 1 ? "linked lists" : d == 2 ? "binary search" : "recursion"));
            t.setSubject("DSA");
            t.setPriority(Priority.MEDIUM);
            t.setEstimatedMinutes(60);
            t.setScheduledDate(today.plusDays(d));
            tasks.save(t);
        }

        // DSA problems
        Object[][] dsa = {
                {"Two Sum", "LeetCode", DsaTopic.HASHING, Difficulty.EASY, 8},
                {"Best Time to Buy and Sell Stock", "LeetCode", DsaTopic.ARRAYS, Difficulty.EASY, 7},
                {"Valid Parentheses", "LeetCode", DsaTopic.STACKS_AND_QUEUES, Difficulty.EASY, 6},
                {"Reverse Linked List", "LeetCode", DsaTopic.LINKED_LISTS, Difficulty.EASY, 4},
                {"Binary Search", "LeetCode", DsaTopic.BINARY_SEARCH, Difficulty.EASY, 3},
                {"Maximum Subarray", "LeetCode", DsaTopic.DYNAMIC_PROGRAMMING, Difficulty.MEDIUM, 2},
                {"Merge Intervals", "LeetCode", DsaTopic.SORTING, Difficulty.MEDIUM, 1},
                {"Number of Islands", "LeetCode", DsaTopic.GRAPHS, Difficulty.MEDIUM, 0}};
        for (Object[] row : dsa) {
            DsaProblem p = new DsaProblem();
            p.setUser(user);
            p.setName((String) row[0]);
            p.setPlatform((String) row[1]);
            p.setTopic((DsaTopic) row[2]);
            p.setDifficulty((Difficulty) row[3]);
            p.setStatus(ProblemStatus.SOLVED);
            p.setSolvedDate(today.minusDays((Integer) row[4]));
            p.setNotes("Sample entry. Replace with your own approach notes.");
            problems.save(p);
        }

        // One project with tasks
        Project project = new Project();
        project.setUser(user);
        project.setName("Portfolio Website");
        project.setDescription("Personal portfolio built with React.");
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setGithubUrl("https://github.com/your-username/portfolio");
        projects.save(project);
        String[] pt = {"Design wireframes", "Build home page", "Add projects section", "Deploy to hosting"};
        for (int i = 0; i < pt.length; i++) {
            ProjectTask t = new ProjectTask();
            t.setUser(user);
            t.setProject(project);
            t.setTitle(pt[i]);
            t.setMilestone(i == 3);
            t.setDone(i < 2);
            projectTasks.save(t);
        }

        // Notes
        Note n1 = new Note();
        n1.setUser(user);
        n1.setTitle("HashMap vs HashSet");
        n1.setContent("HashMap stores key-value pairs. HashSet stores unique values and uses a HashMap internally.");
        n1.setCategory(NoteCategory.CONCEPT);
        notes.save(n1);
        Note n2 = new Note();
        n2.setUser(user);
        n2.setTitle("Tell me about yourself");
        n2.setContent("Keep it to 60 seconds: background, skills, one project, why this role.");
        n2.setCategory(NoteCategory.INTERVIEW);
        notes.save(n2);
    }
}
