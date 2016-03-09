package eu.freme.bservices.controllers.nifconverter;

import com.hp.hpl.jena.rdf.model.Model;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.ExceptionHandlerService;
import eu.freme.common.rest.BaseRestController;
import eu.freme.common.rest.NIFParameterSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 09.03.2016.
 */
public class NifConverterController extends BaseRestController {

    @Autowired
    RDFConversionService rdfConversionService;

    @Autowired
    ExceptionHandlerService exceptionHandlerService;

    @RequestMapping(value = "/toolbox/nif-converter", method = RequestMethod.POST)
    public ResponseEntity<String> convert(
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestBody String postBody,
            @RequestParam Map<String, String> allParams) {

        NIFParameterSet nifParameters = this.normalizeNif(postBody,
                acceptHeader, contentTypeHeader, allParams, false);

        Model inModel = null;
        try {
            if (nifParameters.getInformat().equals(RDFConstants.RDFSerialization.PLAINTEXT)) {
                // TODO: implement model creation from plaintext
            }else {
                inModel = rdfConversionService.unserializeRDF(nifParameters.getInput(), nifParameters.getInformat());
            }
            HttpHeaders responseHeaders = new HttpHeaders();
            String serialization = rdfConversionService.serializeRDF(inModel,
                    nifParameters.getOutformat());
            responseHeaders.add("Content-Type", nifParameters.getOutformat()
                    .contentType());
            return new ResponseEntity<>(serialization, responseHeaders,
                    HttpStatus.OK);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
