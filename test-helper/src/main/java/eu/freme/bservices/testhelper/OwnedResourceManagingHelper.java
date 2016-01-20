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

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 20.01.2016.
 */
public class OwnedResourceManagingHelper<T extends OwnedResource> {
    Logger logger = Logger.getLogger(OwnedResourceManagingHelper.class);

    String service;
    Class clazz;

    public static final String creationTimeIdentifier = "creationTime";
    public static final String idIdentifier = "id";

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

        logger.info("create comparative entity via request to ensure that all header and parameter values are considered");
        T entity2 = createEntity(request2, ath.getTokenWithPermission(), HttpStatus.OK);

        logger.info("update entity content");
        returnedEntity = updateEntity(entity.getIdentifier(), request2, ath.getTokenWithPermission(),HttpStatus.OK);
        logger.info("check updated content");

        logger.info("compare updated entity with comparative entity: should be similar");
        assertTrue(containsEntity(returnedEntity, entity2));

        logger.info("compare first entry with updated entry: should be different");
        assertFalse(containsEntity(entity, returnedEntity));

        logger.info("set first entity to visibility=private");
        HashMap<String, Object> newParameters = new HashMap<>();
        newParameters.put(RestrictedResourceManagingController.visibilityParameterName, OwnedResource.Visibility.PRIVATE.name());
        T updatedEntity = updateEntity(entity.getIdentifier(),
                new SimpleEntityRequest(request1.getBody(), newParameters, request1.getHeaders()),
                ath.getTokenWithPermission(),
                HttpStatus.OK);

        logger.info("get all entities as userWithPermission: should return one entity");
        List<T> allEntities = getAllEntities(ath.getTokenWithPermission());
        assertEquals(2,allEntities.size());

        logger.info("get all entities as userWithPermission: should return no entity");
        allEntities = getAllEntities(ath.getTokenWithoutPermission());
        assertEquals(1,allEntities.size());

        logger.info("attempting update without permission: should return NOT_ALLOWED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        returnedEntity = updateEntity(entity.getIdentifier(),request1, ath.getTokenWithoutPermission(),HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        // getall, update,
    }


    public boolean containsEntity(T entity1, T entity2) throws JsonProcessingException {
        JSONObject jObject1 = new JSONObject(toJSON(entity1));
        JSONObject jObject2 = new JSONObject(toJSON(entity2));
        for(Iterator iterator = jObject2.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            Object o1 = jObject1.get(key);
            if(jObject2.isNull(key) || key.equals(creationTimeIdentifier) || key.equals(idIdentifier))
                continue;
            Object o2 = jObject2.get(key);
            if(!o1.toString().equals(o2.toString())) {
                logger.info("entities are not equal at key=\""+key+"\": \""+o1.toString()+"\" != \""+o2.toString()+"\"");
                return false;
            }
        }
        return true;
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
