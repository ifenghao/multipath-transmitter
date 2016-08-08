package gui;

import client.Sender;
import client.utils.AvailableAddressFinder;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by zfh on 16-5-7.
 */
public class SendPane extends JPanel {
    private JButton sendButton = new JButton("开始发送");
    private JButton selectButton = new JButton("选择文件");
    private JTextArea textArea = new JTextArea(8, 20);

    public SendPane(final MainFrame mainFrame) {
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(mainFrame.getPathRootFind()));
                fileChooser.setMultiSelectionEnabled(true);
                fileChooser.setDialogTitle("选择要发送的文件");
                fileChooser.showDialog(null, "选择");
                if (fileChooser.getSelectedFiles().length == 0) {
                    return;
                }
                StringBuilder filesBuilder = new StringBuilder();
                for (File file : fileChooser.getSelectedFiles()) {
                    filesBuilder.append(file.toString());
                    filesBuilder.append("\n");
                }
                String files = filesBuilder.toString();
                files = files.substring(0, files.lastIndexOf("\n"));
                textArea.setText(files);
            }
        });
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String address = mainFrame.getAddress();
                int port = mainFrame.getPort();
                if (address == null || port == 0) {
                    JOptionPane.showMessageDialog(null, "未知的服务器地址和端口", "警告", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String files = textArea.getText();
                if (files.equals("")) {
                    JOptionPane.showMessageDialog(null, "没有需要发送的文件", "警告", JOptionPane.WARNING_MESSAGE);
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
                    int end = file.lastIndexOf(File.separator);
                    String pathRoot = file.substring(0, end + 1);
                    String fileName = file.substring(end + 1);
                    Sender sender=new Sender(address, port, selectedLocalIps, 0, fileName, pathRoot,mainFrame);
                    mainFrame.getTaskPane().addTask(sender,fileName);
                    mainFrame.getPool().submit(sender);
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
        selectButton.setAlignmentX(CENTER_ALIGNMENT);
        sendButton.setAlignmentX(CENTER_ALIGNMENT);
        hBox.add(selectButton);
        hBox.add(Box.createHorizontalStrut(20));
        hBox.add(sendButton);
        add(scrollPane);
        add(Box.createVerticalStrut(10));
        add(hBox);
        TitledBorder titledBorder = BorderFactory.createTitledBorder("发送文件");
        titledBorder.setTitleJustification(TitledBorder.CENTER);
        setBorder(titledBorder);
        setMaximumSize(new Dimension(300, 200));
        setMinimumSize(new Dimension(300, 200));
    }
}
