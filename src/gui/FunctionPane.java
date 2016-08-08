package gui;

import client.utils.AvailableAddressFinder;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by zfh on 16-5-7.
 */
public class FunctionPane extends JPanel {
    private ReceivePane receivePane;
    private SendPane sendPane;

    public FunctionPane(final MainFrame mainFrame) {
        receivePane = new ReceivePane(mainFrame);
        sendPane = new SendPane(mainFrame);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Box hBox = Box.createHorizontalBox();
        hBox.add(receivePane);
        hBox.add(Box.createHorizontalStrut(10));
        hBox.add(sendPane);
        add(hBox);
        setMaximumSize(new Dimension(600, 200));
        setMinimumSize(new Dimension(600, 200));
    }
}
