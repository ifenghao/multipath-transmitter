package client.parsers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zfh on 16-4-18.
 */
public enum SendStatus {
    SEND_REQUEST, WAIT_RESPONSE,
    ACCEPT_BAD_REQUEST("Bad Request"),ACCEPT_SERVICE_UNAVAILABLE("Service Unavailable"),
    ACCEPT_OK("OK"), SENDING, WAIT_DONE, ACCEPT_DONE("Done"), FINISHED;
    private String response;

    private SendStatus() {
    }

    private SendStatus(String response) {
        this.response = response;
    }

    public static SendStatus getMatchedStatus(String response){
        for (SendStatus sendStatus:SendStatus.values()){
            if (response.equals(sendStatus.getResponse())){
                return sendStatus;
            }
        }
        throw new RuntimeException("no this status");
    }

    public static List<String> listResponse(){
        List<String> list=new ArrayList<String>();
        list.add(ACCEPT_BAD_REQUEST.getResponse());
        list.add(ACCEPT_SERVICE_UNAVAILABLE.getResponse());
        list.add(ACCEPT_OK.getResponse());
        list.add(ACCEPT_DONE.getResponse());
        return list;
    }

    public String getResponse() {
        return response;
    }
}

