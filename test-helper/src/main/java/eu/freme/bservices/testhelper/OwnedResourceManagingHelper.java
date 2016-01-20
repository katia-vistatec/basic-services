package eu.freme.bservices.testhelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.common.rest.RestrictedResourceManagingController;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

    public void checkAllOperations(SimpleEntityRequest request1, SimpleEntityRequest request2) throws IOException, UnirestException {
        logger.info("create entity with first body, parameters and headers");
        T entity = createEntity(request1, ath.getTokenWithPermission(),HttpStatus.OK);
        T returnedEntity = getEntity(entity.getIdentifier(), ath.getTokenWithPermission(),HttpStatus.OK);

        logger.info("set first entity to visibility=private");
        HashMap<String, Object> newParameters = new HashMap<>();
        newParameters.put(RestrictedResourceManagingController.visibilityParameterName, OwnedResource.Visibility.PRIVATE.name());
        T updatedEntity = updateEntity(entity.getIdentifier(),
                new SimpleEntityRequest(request1.getBody(), newParameters, request1.getHeaders()),
                ath.getTokenWithPermission(),
                HttpStatus.OK);

        logger.info("get all entities as userWithPermission: should return one entity");
        List<T> allEntities = getAllEntities(ath.getTokenWithPermission());
        assertEquals(1,allEntities.size());

        logger.info("get all entities as userWithPermission: should return no entity");
        allEntities = getAllEntities(ath.getTokenWithoutPermission());
        assertEquals(0,allEntities.size());

        logger.info("attempting update without permission: should return NOT_ALLOWED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        returnedEntity = updateEntity(entity.getIdentifier(),request1,ath.getTokenWithoutPermission(),HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        logger.info("testing update entity content: should be different than original content");
        returnedEntity = updateEntity(entity.getIdentifier(),request2,ath.getTokenWithPermission(),HttpStatus.OK);
        logger.info("check updated content");
        sameEntities(entity,entity);
        sameEntities(entity, returnedEntity);

        // getall, update,
    }

    public boolean sameEntities(T entity1, T entity2) throws JsonProcessingException {
        HashMap<String,Object> json1 = jsonHashMapFromString(toJSON(entity1));
        HashMap<String,Object> json2 = jsonHashMapFromString(toJSON(entity2));
        for (String key: json1.keySet()) {
            try {
                clazz.getField(key);
            } catch (NoSuchFieldException e) {
                if(json1.get(key)!=json2.get(key)) {
                    return false;
                }
            }
        }
        return true;
    }

    public HashMap<String,Object > jsonHashMapFromString(String json) {
        HashMap<String, Object> map = new HashMap<String, Object >();
        JSONObject jObject = new JSONObject(json);
        Iterator<?> keys = jObject.keys();
        while( keys.hasNext() ){

            String key = (String) keys.next();
            try {;
                String value = jObject.getString(key);
                map.put(key, value);
            } catch (Exception e) {
                logger.error("function currently not working, key "+key+" not added to hashmap");
            }
        }
        return map;
    }


    // CREATE
    public T createEntity(SimpleEntityRequest request, String token, HttpStatus expectedStatus) throws IOException, UnirestException {
        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + RestrictedResourceManagingController.relativeManagePath;
        response = ath.addAuthentication(Unirest.post(url), token)
                .headers(request.getHeaders())
                .queryString(request.getParameters())
                .body(request.getBody())
                .asString();
        assertEquals(expectedStatus.value(), response.getStatus());
        try {
            return fromJSON(response.getBody());
        }catch (IOException e){
            logger.info("json response was not valid concerning the entity class");
            return null;
        }
    }


    //UPDATE
    public T updateEntity(String identifier, SimpleEntityRequest request, String token, HttpStatus expectedStatus) throws IOException, UnirestException {

        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + RestrictedResourceManagingController.relativeManagePath;
        response = ath.addAuthentication(Unirest.put(url+"/"+identifier), token)
                .headers(request.getHeaders())
                .queryString(request.getParameters())
                .body(request.getBody())
                .asString();

        assertEquals(expectedStatus.value(), response.getStatus());
        try {
            return fromJSON(response.getBody());
        }catch (IOException e){
            logger.info("json response was not valid concerning the entity class");
            return null;
        }

    }

    //DELETE
    public void deleteEntity(String identifier, String token, HttpStatus expectedStatus ) throws UnirestException {
        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + RestrictedResourceManagingController.relativeManagePath;
        response = ath.addAuthentication(Unirest.delete(url), token)
                .asString();
        assertEquals(expectedStatus.value(), response.getStatus());
    }


    // GET
    public T getEntity(String identifier, String token, HttpStatus expectedStatus) throws UnirestException, IOException {
        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + RestrictedResourceManagingController.relativeManagePath;
        response = ath.addAuthentication(Unirest.get(url+"/"+identifier), token)
                .queryString(RestrictedResourceManagingController.identifierParameterName, identifier)
                .asString();
        assertEquals(expectedStatus.value(), response.getStatus());

        try {
            return fromJSON(response.getBody());
        }catch (IOException e){
            logger.info("json response was not valid concerning the entity class");
            return null;
        }
    }

    //GETALL
    public List<T> getAllEntities(String token) throws UnirestException, IOException {
        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + RestrictedResourceManagingController.relativeManagePath;
        response = ath.addAuthentication(Unirest.get(url), token)
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
