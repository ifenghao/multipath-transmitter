package client.parsers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zfh on 16-4-18.
 */
public enum ReceiveStatus {
    SEND_REQUEST, WAIT_RESPONSE,
    ACCEPT_NOT_FOUND("Not Found"), ACCEPT_BAD_REQUEST("Bad Request"), ACCEPT_SERVICE_UNAVAILABLE("Service Unavailable"),
    WAIT_HEADER("OK"), RECEIVING, RECEIVE_OVER, RESPOND_DONE, FINISHED;
    private String response;

    private ReceiveStatus() {
    }

    private ReceiveStatus(String response) {
        this.response = response;
    }

    public static ReceiveStatus getMatchedStatus(String response) {
        for (ReceiveStatus receiveStatus : ReceiveStatus.values()) {
            if (response.equals(receiveStatus.getResponse())) {
                return receiveStatus;
            }
        }
        throw new RuntimeException("no this status");
    }

    public static List<String> listResponse(){
        List<String> list=new ArrayList<String>();
        list.add(ACCEPT_NOT_FOUND.getResponse());
        list.add(ACCEPT_BAD_REQUEST.getResponse());
        list.add(ACCEPT_SERVICE_UNAVAILABLE.getResponse());
        list.add(WAIT_HEADER.getResponse());
        return list;
    }

    public String getResponse() {
        return response;
    }
}
