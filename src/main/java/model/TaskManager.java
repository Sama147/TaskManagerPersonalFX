package model;

import util.DatabaseManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class TaskManager {
    private Map<String, Section> sectionsMap;
    private DatabaseManager dbManager;

    public TaskManager() {
        System.out.println("TaskManager: Constructor called.");
        this.sectionsMap = new LinkedHashMap<>();
        this.dbManager = new DatabaseManager();

        loadAllDataFromDatabase();
        System.out.println("TaskManager: Data loading initiated on startup.");
    }

    private void loadAllDataFromDatabase() {
        System.out.println("TaskManager: Loading all data from database...");
        sectionsMap.clear();
        Map<String, Section> loadedSections = dbManager.loadAllSections();
        System.out.println("TaskManager: DatabaseManager returned " + loadedSections.size() + " sections.");

        for (Map.Entry<String, Section> entry : loadedSections.entrySet()) {
            String sectionName = entry.getKey();
            Section section = entry.getValue();
            System.out.println("TaskManager: Loading tasks for section '" + sectionName + "'...");
            List<Task> tasksForSection = dbManager.loadTasksForSection(sectionName);
            System.out.println("TaskManager: Found " + tasksForSection.size() + " tasks for section '" + sectionName + "'.");
            for (Task task : tasksForSection) {
                section.addTask(task);
            }
            sectionsMap.put(sectionName, section);
        }
        System.out.println("TaskManager: Finished loading all data. Total sections in memory: " + sectionsMap.size());
        System.out.println("TaskManager: Total tasks in memory (after loading): " + getAllTasks().size());
    }

    public void addSection(String sectionName) {
        System.out.println("TaskManager: Attempting to add section: " + sectionName);
        if (!sectionsMap.containsKey(sectionName)) {
            int sectionId = dbManager.saveSection(sectionName);
            if (sectionId != -1) {
                sectionsMap.put(sectionName, new Section(sectionName));
                System.out.println("TaskManager: Section '" + sectionName + "' added to DB and memory.");
            } else {
                System.err.println("TaskManager: Failed to add section '" + sectionName + "' to database or it already exists.");
            }
        } else {
            System.out.println("TaskManager: Section '" + sectionName + "' already exists in memory.");
        }
    }

    public boolean removeSection(String sectionName) {
        System.out.println("TaskManager: Attempting to remove section: " + sectionName);
        boolean dbDeleted = dbManager.deleteSection(sectionName);
        if (dbDeleted) {
            sectionsMap.remove(sectionName);
            System.out.println("TaskManager: Section '" + sectionName + "' successfully removed from DB and memory.");
            return true;
        } else {
            System.err.println("TaskManager: Failed to remove section '" + sectionName + "' from database or it did not exist.");
            return false;
        }
    }

    public void addTask(String sectionName, Task task) {
        System.out.println("TaskManager: Attempting to add task '" + task.getName() + "' to section '" + sectionName + "'.");
        if (!sectionsMap.containsKey(sectionName)) {
            addSection(sectionName);
        }

        int taskId = dbManager.saveTask(task, sectionName);
        if (taskId != -1) {
            Section targetSection = sectionsMap.get(sectionName);
            if (targetSection != null) {
                targetSection.addTask(task);
                System.out.println("TaskManager: Task '" + task.getName() + "' added to section '" + sectionName + "' in DB and memory.");
            }
        } else {
            System.err.println("TaskManager: Failed to add task '" + task.getName() + "' to database.");
        }
    }

    public boolean removeTask(String sectionName, Task task) {
        System.out.println("TaskManager: Attempting to remove task '" + task.getName() + "' from section '" + sectionName + "'.");
        boolean dbDeleted = dbManager.deleteTask(task.getName(), sectionName);
        if (dbDeleted) {
            Section section = sectionsMap.get(sectionName);
            if (section != null) {
                boolean inMemoryRemoved = section.removeTask(task);
                if (inMemoryRemoved) {
                    System.out.println("TaskManager: Task '" + task.getName() + "' removed from section '" + sectionName + "' from DB and memory.");
                    return true;
                } else {
                    System.err.println("TaskManager: Task '" + task.getName() + "' not found in in-memory section '" + sectionName + "'.");
                    return false;
                }
            } else {
                System.err.println("TaskManager: Section '" + sectionName + "' not found in memory for task removal.");
                return false;
            }
        } else {
            System.err.println("TaskManager: Failed to remove task '" + task.getName() + "' from database.");
            return false;
        }
    }

    public List<String> getSectionNames() {
        List<String> names = new ArrayList<>(sectionsMap.keySet());
        System.out.println("TaskManager: getSectionNames() called. Returning " + names.size() + " section names.");
        return names;
    }

    public List<Task> getTasksForSection(String sectionName) {
        Section section = sectionsMap.get(sectionName);
        List<Task> tasks = (section != null) ? section.getTasks() : Collections.emptyList();
        System.out.println("TaskManager: getTasksForSection('" + sectionName + "') called. Returning " + tasks.size() + " tasks.");
        return tasks;
    }

    public List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>();
        for (Section section : sectionsMap.values()) {
            allTasks.addAll(section.getTasks());
        }
        System.out.println("TaskManager: getAllTasks() called. Returning " + allTasks.size() + " total tasks.");
        return allTasks;
    }

    public List<Task> searchTasksByName(String searchTerm) {
        System.out.println("TaskManager: searchTasksByName('" + searchTerm + "') called.");
        List<Task> matchingTasks = new ArrayList<>();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return matchingTasks;
        }

        String lowerCaseSearchTerm = searchTerm.toLowerCase();

        for (Section section : sectionsMap.values()) {
            for (Task task : section.getTasks()) {
                if (task.getName().toLowerCase().contains(lowerCaseSearchTerm)) {
                    matchingTasks.add(task);
                }
            }
        }
        System.out.println("TaskManager: searchTasksByName('" + searchTerm + "') found " + matchingTasks.size() + " tasks.");
        return matchingTasks;
    }

    public List<Task> getSortedTasksByDueDateAndPriority() {
        System.out.println("TaskManager: getSortedTasksByDueDateAndPriority() called.");
        List<Task> allTasks = getAllTasks();
        System.out.println("TaskManager: Sorting " + allTasks.size() + " tasks.");

        Collections.sort(allTasks, new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                int dateComparison = task1.getDueDate().compareTo(task2.getDueDate());
                if (dateComparison != 0) {
                    return dateComparison;
                }
                int priority1Value = getPriorityValue(task1.getPriority());
                int priority2Value = getPriorityValue(task2.getPriority());
                return Integer.compare(priority2Value, priority1Value);
            }

            private int getPriorityValue(Priority priority) {
                switch (priority) {
                    case HIGH: return 3;
                    case MODERATE: return 2;
                    case LOW: return 1;
                    default: return 0;
                }
            }
        });
        System.out.println("TaskManager: Tasks sorted.");
        return allTasks;
    }
}
