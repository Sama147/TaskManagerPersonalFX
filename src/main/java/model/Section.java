package model;

import java.util.ArrayList;
import java.util.Collections;      // for Collections.unmodifiableList
import java.util.List;             // for List interface

public class Section
{

    private String name;
    private List<Task> tasks;

    // Constructor
    public Section(String sectionName)
    {
        this.name = sectionName;
        this.tasks = new ArrayList<>();
    }

    //Setters and Getters
    public void setName(String name) {this.name = name;}
    public String getName() {return name;}

    public List<Task> getTasks()
    {
        return Collections.unmodifiableList(tasks); // Correctly returns an unmodifiable view
    }

    public void addTask(Task task)
    {
        if (task != null) {
            tasks.add(task);
        }
    }
    public boolean removeTask(Task task)
    {
        return tasks.remove(task);
    }

}

