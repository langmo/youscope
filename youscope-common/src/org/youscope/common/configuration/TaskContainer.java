/**
 * 
 */
package org.youscope.common.configuration;

/**
 * @author langmo
 */
public interface TaskContainer
{
    /**
     * Returns the tasks in the container.
     * 
     * @return Tasks.
     */
    public TaskConfiguration[] getTasks();
    
    /**
     * Returns the number of tasks in the container.
     * @return number of tasks.
     */
    public int getNumTasks();
    
    /**
     * Returns the task at the given position.
     * @param taskID task position.
     * @return task at given position.
     * @throws IndexOutOfBoundsException thrown if task index is invalid.
     */
    public TaskConfiguration getTask(int taskID) throws IndexOutOfBoundsException;
    
    /**
     * Removes the task at the given index.
     * @param taskID Task id to remove.
     * @throws IndexOutOfBoundsException
     */
    public void removeTask(int taskID) throws IndexOutOfBoundsException;
    
    /**
     * Adds a new task at the end of the tasks.
     * @param taskConfiguration Task to add.
     */
    public void addTask(TaskConfiguration taskConfiguration);
    
    /**
     * Inserts a task at the given position in the task list. The indices of the task having previously the given index and of all tasks with a higher index is increased by one.
     * @param taskConfiguration Task to add.
     * @param taskID Index where to insert the task.
     * @throws IndexOutOfBoundsException
     */
    public void insertTask(TaskConfiguration taskConfiguration, int taskID) throws IndexOutOfBoundsException;
}
