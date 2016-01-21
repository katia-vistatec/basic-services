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
import java.util.Map;

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
        int countAsUserWithPermission;
        int countAsUserWithoutPermission;
        int countAsAdmin;
        int countAsAnonymous;
        T entity;
        T entity2;
        T returnedEntity;
        String identifier1;
        String identifier2;

        Map<String, Object> parameters1 = request1.getParameters();
        Map<String, Object> parameters2 = request2.getParameters();
        if(parameters1==null)
            parameters1 = new HashMap<>();
        if(parameters2==null)
            parameters2 = new HashMap<>();
        // force visibility:
        // parameter value overwrites manipulations in RestrictedResourceManagingController.createEntity
        // and RestrictedResourceManagingController.updateEntity create
        parameters1.put(RestrictedResourceManagingController.visibilityParameterName, OwnedResource.Visibility.PUBLIC);
        parameters1.put(RestrictedResourceManagingController.visibilityParameterName, OwnedResource.Visibility.PUBLIC);

        // force description:
        // parameter value overwrites manipulations in RestrictedResourceManagingController.createEntity
        // and RestrictedResourceManagingController.updateEntity create
        parameters2.put(RestrictedResourceManagingController.descriptionParameterName, "description1");
        parameters2.put(RestrictedResourceManagingController.descriptionParameterName, "description2");

        // count already existing entities
        logger.info("count entities as userWithPermission...");
        List<T> allEntities = getAllEntities(ath.getTokenWithPermission());
        countAsUserWithPermission = allEntities.size();
        logger.info("count entities as userWithPermission: "+countAsUserWithPermission);

        logger.info("count entities as userWithoutPermission");
        allEntities = getAllEntities(ath.getTokenWithoutPermission());
        countAsUserWithoutPermission = allEntities.size();
        logger.info("count entities as userWithoutPermission: "+countAsUserWithoutPermission);

        logger.info("count entities as asmin");
        allEntities = getAllEntities(ath.getTokenAdmin());
        countAsAdmin = allEntities.size();
        logger.info("count entities as admin: "+countAsAdmin);

        logger.info("count entities as anonymous user");
        allEntities = getAllEntities(null);
        countAsAnonymous = allEntities.size();
        logger.info("count entities as admin: "+countAsAdmin);

        // check CREATE entities
        logger.info("attempting creating entity as anonymous user: should return UNAUTHORIZED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        entity = createEntity(request1, null, HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        logger.info("create entity with first body, parameters and headers");
        entity = createEntity(request1, ath.getTokenWithPermission(),HttpStatus.OK);
        identifier1 = entity.getIdentifier();
        returnedEntity = getEntity(identifier1, ath.getTokenWithPermission(),HttpStatus.OK);
        assertTrue(containsEntity(returnedEntity, entity));
        assertTrue(containsEntity(entity, returnedEntity));


        // check UPDATE entities
        logger.info("attempting update without permission: should return UNAUTHORIZED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        returnedEntity = updateEntity(identifier1,request1, ath.getTokenWithoutPermission(),HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        logger.info("attempting update as anonymous user: should return UNAUTHORIZED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        returnedEntity = updateEntity(identifier1,request1, ath.getTokenWithoutPermission(),HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        logger.info("update entity content");
        returnedEntity = updateEntity(identifier1, request2, ath.getTokenWithPermission(),HttpStatus.OK);
        logger.info("check updated content");

        logger.info("create comparative entity via request to ensure that all header and parameter values are considered");
        entity2 = createEntity(request2, ath.getTokenWithPermission(), HttpStatus.OK);
        identifier2 = entity2.getIdentifier();

        logger.info("compare updated entity with comparative entity: should be similar");
        assertTrue(containsEntity(returnedEntity, entity2));
        assertTrue(containsEntity(entity2, returnedEntity));

        logger.info("compare first entity with updated entity: should be different");
        assertFalse(containsEntity(returnedEntity, entity));
        assertFalse(containsEntity(entity, returnedEntity));

        logger.info("set first entity to visibility=private as userWithPermission");
        HashMap<String, Object> newParameters = new HashMap<>();
        newParameters.put(RestrictedResourceManagingController.visibilityParameterName, OwnedResource.Visibility.PRIVATE.name());
        returnedEntity = updateEntity(identifier1,
                new SimpleEntityRequest(request1.getBody(), newParameters, request1.getHeaders()),
                ath.getTokenWithPermission(),
                HttpStatus.OK);
        returnedEntity = getEntity(identifier1,ath.getTokenWithPermission(), HttpStatus.OK);
        assertEquals(returnedEntity.getVisibility(), OwnedResource.Visibility.PRIVATE);

        String updatedDescription = "updated_description";
        logger.info("set first entity to description="+updatedDescription + " as admin");
        newParameters.clear();
        newParameters.put(RestrictedResourceManagingController.descriptionParameterName, updatedDescription);
        returnedEntity = updateEntity(identifier1,
                new SimpleEntityRequest(request1.getBody(), newParameters, request1.getHeaders()),
                ath.getTokenWithPermission(),
                HttpStatus.OK);
        returnedEntity = getEntity(identifier1,ath.getTokenAdmin(), HttpStatus.OK);
        assertEquals(returnedEntity.getDescription(),updatedDescription);

        // check changed visibility state with getAll
        logger.info("get all entities as userWithPermission: should return +two entities");
        allEntities = getAllEntities(ath.getTokenWithPermission());
        assertEquals(2+countAsUserWithPermission,allEntities.size());
        logger.info("get all entities as userWithPermission: should return +one entity");
        allEntities = getAllEntities(ath.getTokenWithoutPermission());
        assertEquals(1+countAsUserWithoutPermission,allEntities.size());
        logger.info("get all entities as admin: should return +two entities");
        allEntities = getAllEntities(ath.getTokenAdmin());
        assertEquals(2+countAsAdmin,allEntities.size());
        logger.info("get all entities as anonymous user: should return +one entities");
        allEntities = getAllEntities(null);
        assertEquals(1+countAsAnonymous,allEntities.size());

        // DELETE entities
        logger.info("attempt to delete private entity as userWithoutPermission: should return METHOD_NOT_ALLOWED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        deleteEntity(identifier1,ath.getTokenWithoutPermission(),HttpStatus.METHOD_NOT_ALLOWED);
        logger.info("attempt to delete public entity as userWithoutPermission: should return METHOD_NOT_ALLOWED");
        deleteEntity(identifier2,ath.getTokenWithoutPermission(),HttpStatus.METHOD_NOT_ALLOWED);
        logger.info("attempt to delete private entity as anonymous user: should return METHOD_NOT_ALLOWED");
        deleteEntity(identifier1,null,HttpStatus.METHOD_NOT_ALLOWED);
        logger.info("attempt to delete public entity as anonymous user: should return METHOD_NOT_ALLOWED");
        deleteEntity(identifier2,null,HttpStatus.METHOD_NOT_ALLOWED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        // TODO: add check delete of private/public as userWithPermission and as admin
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
