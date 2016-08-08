package gui;

import client.utils.AvailableAddressFinder;
import client.utils.ContentBuilder;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zfh on 16-4-22.
 */
public class MainFrame extends JFrame {
    private AvailableAddressFinder clientFinder;
    ExecutorService pool = Executors.newCachedThreadPool();
    private String pathRootFind;
    private String pathRootSave;
    private String address;
    private int port = 0;
    private List<ChannelPane> selectedChannels;
    private ConcurrentHashMap<String,ChannelStatus> channelMap=new ConcurrentHashMap<String, ChannelStatus>();

    private SettingServerPane settingServerPane;
    private StatusPane statusPane;
    private FunctionPane functionPane;
    private TaskPane taskPane;

    public MainFrame() {
        this.pathRootFind = "/home/zfh/find/";
        ContentBuilder.createDir(pathRootFind);
        this.pathRootSave = "/home/zfh/save/";
        ContentBuilder.createDir(pathRootSave);
        try {
            clientFinder = new AvailableAddressFinder();
            settingServerPane = new SettingServerPane(this);
            statusPane = new StatusPane(this);
            functionPane = new FunctionPane(this);
            taskPane = new TaskPane();
        } catch (SocketException e) {
            JOptionPane.showMessageDialog(null, "没有可以使用的网络通道！", "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<InetAddress> localIps = clientFinder.getList();
        for (int i = 0; i < localIps.size(); i++) {
            channelMap.put(localIps.get(i).toString(), new ChannelStatus());
        }
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        settingServerPane.setAlignmentX(LEFT_ALIGNMENT);
        settingServerPane.setAlignmentY(TOP_ALIGNMENT);
        JScrollPane scrollPane1 = new JScrollPane(statusPane);
        scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane1.setMinimumSize(new Dimension(300, 320));
        scrollPane1.setMaximumSize(new Dimension(300, 320));
        scrollPane1.setAlignmentX(LEFT_ALIGNMENT);
        scrollPane1.setAlignmentY(BOTTOM_ALIGNMENT);

        Box vBox1 = Box.createVerticalBox();
        vBox1.add(settingServerPane);
        vBox1.add(Box.createVerticalStrut(5));
        vBox1.add(scrollPane1);

        JScrollPane scrollPane2 = new JScrollPane(taskPane);
        scrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane2.setMinimumSize(new Dimension(600, 370));
        scrollPane2.setMaximumSize(new Dimension(600, 370));
        scrollPane2.setAlignmentX(CENTER_ALIGNMENT);
        scrollPane2.setAlignmentY(BOTTOM_ALIGNMENT);
        Box vBox2=Box.createVerticalBox();
        vBox2.add(functionPane);
        vBox2.add(Box.createVerticalStrut(5));
        vBox2.add(scrollPane2);

        Box top = Box.createHorizontalBox();
        top.add(vBox1);
        top.add(Box.createHorizontalGlue());
        top.add(vBox2);
        add(top);
    }

    public AvailableAddressFinder getClientFinder() {
        return clientFinder;
    }

    public ExecutorService getPool() {
        return pool;
    }

    public String getPathRootFind() {
        return pathRootFind;
    }

    public String getPathRootSave() {
        return pathRootSave;
    }

    public ConcurrentHashMap<String, ChannelStatus> getChannelMap() {
        return channelMap;
    }

    public SettingServerPane getSettingServerPane() {
        return settingServerPane;
    }

    public StatusPane getStatusPane() {
        return statusPane;
    }

    public FunctionPane getFunctionPane() {
        return functionPane;
    }

    public TaskPane getTaskPane() {
        return taskPane;
    }

    public synchronized void setAddress(String address) {
        this.address = address;
    }

    public synchronized void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

    public void setSelectedChannels(List<ChannelPane> selectedChannels) {
        this.selectedChannels = selectedChannels;
    }

    public List<ChannelPane> getSelectedChannels() {
        return selectedChannels;
    }

    public static void main(String[] args) {
        final MainFrame mainFrame = new MainFrame();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mainFrame.setTitle(mainFrame.getClass().getSimpleName());
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mainFrame.setSize(800, 590);
                mainFrame.setVisible(true);
            }
        });
        Timer timer=new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mainFrame.getStatusPane().updateProgress(mainFrame.getChannelMap());
            }
        },1000,1000);
    }
}
