package gui;

import java.net.SocketAddress;

/**
 * Created by zfh on 16-5-31.
 */
public class ChannelStatus {
    private int rx;
    private int tx;

    public ChannelStatus() {
        this.rx=0;
        this.tx=0;
    }

    public void addRX(int increment){
        this.rx+=increment;
    }

    public void addTX(int increment){
        this.tx+=increment;
    }

    public int getRx() {
        return rx;
    }

    public int getTx() {
        return tx;
    }

    public void reset(){
        rx=0;
        tx=0;
    }
}
