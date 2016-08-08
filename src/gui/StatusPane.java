package gui;

import client.utils.AvailableAddressFinder;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zfh on 16-5-7.
 */
public class StatusPane extends JPanel {
    private int channels;
    private List<ChannelPane> channelPanes;

    public StatusPane(MainFrame mainFrame) {
        List<InetAddress> localIps = mainFrame.getClientFinder().getList();
        channels = localIps.size();
        channelPanes = new ArrayList<ChannelPane>(channels);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        for (int i = 0; i < channels; i++) {
            channelPanes.add(new ChannelPane(i,localIps.get(i)));
            add(channelPanes.get(i));
        }
        mainFrame.setSelectedChannels(channelPanes);
        TitledBorder titledBorder = BorderFactory.createTitledBorder("通道速率");
        titledBorder.setTitleJustification(TitledBorder.CENTER);
        setBorder(titledBorder);
        setMaximumSize(new Dimension(300, 300));
        setMinimumSize(new Dimension(300, 300));
    }

    public void updateProgress(ConcurrentHashMap<String,ChannelStatus> channelMap){
        for (ChannelPane channelPane:channelPanes){
            ChannelStatus channelStatus=channelMap.get(channelPane.getLocalIp().toString());
            channelPane.setStatus(channelStatus);
            channelStatus.reset();
        }
    }
}
