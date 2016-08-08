package gui;

import client.Receiver;
import client.utils.AvailableAddressFinder;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;

/**
 * Created by zfh on 16-5-7.
 */
public class ReceivePane extends JPanel {
    private JButton receiveButton = new JButton("开始接收");
    private JButton scanButton = new JButton("查看文件");
    private JTextArea textArea = new JTextArea(8, 20);

    public ReceivePane(final MainFrame mainFrame) {
        receiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String address = mainFrame.getAddress();
                int port = mainFrame.getPort();
                if (address == null || port == 0) {
                    JOptionPane.showMessageDialog(null, "未知的服务器地址和端口", "警告", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String files = textArea.getText();
                files = files.trim();
                if (files.equals("")) {
                    JOptionPane.showMessageDialog(null, "没有需要接收的文件", "警告", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String[] fileArray = files.split("\\s+");
                List<InetAddress> selectedLocalIps = new ArrayList<InetAddress>();
                for (ChannelPane channel : mainFrame.getSelectedChannels()){
                    if (channel.isSelected()){
                        selectedLocalIps.add(channel.getLocalIp());
                    }
                }
                for (String file : fileArray) {
                    Receiver receiver=new Receiver(address, port, selectedLocalIps, 0, file,
                            mainFrame.getPathRootSave(), mainFrame);
                    mainFrame.getTaskPane().addTask(receiver,file);
                    mainFrame.getPool().submit(receiver);
                }
            }
        });
        scanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(new File(mainFrame.getPathRootSave()));
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, "保存目录不存在", "警告", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        textArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(300, 200));
        scrollPane.setMaximumSize(new Dimension(300, 200));
        Box hBox = Box.createHorizontalBox();
        receiveButton.setAlignmentX(CENTER_ALIGNMENT);
        scanButton.setAlignmentX(CENTER_ALIGNMENT);
        hBox.add(receiveButton);
        hBox.add(Box.createHorizontalStrut(20));
        hBox.add(scanButton);
        add(scrollPane);
        add(Box.createVerticalStrut(10));
        add(hBox);
        TitledBorder titledBorder = BorderFactory.createTitledBorder("接收文件");
        titledBorder.setTitleJustification(TitledBorder.CENTER);
        setBorder(titledBorder);
        setMaximumSize(new Dimension(300, 200));
        setMinimumSize(new Dimension(300, 200));
    }
}
