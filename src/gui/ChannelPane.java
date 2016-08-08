package gui;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;

/**
 * Created by zfh on 16-5-16.
 */
public class ChannelPane extends JPanel {
    private int id;
    private InetAddress localIp;

    private JCheckBox checkBox = new JCheckBox();
    private JLabel channelLabel = new JLabel();
    private JLabel rxLabel = new JLabel("RX(kB/s):");
    private JLabel txLabel = new JLabel("TX(kB/s):");
    private JLabel rxsLabel = new JLabel("0");
    private JLabel txsLabel = new JLabel("0");
    private JProgressBar rxBar = new JProgressBar(JProgressBar.HORIZONTAL);
    private JProgressBar txBar = new JProgressBar(JProgressBar.HORIZONTAL);

    public ChannelPane(int id ,InetAddress localIp) {
        this.id=id;
        this.localIp=localIp;
        channelLabel.setText("channel"+id+" IP:"+localIp);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Box hBox1 = Box.createHorizontalBox();
        Box hBox2 = Box.createHorizontalBox();
        Box hBox3 = Box.createHorizontalBox();
        Box vBox1 = Box.createVerticalBox();
        Box vBox2 = Box.createVerticalBox();
        Box vBox3 = Box.createVerticalBox();
        Box vBox4 = Box.createVerticalBox();
        checkBox.setSelected(true);
        hBox1.add(checkBox);
        hBox1.add(Box.createHorizontalStrut(20));
        hBox1.add(channelLabel);
        rxLabel.setMinimumSize(new Dimension(80, 20));
        rxLabel.setMaximumSize(new Dimension(80, 20));
        rxsLabel.setMinimumSize(new Dimension(80, 20));
        rxsLabel.setMaximumSize(new Dimension(80, 20));
        rxBar.setMinimumSize(new Dimension(150, 30));
        rxBar.setMaximumSize(new Dimension(150, 30));
        rxBar.setBorderPainted(true);
        rxBar.setMinimum(0);
        rxBar.setMaximum(15000000);
        vBox1.add(rxLabel);
        vBox1.add(rxsLabel);
        vBox2.add(Box.createVerticalStrut(5));
        vBox2.add(rxBar);
        vBox2.add(Box.createVerticalStrut(5));
        hBox2.add(vBox1);
        hBox2.add(Box.createHorizontalStrut(10));
        hBox2.add(vBox2);
        txLabel.setMinimumSize(new Dimension(80, 20));
        txLabel.setMaximumSize(new Dimension(80, 20));
        txsLabel.setMinimumSize(new Dimension(80, 20));
        txsLabel.setMaximumSize(new Dimension(80, 20));
        txBar.setMinimumSize(new Dimension(150, 30));
        txBar.setMaximumSize(new Dimension(150, 30));
        txBar.setBorderPainted(true);
        txBar.setMinimum(0);
        txBar.setMaximum(15000000);
        vBox3.add(txLabel);
        vBox3.add(txsLabel);
        vBox4.add(Box.createVerticalStrut(5));
        vBox4.add(txBar);
        vBox4.add(Box.createVerticalStrut(5));
        hBox3.add(vBox3);
        hBox3.add(Box.createHorizontalStrut(10));
        hBox3.add(vBox4);
        add(hBox1);
        add(Box.createVerticalGlue());
        add(hBox2);
        add(Box.createVerticalGlue());
        add(hBox3);
        Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);
        setBorder(border);
        setMaximumSize(new Dimension(300, 100));
        setMinimumSize(new Dimension(300, 100));
    }

    public boolean isSelected(){
        return checkBox.isSelected();
    }

    public void setStatus(ChannelStatus channelStatus){
        rxsLabel.setText(""+channelStatus.getRx()/1000.0);
        txsLabel.setText(""+channelStatus.getTx()/1000.0);
        rxBar.setValue(channelStatus.getRx());
        txBar.setValue(channelStatus.getTx());
    }

    public int getId() {
        return id;
    }

    public InetAddress getLocalIp() {
        return localIp;
    }
}
