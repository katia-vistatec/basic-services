package eu.freme.bservices.testhelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 20.01.2016.
 */
public class SimpleEntityRequest {
    private final String body;
    private Map<String, Object> parameters;
    private Map<String, String> headers;

    public SimpleEntityRequest(String body, Map<String, Object> parameters, Map<String, String> headers) {
        this.body = body;
        this.parameters = parameters;
        this.headers = headers;
    }

    public SimpleEntityRequest(String body){
        this.body = body;
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

    public SimpleEntityRequest putParameter(String key, Object value){
        if(parameters==null)
            parameters = new HashMap<>();
        parameters.put(key,value);
        return this;
    }

    public SimpleEntityRequest putHeader(String key, String value){
        if(headers==null)
            headers = new HashMap<>();
        headers.put(key, value);
        return this;
    }
}
