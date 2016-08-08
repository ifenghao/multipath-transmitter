package gui;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Created by zfh on 16-5-22.
 */
public class TaskItemPane extends JPanel {
    private JLabel itemLabel = new JLabel();
    private JLabel nameLabel = new JLabel();
    private JProgressBar bar = new JProgressBar(JProgressBar.HORIZONTAL);

    public TaskItemPane(int id,String method,String file) {
        this.itemLabel.setText(id+". "+method+" :");
        this.nameLabel.setText(file);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        Box vBox1 = Box.createVerticalBox();
        Box vBox2 = Box.createVerticalBox();
        itemLabel.setMinimumSize(new Dimension(150, 20));
        itemLabel.setMaximumSize(new Dimension(150, 20));
        nameLabel.setMinimumSize(new Dimension(150, 20));
        nameLabel.setMaximumSize(new Dimension(150, 20));
        bar.setMinimumSize(new Dimension(300, 30));
        bar.setMaximumSize(new Dimension(300, 30));
        bar.setAlignmentX(RIGHT_ALIGNMENT);
        bar.setStringPainted(true);
        bar.setBorderPainted(true);
        bar.setMinimum(0);
        vBox1.add(itemLabel);
        vBox1.add(nameLabel);
        vBox2.add(Box.createVerticalStrut(5));
        vBox2.add(bar);
        vBox2.add(Box.createVerticalStrut(5));
        add(vBox1);
        add(Box.createHorizontalStrut(10));
        add(vBox2);
        Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);
        setBorder(border);
        setMaximumSize(new Dimension(600, 60));
        setMinimumSize(new Dimension(600, 60));
    }

    public void setProgress(long alreadyLength,long totalLength){
        while (totalLength>Integer.MAX_VALUE){
            totalLength>>=1;
            alreadyLength>>=1;
        }
        bar.setMaximum((int)totalLength);
        bar.setValue((int)alreadyLength);
    }
}
