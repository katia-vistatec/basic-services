package eu.freme.bservices.controller.sparqlconverters;


import com.google.common.base.Strings;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import eu.freme.common.conversion.rdf.JenaRDFConversionService;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.FREMEHttpException;
import eu.freme.common.persistence.model.Filter;
import eu.freme.common.rest.NIFParameterSet;
import eu.freme.common.rest.OwnedResourceManagingController;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 12.01.2016.
 */
@RestController
@RequestMapping("/toolbox/filter")
public class SparqlConverterController extends OwnedResourceManagingController<Filter> {

    Logger logger = Logger.getLogger(SparqlConverterController.class);

    @Autowired
    JenaRDFConversionService jenaRDFConversionService;

    @RequestMapping(value = "/documents/{filterName}", method = RequestMethod.POST)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> filter(
            @PathVariable("filterName") String filterName,
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestBody String postBody,
            @RequestParam Map<String, String> allParams
    ){
        try {
            NIFParameterSet nifParameters = this.normalizeNif(postBody,
                    acceptHeader, contentTypeHeader, allParams, false);

            Filter filter = getEntityDAO().findOneByIdentifier(filterName);

            Model model = jenaRDFConversionService.unserializeRDF(
                    nifParameters.getInput(), nifParameters.getInformat());

            String serialization = null;
            switch (filter.getQueryType()){
                case Query.QueryTypeConstruct:
                    Model resultModel = filter.getFilteredModel(model);
                    serialization = jenaRDFConversionService.serializeRDF(resultModel,
                            nifParameters.getOutformat());
                    break;
                case Query.QueryTypeSelect:
                    ResultSet resultSet = filter.getFilteredResultSet(model);
                    // write to a ByteArrayOutputStream
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    switch (nifParameters.getOutformat()){
                        case CSV:
                            ResultSetFormatter.outputAsCSV(outputStream, resultSet);
                            break;
                        case XML:
                            ResultSetFormatter.outputAsXML(outputStream, resultSet);
                            break;
                        case JSON:
                            ResultSetFormatter.outputAsJSON(outputStream, resultSet);
                            break;
                        case TURTLE:
                        case JSON_LD:
                        case RDF_XML:
                        case N3:
                        case N_TRIPLES:
                            ResultSetFormatter.outputAsRDF(outputStream, jenaRDFConversionService.getJenaType(nifParameters.getOutformat()), resultSet);
                            break;
                        default:
                            throw new BadRequestException("Unsupported output format for resultset(SELECT) query: "+nifParameters.getOutformat()+". Only JSON, CSV, XML and RDF types are supported.");
                    }
                    serialization = new String(outputStream.toByteArray());
                    break;
                default:
                    throw new BadRequestException("Unsupported filter query. Only sparql SELECT and CONSTRUCT are allowed types.");
            }

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("Content-Type", nifParameters.getOutformat()
                    .contentType());
            return new ResponseEntity<>(serialization, responseHeaders,
                    HttpStatus.OK);

        }catch (BadRequestException ex){
            logger.error(ex.getMessage());
            throw ex;
        }catch(Exception ex){
            logger.error(ex.getMessage());
            throw new FREMEHttpException(ex.getMessage());
        }
    }

    @Override
    protected Filter createEntity(String id, String body, Map<String, String> parameters, Map<String, String> headers) throws AccessDeniedException {
        // AccessDeniedException can be thrown, if current authentication is the anonymousUser
        return new Filter(id, body);
    }

    @Override
    protected void updateEntity(Filter filter, String body, Map<String, String> parameters, Map<String, String> headers) {
        if(!Strings.isNullOrEmpty(body) && !body.trim().isEmpty() && !body.trim().toLowerCase().equals("null") && !body.trim().toLowerCase().equals("empty")){
            filter.setQuery(body);
            filter.constructQuery();
        }
    }
}
