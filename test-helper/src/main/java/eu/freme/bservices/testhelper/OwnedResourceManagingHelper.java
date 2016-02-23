package eu.freme.bservices.testhelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.common.rest.OwnedResourceManagingController;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 20.01.2016.
 */
public class OwnedResourceManagingHelper<T extends OwnedResource> {
    Logger logger = Logger.getLogger(OwnedResourceManagingHelper.class);

    String service;
    Class clazz;

    public final String creationTimeIdentifier = "creationTime";
    public final String idIdentifier = "id";
    public final String identifierIdentifier;

    AuthenticatedTestHelper ath;

    public OwnedResourceManagingHelper(String service, Class clazz, AuthenticatedTestHelper ath, String entityIdentifier){
        this.service = service;
        this.clazz = clazz;
        this.ath = ath;
        if(entityIdentifier!=null)
            this.identifierIdentifier = entityIdentifier;
        else
            this.identifierIdentifier = idIdentifier;
    }

    public void checkCRUDOperations(SimpleEntityRequest request, SimpleEntityRequest updateRequest, T expectedCreatedEntity, T expectedUpdatedEntity, String notExistingIdentifier) throws IOException, UnirestException {

        checkCREATE(request, expectedCreatedEntity);
        checkGETonPUBLIC(request, expectedCreatedEntity, notExistingIdentifier);
        checkGETonPRIVATE(request, expectedCreatedEntity, notExistingIdentifier);
        checkGETALLonPUBLIC(request, expectedCreatedEntity);
        checkGETALLonPRIVATE(request, expectedCreatedEntity);
        checkUPDATE(request, updateRequest, expectedCreatedEntity, expectedUpdatedEntity, notExistingIdentifier);
        checkDELETE(request, expectedCreatedEntity, notExistingIdentifier);



        /*int countAsUserWithPermission;
        int countAsUserWithoutPermission;
        int countAsAdmin;
        int countAsAnonymous;
        T entity1;
        //T entity2;
        T returnedEntity;
        String identifier1;
        //String identifier2;

        // force visibility:
        // parameter value overwrites manipulations in OwnedResourceManagingController.createEntity
        // and OwnedResourceManagingController.updateEntity
        request.putParameter(OwnedResourceManagingController.visibilityParameterName, OwnedResource.Visibility.PUBLIC.name());
        updateRequest.putParameter(OwnedResourceManagingController.visibilityParameterName, OwnedResource.Visibility.PUBLIC.name());

        // force description:
        // parameter value overwrites manipulations in OwnedResourceManagingController.createEntity
        // and OwnedResourceManagingController.updateEntity
        request.putParameter(OwnedResourceManagingController.descriptionParameterName, "description1");
        updateRequest.putParameter(OwnedResourceManagingController.descriptionParameterName, "description2");

        // count already existing entities
        logger.info("count entities as userWithPermission");
        List<T> allEntities = getAllEntities(ath.getTokenWithPermission());
        countAsUserWithPermission = allEntities.size();
        logger.info("count entities as userWithPermission: "+countAsUserWithPermission);

        logger.info("count entities as userWithoutPermission");
        allEntities = getAllEntities(ath.getTokenWithoutPermission());
        countAsUserWithoutPermission = allEntities.size();
        logger.info("count entities as userWithoutPermission: "+countAsUserWithoutPermission);

        logger.info("count entities as admin");
        allEntities = getAllEntities(ath.getTokenAdmin());
        countAsAdmin = allEntities.size();
        logger.info("count entities as admin: "+ countAsAdmin);

        logger.info("count entities as anonymous user");
        allEntities = getAllEntities(null);
        countAsAnonymous = allEntities.size();
        logger.info("count entities as admin: "+countAsAdmin);

        // check CREATE entities
        logger.info("attempting creating entity as anonymous user: should return UNAUTHORIZED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        entity1 = createEntity(request, null, HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        logger.info("create entity with first body, parameters and headers");
        entity1 = createEntity(request, ath.getTokenWithPermission(),HttpStatus.OK);
        assertTrue(containsEntity(entity1, expectedCreatedEntity));

        logger.info("refetch entity and check if it equals the expectedCreatedEntity");
        identifier1 = entity1.getIdentifier();
        returnedEntity = getEntity(identifier1, ath.getTokenWithPermission(),HttpStatus.OK);
        assertTrue(containsEntity(returnedEntity, entity1));
        assertTrue(containsEntity(entity1, returnedEntity));


        // check UPDATE entities
        logger.info("attempting update without permission: should return UNAUTHORIZED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        returnedEntity = updateEntity(identifier1, request, ath.getTokenWithoutPermission(),HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        logger.info("attempting update as anonymous user: should return UNAUTHORIZED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        returnedEntity = updateEntity(identifier1, request, null, HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        logger.info("update entity content");
        returnedEntity = updateEntity(identifier1, updateRequest, ath.getTokenWithPermission(),HttpStatus.OK);
        logger.info("check updated content");

        //logger.info("create second entity with request");
        //entity2 = createEntity(request, ath.getTokenWithPermission(), HttpStatus.OK);
        //identifier2 = entity2.getIdentifier();

        logger.info("compare updated entity with expectedUpdatedEntity: should be similar");
        assertTrue(containsEntity(returnedEntity, expectedUpdatedEntity));
        //assertTrue(containsEntity(entity2, returnedEntity));

        logger.info("compare first entity with updated entity: should be different");
        assertFalse(containsEntity(returnedEntity, entity1));
        assertFalse(containsEntity(entity1, returnedEntity));

        logger.info("set first entity to visibility=private as userWithPermission");
        HashMap<String, Object> newParameters = new HashMap<>();
        newParameters.put(OwnedResourceManagingController.visibilityParameterName, OwnedResource.Visibility.PRIVATE.name());
        returnedEntity = updateEntity(identifier1,
                new SimpleEntityRequest(request.getBody(), newParameters, request.getHeaders()),
                ath.getTokenWithPermission(),
                HttpStatus.OK);
        returnedEntity = getEntity(identifier1,ath.getTokenWithPermission(), HttpStatus.OK);
        assertEquals(returnedEntity.getVisibility(), OwnedResource.Visibility.PRIVATE);

        String updatedDescription = "updated_description";
        logger.info("set first entity to description="+updatedDescription + " as admin");
        newParameters.clear();
        newParameters.put(OwnedResourceManagingController.descriptionParameterName, updatedDescription);
        // ensure to not change back the visibility. This can happen, if the body contains a visibility value
        newParameters.put(OwnedResourceManagingController.visibilityParameterName, OwnedResource.Visibility.PRIVATE.name());
        returnedEntity = updateEntity(identifier1,
                new SimpleEntityRequest(request.getBody(), newParameters, request.getHeaders()),
                ath.getTokenWithPermission(),
                HttpStatus.OK);
        returnedEntity = getEntity(identifier1,ath.getTokenAdmin(), HttpStatus.OK);
        assertEquals(returnedEntity.getDescription(),updatedDescription);

        // check GET
        logger.info("read private entity1 as userWithPermission");
        returnedEntity = getEntity(identifier1,ath.getTokenWithPermission(),HttpStatus.OK);
        logger.info("ensure that entity1 is PRIVATE");
        assertEquals(returnedEntity.getVisibility(), OwnedResource.Visibility.PRIVATE);
        logger.info("read public entity2 as userWithPermission");
        returnedEntity = getEntity(identifier2,ath.getTokenWithPermission(),HttpStatus.OK);
        logger.info("ensure that entity2 is PUBLIC");
        assertEquals(returnedEntity.getVisibility(), OwnedResource.Visibility.PUBLIC);
        logger.info("read private entity1 as admin");
        returnedEntity = getEntity(identifier1,ath.getTokenAdmin(),HttpStatus.OK);
        logger.info("read public entity2 as admin");
        returnedEntity = getEntity(identifier2,ath.getTokenAdmin(),HttpStatus.OK);
        logger.info("read private entity1 as userWithoutPermission");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        returnedEntity = getEntity(identifier1,ath.getTokenWithoutPermission(),HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);
        logger.info("read public entity2 as userWithoutPermission");
        returnedEntity = getEntity(identifier2,ath.getTokenWithoutPermission(),HttpStatus.OK);
        logger.info("read private entity1 as anonymous user");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        returnedEntity = getEntity(identifier1,null,HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);
        logger.info("read public entity2 as anonymous user");
        returnedEntity = getEntity(identifier2,null,HttpStatus.OK);

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

        // check DELETE entities
        logger.info("attempt to delete private entity as userWithoutPermission: should return UNAUTHORIZED");
        deleteEntity(identifier1,ath.getTokenWithoutPermission(),HttpStatus.UNAUTHORIZED);
        logger.info("attempt to delete public entity as userWithoutPermission: should return UNAUTHORIZED");
        deleteEntity(identifier2,ath.getTokenWithoutPermission(),HttpStatus.UNAUTHORIZED);
        logger.info("attempt to delete private entity as anonymous user: should return UNAUTHORIZED");
        deleteEntity(identifier1,null,HttpStatus.UNAUTHORIZED);
        logger.info("attempt to delete public entity as anonymous user: should return UNAUTHORIZED");
        deleteEntity(identifier2,null,HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        logger.info("delete private entity as admin");
        deleteEntity(identifier1,ath.getTokenAdmin(),HttpStatus.OK);
        logger.info("delete public entity as admin");
        deleteEntity(identifier2,ath.getTokenAdmin(),HttpStatus.OK);

        logger.info("recreate one private and one public entity");
        entity1 = createEntity(request,ath.getTokenWithPermission(),HttpStatus.OK);
        assertEquals(OwnedResource.Visibility.PUBLIC, entity1.getVisibility());
        updateRequest.putParameter(OwnedResourceManagingController.visibilityParameterName,OwnedResource.Visibility.PRIVATE.name());
        entity2 = createEntity(updateRequest,ath.getTokenWithPermission(),HttpStatus.OK);
        assertEquals(OwnedResource.Visibility.PRIVATE, entity2.getVisibility());

        logger.info("delete public entity as userWithPermission");
        deleteEntity(entity1.getIdentifier(),ath.getTokenWithPermission(),HttpStatus.OK);
        logger.info("delete private entity as userWithPermission");
        deleteEntity(entity2.getIdentifier(),ath.getTokenWithPermission(),HttpStatus.OK);

        logger.info("ensure that all created entities are deleted");
        assertEquals(countAsAdmin,getAllEntities(ath.getTokenAdmin()).size());

        logger.info("try to fetch not existing entity: should return NOT_FOUND");
        returnedEntity = getEntity(notExistingIdentifier, ath.getTokenWithPermission(), HttpStatus.NOT_FOUND);

 //       logger.info("delete nonexisting entity as admin: should return NOT FOUND");
 //       deleteEntity(identifier2,ath.getTokenAdmin(),HttpStatus.NOT_FOUND);

 */
    }

