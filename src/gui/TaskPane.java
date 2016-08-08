package gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zfh on 16-5-7.
 */
public class TaskPane extends JPanel {
    private ConcurrentHashMap<Callable<Void>,TaskItemPane> taskMap=new ConcurrentHashMap<Callable<Void>, TaskItemPane>();

    public TaskPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        TitledBorder titledBorder = BorderFactory.createTitledBorder("任务");
        titledBorder.setTitleJustification(TitledBorder.CENTER);
        setBorder(titledBorder);
        setMaximumSize(new Dimension(500, 350));
        setMinimumSize(new Dimension(500, 350));
    }

    public void addTask(Callable<Void> task, String file){
        TaskItemPane taskItem = new TaskItemPane(taskMap.size(),task.getClass().getSimpleName(),file);
        taskMap.put(task,taskItem);
        add(taskItem);
        add(Box.createVerticalStrut(5));
        validate();
        repaint();
    }

    public ConcurrentHashMap<Callable<Void>, TaskItemPane> getTaskMap() {
        return taskMap;
    }
}
