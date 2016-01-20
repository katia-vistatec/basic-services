package eu.freme.bservices.testhelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.common.persistence.model.Pipeline;
import eu.freme.common.persistence.model.SerializedRequest;
import eu.freme.common.rest.RestrictedResourceManagingController;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 20.01.2016.
 */
public class OwnedResourceManagingHelper<T extends OwnedResource> {
    Logger logger = Logger.getLogger(OwnedResourceManagingHelper.class);

    String service;
    Class clazz;

    AuthenticatedTestHelper ath;

    public OwnedResourceManagingHelper(String service, Class clazz, AuthenticatedTestHelper ath){
        this.service = service;
        this.clazz = clazz;
        this.ath = ath;
    }

    public void checkAllOperations(String body1, String body2, Map<String, Object> parameters1, Map<String, Object> parameters2, Map<String, String> headers1, Map<String, String> headers2){
        //TODO: impolement!
    }


    public T createEntity(String body, Map<String, Object> parameters, Map<String, String> headers) throws IOException, UnirestException {
        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + RestrictedResourceManagingController.relativeManagePath;
        response = ath.addAuthentication(Unirest.post(url))
                .headers(headers)
                .queryString(parameters)
                .body(body)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        T responseEntity = fromJSON(response.getBody());
        return responseEntity;
    }

    public T updateEntity(String identifier, String body, Map<String, Object> parameters, Map<String, String> headers) throws IOException, UnirestException {

        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + RestrictedResourceManagingController.relativeManagePath;
        response = ath.addAuthentication(Unirest.put(url+"/"+identifier))
                .headers(headers)
                .queryString(parameters)
                .body(body)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        T responseEntity = fromJSON(response.getBody());

        return responseEntity;
    }

    public void deleteEntity(String identifier) throws UnirestException {
        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + RestrictedResourceManagingController.relativeManagePath;
        response = ath.addAuthentication(Unirest.delete(url))
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    public T getEntity(String identifier) throws UnirestException, IOException {
        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + RestrictedResourceManagingController.relativeManagePath;
        response = ath.addAuthentication(Unirest.get(url+"/"+identifier))
                .queryString(RestrictedResourceManagingController.identifierParameterName, identifier)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        return fromJSON(response.getBody());
    }

    public List<T> getAllEntitis() throws UnirestException, IOException {
        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + RestrictedResourceManagingController.relativeManagePath;
        response = ath.addAuthentication(Unirest.get(url))
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.getBody(),
                TypeFactory.defaultInstance().constructCollectionType(List.class, clazz));
    }


    public String toJSON(T entity) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(entity);
    }

    @SuppressWarnings("unchecked")
    public T fromJSON(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return (T) mapper.readValue(json, clazz);
    }


}
