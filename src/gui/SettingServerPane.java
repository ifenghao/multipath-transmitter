package gui;

import client.Checker;
import client.utils.AvailableAddressFinder;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by zfh on 16-5-7.
 */
public class SettingServerPane extends JPanel {
    private JLabel addressLabel = new JLabel("服务器地址:");
    private JLabel portLabel = new JLabel("服务器端口:");
    private JTextField addressText = new JTextField("10.13.88.15", 10);
    private JTextField portText = new JTextField("8080", 10);
    private JButton testButton = new JButton("测试连通");
    private JButton checkButton = new JButton("开始检查");
    private JButton settingButton = new JButton("设置网络");
    private JTextArea checkTextArea = new JTextArea(5, 15);

    public SettingServerPane(final MainFrame mainFrame) {
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String address = addressText.getText();
                int port = Integer.parseInt(portText.getText());
                mainFrame.setAddress(address);
                mainFrame.setPort(port);
                List<InetAddress> localIps = mainFrame.getClientFinder().getList();
                StringBuilder message = new StringBuilder();
                for (int i = 0; i < localIps.size(); i++) {
                    mainFrame.getChannelMap().put(localIps.get(i).toString(),new ChannelStatus());
                    if (isReachable(localIps.get(i), address, port)) {
                        message.append("通道" + i + "可以连接\n");
                    } else {
                        message.append("通道" + i + "不可连接\n");
                    }
                }
                JOptionPane.showMessageDialog(null, message.toString(), "通道连通性测试", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String address = mainFrame.getAddress();
                int port = mainFrame.getPort();
                if (address == null || port == 0) {
                    JOptionPane.showMessageDialog(null, "未知的服务器地址和端口", "警告", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                List<InetAddress> localIps = mainFrame.getClientFinder().getList();
                Future<String> files = mainFrame.getPool().submit(new Checker(address, port, localIps, 0));
                try {
                    checkTextArea.setText(files.get());
                } catch (InterruptedException e1) {
                    JOptionPane.showMessageDialog(null, e1.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException e2) {
                    JOptionPane.showMessageDialog(null, e2.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Box hBox1 = Box.createHorizontalBox();
        Box hBox2 = Box.createHorizontalBox();
        Box hBox3 = Box.createHorizontalBox();
        addressText.setMinimumSize(new Dimension(200, 30));
        addressText.setMaximumSize(new Dimension(200, 30));
        portText.setMinimumSize(new Dimension(200, 30));
        portText.setMaximumSize(new Dimension(200, 30));
        hBox1.add(addressLabel);
        hBox1.add(Box.createHorizontalStrut(10));
        hBox1.add(addressText);
        hBox1.setAlignmentY(TOP_ALIGNMENT);
        hBox2.add(portLabel);
        hBox2.add(Box.createHorizontalStrut(10));
        hBox2.add(portText);
        hBox2.setAlignmentY(TOP_ALIGNMENT);

        testButton.setAlignmentX(LEFT_ALIGNMENT);
        settingButton.setAlignmentX(CENTER_ALIGNMENT);
        checkButton.setAlignmentX(RIGHT_ALIGNMENT);
        hBox3.add(testButton);
        hBox3.add(Box.createHorizontalStrut(15));
        hBox3.add(checkButton);
        hBox3.add(Box.createHorizontalStrut(15));
        hBox3.add(settingButton);
        hBox3.setAlignmentY(TOP_ALIGNMENT);

        checkTextArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(checkTextArea);
        scrollPane.setMinimumSize(new Dimension(300, 200));
        scrollPane.setMaximumSize(new Dimension(300, 200));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        TitledBorder titledCheckBorder = BorderFactory.createTitledBorder("检查资源");
        titledCheckBorder.setTitleJustification(TitledBorder.CENTER);
        scrollPane.setBorder(titledCheckBorder);

        add(hBox1);
        add(hBox2);
        add(hBox3);
        add(scrollPane);
        TitledBorder titledBorder = BorderFactory.createTitledBorder("服务器设置");
        titledBorder.setTitleJustification(TitledBorder.CENTER);
        setBorder(titledBorder);
        setMaximumSize(new Dimension(300, 250));
        setMinimumSize(new Dimension(300, 250));
    }

    public boolean isReachable(InetAddress localIp, String remoteAddress, int remotePort) {
        SocketAddress localSocket = new InetSocketAddress(localIp, 0);
        SocketAddress remoteSocket = new InetSocketAddress(remoteAddress, remotePort);
        Socket socket = new Socket();
        try {
            socket.bind(localSocket);
            socket.connect(remoteSocket, 1000);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
