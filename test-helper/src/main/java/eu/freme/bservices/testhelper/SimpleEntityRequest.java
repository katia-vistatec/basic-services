package eu.freme.bservices.testhelper;

import java.util.Map;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 20.01.2016.
 */
public class SimpleEntityRequest {
    private final String body;
    private final Map<String, Object> parameters;
    private final Map<String, String> headers;

    public SimpleEntityRequest(String body, Map<String, Object> parameters, Map<String, String> headers) {
        this.body = body;
        this.parameters = parameters;
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
