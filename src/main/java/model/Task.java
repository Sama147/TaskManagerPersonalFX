package model;
import java.util.Date;
import java.text.SimpleDateFormat;
//used for transforming to readable date

public class Task
{
    private String name;
    private Priority priority;
    private Date dueDate;
    private int id;

    //constructor
    public Task (String name, Priority priority, Date dueDate)
    {
        this.name = name;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    //Setters and Getters
    public void setName(String name)
    {
        this.name= name;
    }
    public void setPriority(Priority priority)
    {
        this.priority= priority;
    }
    public void setDate(Date dueDate)
    {
        this.dueDate=dueDate;
    }

    public String getName()
    {
        return name;
    }
    public Priority getPriority()
    {
        return priority;
    }
    public Date getDueDate()
    {
        return dueDate;
    }

    //For reformatting
    @Override // This annotation is now correct because the method name matches the superclass method
    public String toString() // Corrected: 'public' (lowercase) and 'toString' (lowercase 't')
    {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd"); // Consider adding year: "MMM dd, yyyy"
        return name + "( Priority: " + priority + " Due: " + sdf.format(dueDate) + " )";
    }
}



