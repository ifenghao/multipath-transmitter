package client.parsers;

/**
 * Created by zfh on 16-4-18.
 */
public enum CheckStatus {
    SEND_REQUEST, WAIT_RESPONSE,
    ACCEPT_BAD_REQUEST("Bad Request"), ACCEPT_SERVICE_UNAVAILABLE("Service Unavailable"),
    WAIT_HEADER("OK"), CHECKING, CHECK_OVER, FINISHED;
    private String response;

    private CheckStatus() {
    }

    private CheckStatus(String response) {
        this.response = response;
    }

    public static CheckStatus getMatchedStatus(String response) {
        for (CheckStatus checkStatus : CheckStatus.values()) {
            if (response.equals(checkStatus.getResponse())) {
                return checkStatus;
            }
        }
        throw new RuntimeException("no this status");
    }

    public String getResponse() {
        return response;
    }
}
