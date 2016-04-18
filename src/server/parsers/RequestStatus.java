package server.parsers;

/**
 * Created by zfh on 16-4-18.
 */
public enum RequestStatus {
    WAIT_REQUEST, RESPOND_OK("OK"),// 正常请求
    RESPOND_BAD_REQUEST("Bad Request"), RESPOND_NOT_FOUND("Not Found"), RESPOND_SERVICE_UNAVAILABLE("Service Unavailable"),// 异常请求
    REQUEST_END;// 结束请求
    private String response;

    private RequestStatus() {
    }

    private RequestStatus(String response) {
        this.response = "Response:" + response + "\r\n";
    }

    public String getResponse() {
        return response;
    }
}
