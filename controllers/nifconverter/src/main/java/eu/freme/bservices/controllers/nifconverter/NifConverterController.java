package eu.freme.bservices.controllers.nifconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import eu.freme.bservices.internationalization.api.InternationalizationAPI;
import eu.freme.bservices.internationalization.okapi.nif.converter.ConversionException;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.FREMEHttpException;
import eu.freme.common.exception.InternalServerErrorException;
import eu.freme.common.rest.NIFParameterFactory;
import eu.freme.common.rest.NIFParameterSet;
import eu.freme.common.rest.RestHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import eu.freme.common.conversion.rdf.RDFConstants;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

/**
 * This implements an anything to NIF converter.
 * You can send plaintext or the input formats
 * that e-Internationalisation supports to the
 * endpoint and it returns NIF.
 *
 * Created by Arne Binder (arne.b.binder@gmail.com) on 09.03.2016.
 */
@RestController
public class NifConverterController {

    Logger logger = Logger.getLogger(NifConverterController.class);

    @Autowired
    RestHelper restHelper;

    @Autowired
    RDFConversionService rdfConversionService;

    @Autowired
    NIFParameterFactory nifParameterFactory;

    @RequestMapping(value = "/toolbox/nif-converter", method = RequestMethod.POST)
    public ResponseEntity<String> convert(
            @RequestHeader(value = "Accept") String acceptHeader,
            @RequestHeader(value = "Content-Type") String contentTypeHeader,
            @RequestBody String postBody,
            @RequestParam Map<String, String> allParams) {

        NIFParameterSet nifParameters =  restHelper.normalizeNif(postBody,
                acceptHeader, contentTypeHeader, allParams, false);
        try {
            Model model;
            if(nifParameters.getInformat().equals(RDFConstants.RDFSerialization.PLAINTEXT)){
                model = ModelFactory.createDefaultModel();
                rdfConversionService.plaintextToRDF(model, nifParameters.getInput(), null, nifParameterFactory.getDefaultPrefix());
            }else {
                model = rdfConversionService.unserializeRDF(postBody, nifParameters.getInformat());
            }
            return restHelper.createSuccessResponse(model, nifParameters.getOutformat());
        }catch (ConversionException e){
            logger.error("Error", e);
            throw new InternalServerErrorException("Conversion from \""
                    + contentTypeHeader + "\" to NIF failed");
        }catch (FREMEHttpException e){
            logger.error("Error", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new BadRequestException(e.getMessage());
        }
    }
}