    private void checkCREATE(SimpleEntityRequest request, T expectedCreatedEntity) throws IOException, UnirestException {
        T entity;
        int countAsAdmin;
        int currentCount;

        // count existing entities
        countAsAdmin = getAllEntities(ath.getTokenAdmin()).size();

        //// check CREATE entities
        // as anonymous user
        logger.info("attempting creating entity as anonymous user: should return UNAUTHORIZED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        entity = createEntity(request, null, HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        // as userWithPermission
        logger.info("create entity with request body, parameters and headers");
        entity = createEntity(request, ath.getTokenWithPermission(),HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        //// delete created
        logger.info("delete created entity");
        deleteEntity(entity.getIdentifier(), ath.getTokenWithPermission(), HttpStatus.OK);

        // check entity count
        currentCount = getAllEntities(ath.getTokenAdmin()).size();
        assertEquals(countAsAdmin, currentCount);
    }

    private void checkGETonPUBLIC(SimpleEntityRequest request, T expectedCreatedEntity, String notExistingIdentifier) throws IOException, UnirestException {
        T entity;
        String identifier;
        int countAsAdmin;
        int currentCount;
        SimpleEntityRequest localRequest = new SimpleEntityRequest(request);
        OwnedResource.Visibility visibilityBefore = expectedCreatedEntity.getVisibility();

        // count existing entites
        countAsAdmin = getAllEntities(ath.getTokenAdmin()).size();

        // assert visibility=PUBLIC
        expectedCreatedEntity.setVisibility(OwnedResource.Visibility.PUBLIC);
        localRequest.putParameter(OwnedResourceManagingController.visibilityParameterName, OwnedResource.Visibility.PUBLIC.name());

        //// create entity
        // as userWithPermission: use this in further requests
        logger.info("create entity with request body, parameters and headers");
        entity = createEntity(localRequest, ath.getTokenWithPermission(),HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));
        identifier = entity.getIdentifier();

        //// check GET entity
        // as anonymous user
        logger.info("fetch entity as anonymous user");
        entity = getEntity(identifier, null, HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        // as userWithoutPermission
        logger.info("fetch entity as userWithoutPermission");
        entity = getEntity(identifier, ath.getTokenWithoutPermission(), HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        // as userWithPermission
        logger.info("fetch entity as userWithPermission");
        entity = getEntity(identifier, ath.getTokenWithPermission(), HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        // as admin
        logger.info("fetch entity as admin");
        entity = getEntity(identifier, ath.getTokenAdmin(), HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        //// delete created
        logger.info("delete created entity");
        deleteEntity(identifier, ath.getTokenWithPermission(), HttpStatus.OK);

        // try to fetch not existing entity
        logger.info("fetch not existing entity: should return NOT_FOUND");
        LoggingHelper.loggerIgnore(LoggingHelper.ownedResourceNotFoundException);
        getEntity(notExistingIdentifier, ath.getTokenAdmin(), HttpStatus.NOT_FOUND);
        LoggingHelper.loggerUnignore(LoggingHelper.ownedResourceNotFoundException);

        // check entity count
        currentCount = getAllEntities(ath.getTokenAdmin()).size();
        assertEquals(countAsAdmin, currentCount);

        expectedCreatedEntity.setVisibility(visibilityBefore);
    }

    private void checkGETonPRIVATE(SimpleEntityRequest request, T expectedCreatedEntity, String notExistingIdentifier) throws IOException, UnirestException {
        T entity;
        String identifier;
        int countAsAdmin;
        int currentCount;
        SimpleEntityRequest localRequest = new SimpleEntityRequest(request);
        OwnedResource.Visibility visibilityBefore = expectedCreatedEntity.getVisibility();

        // count existing entites
        countAsAdmin = getAllEntities(ath.getTokenAdmin()).size();

        // assert visibility=PRIVATE
        expectedCreatedEntity.setVisibility(OwnedResource.Visibility.PRIVATE);
        localRequest.putParameter(OwnedResourceManagingController.visibilityParameterName, OwnedResource.Visibility.PRIVATE.name());

        //// create entity
        // as userWithPermission: use this in further requests
        logger.info("create entity with request body, parameters and headers");
        entity = createEntity(localRequest, ath.getTokenWithPermission(),HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));
        identifier = entity.getIdentifier();

        //// check GET entity
        // as anonymous user
        logger.info("fetch entity as anonymous user: should return UNAUTHORIZED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        entity = getEntity(identifier, null, HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        // as userWithoutPermission
        logger.info("fetch entity as userWithoutPermission: should return UNAUTHORIZED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        entity = getEntity(identifier, ath.getTokenWithoutPermission(), HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        // as userWithPermission
        logger.info("fetch entity as userWithPermission");
        entity = getEntity(identifier, ath.getTokenWithPermission(), HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        // as admin
        logger.info("fetch entity as admin");
        entity = getEntity(identifier, ath.getTokenAdmin(), HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        //// delete created
        logger.info("delete created entity");
        deleteEntity(identifier, ath.getTokenWithPermission(), HttpStatus.OK);

        // try to fetch not existing entity
        logger.info("fetch not existing entity: should return NOT_FOUND");
        LoggingHelper.loggerIgnore(LoggingHelper.ownedResourceNotFoundException);
        getEntity(notExistingIdentifier, ath.getTokenAdmin(), HttpStatus.NOT_FOUND);
        LoggingHelper.loggerUnignore(LoggingHelper.ownedResourceNotFoundException);

        // check entity count
        currentCount = getAllEntities(ath.getTokenAdmin()).size();
        assertEquals(countAsAdmin, currentCount);

        expectedCreatedEntity.setVisibility(visibilityBefore);
    }


    private void checkGETALLonPUBLIC(SimpleEntityRequest request, T expectedCreatedEntity) throws IOException, UnirestException {
        List<T> entities;
        T entity;
        String identifier;
        int countAsAdmin;
        int currentCount;
        SimpleEntityRequest localRequest = new SimpleEntityRequest(request);
        OwnedResource.Visibility visibilityBefore = expectedCreatedEntity.getVisibility();

        // count existing entities
        countAsAdmin = getAllEntities(ath.getTokenAdmin()).size();

        // assert visibility=PUBLIC
        expectedCreatedEntity.setVisibility(OwnedResource.Visibility.PUBLIC);
        localRequest.putParameter(OwnedResourceManagingController.visibilityParameterName, OwnedResource.Visibility.PUBLIC.name());

        //// create entity
        // as userWithPermission: use this in further requests
        logger.info("create PUBLIC entity with request body, parameters and headers");
        entity = createEntity(localRequest, ath.getTokenWithPermission(),HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));
        identifier = entity.getIdentifier();

        //// check GET ALL entities
        // as anonymous user
        logger.info("fetch object as anonymous user");
        entities = getAllEntities(null);
        entity = getFirstEntityWithIdentifier(entities, identifier);
        assertNotNull(entity);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        // as userWithoutPermission
        logger.info("fetch all entities as userWithoutPermission");
        entities = getAllEntities(ath.getTokenWithoutPermission());
        entity = getFirstEntityWithIdentifier(entities, identifier);
        assertNotNull(entity);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        // as userWithPermission
        logger.info("fetch all entities as userWithPermission");
        entities = getAllEntities(ath.getTokenWithPermission());
        entity = getFirstEntityWithIdentifier(entities, identifier);
        assertNotNull(entity);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        // as admin
        logger.info("fetch all entities as admin");
        entities = getAllEntities(ath.getTokenAdmin());
        entity = getFirstEntityWithIdentifier(entities, identifier);
        assertNotNull(entity);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        //// delete created
        logger.info("delete created entity");
        deleteEntity(identifier, ath.getTokenWithPermission(), HttpStatus.OK);

        // check entity count
        currentCount = getAllEntities(ath.getTokenAdmin()).size();
        assertEquals(countAsAdmin, currentCount);

        expectedCreatedEntity.setVisibility(visibilityBefore);
    }

    private void checkGETALLonPRIVATE(SimpleEntityRequest request, T expectedCreatedEntity) throws IOException, UnirestException {
        List<T> entities;
        T entity;
        String identifier;
        int countAsAdmin;
        int currentCount;
        SimpleEntityRequest localRequest = new SimpleEntityRequest(request);
        OwnedResource.Visibility visibilityBefore = expectedCreatedEntity.getVisibility();

        // count existing entites
        countAsAdmin = getAllEntities(ath.getTokenAdmin()).size();

        // assert visibility=PUBLIC
        expectedCreatedEntity.setVisibility(OwnedResource.Visibility.PRIVATE);
        localRequest.putParameter(OwnedResourceManagingController.visibilityParameterName, OwnedResource.Visibility.PRIVATE.name());

        //// create entity
        // as userWithPermission: use this in further requests
        logger.info("create PRIVATE entity with request body, parameters and headers");
        entity = createEntity(localRequest, ath.getTokenWithPermission(),HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));
        identifier = entity.getIdentifier();

        //// check GET ALL entities
        // as anonymous user
        logger.info("fetch object as anonymous user");
        entities = getAllEntities(null);
        entity = getFirstEntityWithIdentifier(entities, identifier);
        assertNull(entity);

        // as userWithoutPermission
        logger.info("fetch all entities as userWithoutPermission");
        entities = getAllEntities(ath.getTokenWithoutPermission());
        entity = getFirstEntityWithIdentifier(entities, identifier);
        assertNull(entity);

        // as userWithPermission
        logger.info("fetch all entities as userWithPermission");
        entities = getAllEntities(ath.getTokenWithPermission());
        entity = getFirstEntityWithIdentifier(entities, identifier);
        assertNotNull(entity);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        // as admin
        logger.info("fetch all entities as admin");
        entities = getAllEntities(ath.getTokenAdmin());
        entity = getFirstEntityWithIdentifier(entities, identifier);
        assertNotNull(entity);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        //// delete created
        logger.info("delete created entity");
        deleteEntity(identifier, ath.getTokenWithPermission(), HttpStatus.OK);

        // check entity count
        currentCount = getAllEntities(ath.getTokenAdmin()).size();
        assertEquals(countAsAdmin, currentCount);

        expectedCreatedEntity.setVisibility(visibilityBefore);
    }

    private void checkUPDATE(SimpleEntityRequest request, SimpleEntityRequest updateRequest, T expectedCreatedEntity, T expectedUpdatedEntity, String notExistingIdentifier) throws IOException, UnirestException {
        T entity;
        T updatedEntity;
        String identifier;
        int countAsAdmin;
        int currentCount;

        // count existing entites
        countAsAdmin = getAllEntities(ath.getTokenAdmin()).size();

        //// create entity
        // as userWithPermission: use this in further requests
        logger.info("create entity with request body, parameters and headers");
        entity = createEntity(request, ath.getTokenWithPermission(),HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));
        identifier = entity.getIdentifier();

        //// check UPDATE entity
        // as anonymous user
        logger.info("update entity as anonymous user: should return UNAUTHORIZED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        updatedEntity = updateEntity(identifier, updateRequest, null, HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        // as userWithoutPermission
        logger.info("update entity as userWithoutPermission: should return UNAUTHORIZED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        updatedEntity = updateEntity(identifier, updateRequest, ath.getTokenWithoutPermission(), HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        // as userWithPermission
        logger.info("update entity as userWithPermission");
        updatedEntity = updateEntity(identifier, updateRequest, ath.getTokenWithPermission(), HttpStatus.OK);
        assertTrue(containsEntity(updatedEntity, expectedUpdatedEntity));
        // remove updated entity
        logger.info("delete updated entity");
        deleteEntity(identifier, ath.getTokenWithPermission(), HttpStatus.OK);

        //// create entity
        // as userWithPermission: use this in further requests
        logger.info("create entity with request body, parameters and headers");
        entity = createEntity(request, ath.getTokenWithPermission(),HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));
        identifier = entity.getIdentifier();
        // as admin
        logger.info("update entity as admin");
        updatedEntity = updateEntity(identifier, updateRequest, ath.getTokenAdmin(), HttpStatus.OK);
        assertTrue(containsEntity(updatedEntity, expectedUpdatedEntity));
        // remove updated entity
        logger.info("delete updated entity");
        deleteEntity(identifier, ath.getTokenAdmin(), HttpStatus.OK);

        // try to update not existing entity
        logger.info("update not existing entity: should return NOT_FOUND");
        LoggingHelper.loggerIgnore(LoggingHelper.ownedResourceNotFoundException);
        updateEntity(notExistingIdentifier, updateRequest, ath.getTokenAdmin(), HttpStatus.NOT_FOUND);
        LoggingHelper.loggerUnignore(LoggingHelper.ownedResourceNotFoundException);

        // check entity count
        currentCount = getAllEntities(ath.getTokenAdmin()).size();
        assertEquals(countAsAdmin, currentCount);
    }

    private void checkDELETE(SimpleEntityRequest request, T expectedCreatedEntity, String notExistingIdentifier) throws IOException, UnirestException {
        T entity;
        int countAsAdmin;
        int currentCount;

        // count existing entites
        countAsAdmin = getAllEntities(ath.getTokenAdmin()).size();

        // create entity
        // as userWithPermission
        logger.info("create entity with request body, parameters and headers");
        entity = createEntity(request, ath.getTokenWithPermission(),HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        // as anonymous user
        logger.info("delete entity as anonymous user: should return UNAUTHORIZED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        deleteEntity(entity.getIdentifier(), null, HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        // as userWithoutPermission user
        logger.info("delete entity as anonymous user: should return UNAUTHORIZED");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        deleteEntity(entity.getIdentifier(), ath.getTokenWithoutPermission(), HttpStatus.UNAUTHORIZED);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        // as userWithPermission
        logger.info("delete entity as userWithPermission");
        deleteEntity(entity.getIdentifier(), ath.getTokenWithPermission(), HttpStatus.OK);

        // create entity
        // as userWithPermission
        logger.info("create entity with request body, parameters and headers");
        entity = createEntity(request, ath.getTokenWithPermission(),HttpStatus.OK);
        assertTrue(containsEntity(entity, expectedCreatedEntity));

        // as admin
        logger.info("delete entity as admin");
        deleteEntity(entity.getIdentifier(), ath.getTokenAdmin(), HttpStatus.OK);

        // try to update not existing entity
        logger.info("delete not existing entity: should return NOT_FOUND");
        LoggingHelper.loggerIgnore(LoggingHelper.ownedResourceNotFoundException);
        deleteEntity(notExistingIdentifier, ath.getTokenAdmin(), HttpStatus.NOT_FOUND);
        LoggingHelper.loggerUnignore(LoggingHelper.ownedResourceNotFoundException);

        // check entity count
        currentCount = getAllEntities(ath.getTokenAdmin()).size();
        assertEquals(countAsAdmin, currentCount);
    }


    private T getFirstEntityWithIdentifier(List<T> entities, String identifier){
        for(T entity: entities){
            if(entity.getIdentifier().equals(identifier))
                return entity;
        }
        return null;
    }

    public boolean containsEntity(T entity1, T entity2) throws JsonProcessingException {
        JSONObject jObject1 = new JSONObject(entity1.toJson());
        JSONObject jObject2 = new JSONObject(entity2.toJson());
        for(Iterator iterator = jObject2.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            Object o1 = jObject1.get(key);
            if(jObject2.isNull(key) || key.equals(creationTimeIdentifier) || key.equals(idIdentifier) || key.equals(identifierIdentifier))
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
    @SuppressWarnings("unchecked")
    public T createEntity(SimpleEntityRequest request, String token, HttpStatus expectedStatus) throws IOException, UnirestException {
        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + OwnedResourceManagingController.relativeManagePath;
        response = ath.addAuthentication(Unirest.post(url), token)
                .headers(request.getHeaders())
                .header("content-type", RDFConstants.RDFSerialization.JSON.contentType())
                .queryString(request.getParameters())
                .body(request.getBody())
                .asString();
        assertEquals(expectedStatus.value(), response.getStatus());
        if(expectedStatus.equals(HttpStatus.OK)){
            String contentType = response.getHeaders().getFirst("content-type").split(";")[0];
            assertEquals(RDFConstants.RDFSerialization.JSON.contentType(), contentType);
        }
        try {
            return (T) T.fromJson(response.getBody(), clazz);//fromJSON(response.getBody());
        }catch (IOException e){
            logger.info("json response was not valid concerning the entity class");
            return null;
        }
    }


    //UPDATE
    @SuppressWarnings("unchecked")
    public T updateEntity(String identifier, SimpleEntityRequest request, String token, HttpStatus expectedStatus) throws IOException, UnirestException {
        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + OwnedResourceManagingController.relativeManagePath;
        response = ath.addAuthentication(Unirest.put(url+"/"+identifier), token)
                .headers(request.getHeaders())
                .header("content-type", RDFConstants.RDFSerialization.JSON.contentType())
                .queryString(request.getParameters())
                .body(request.getBody())
                .asString();

        assertEquals(expectedStatus.value(), response.getStatus());
        if(expectedStatus.equals(HttpStatus.OK)){
            String contentType = response.getHeaders().getFirst("content-type").split(";")[0];
            assertEquals(RDFConstants.RDFSerialization.JSON.contentType(), contentType);
        }
        try {
            return (T) T.fromJson(response.getBody(), clazz);//fromJSON(response.getBody());
        }catch (IOException e){
            logger.info("json response was not valid concerning the entity class");
            return null;
        }
    }

    //DELETE
    public void deleteEntity(String identifier, String token, HttpStatus expectedStatus ) throws UnirestException {
        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + OwnedResourceManagingController.relativeManagePath + "/"+identifier;
        response = ath.addAuthentication(Unirest.delete(url), token)
                .asString();
        assertEquals(expectedStatus.value(), response.getStatus());
    }


    // GET
    @SuppressWarnings("unchecked")
    public T getEntity(String identifier, String token, HttpStatus expectedStatus) throws UnirestException, IOException {
        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + OwnedResourceManagingController.relativeManagePath;
        response = ath.addAuthentication(Unirest.get(url+"/"+identifier), token)
                .asString();
        assertEquals(expectedStatus.value(), response.getStatus());
        if(expectedStatus.equals(HttpStatus.OK)){
            String contentType = response.getHeaders().getFirst("content-type").split(";")[0];
            assertEquals(RDFConstants.RDFSerialization.JSON.contentType(), contentType);
        }

        try {
            return (T) T.fromJson(response.getBody(), clazz);//fromJSON(response.getBody());
        }catch (IOException e){
            logger.info("json response was not valid concerning the entity class");
            return null;
        }
    }

    //GETALL
    public List<T> getAllEntities(String token) throws UnirestException, IOException {
        HttpResponse<String> response;
        String url = ath.getAPIBaseUrl() + service + OwnedResourceManagingController.relativeManagePath;
        response = ath.addAuthentication(Unirest.get(url), token)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        String contentType = response.getHeaders().getFirst("content-type").split(";")[0];
        assertEquals(RDFConstants.RDFSerialization.JSON.contentType(), contentType);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.getBody(),
                TypeFactory.defaultInstance().constructCollectionType(List.class, clazz));
    }

}
