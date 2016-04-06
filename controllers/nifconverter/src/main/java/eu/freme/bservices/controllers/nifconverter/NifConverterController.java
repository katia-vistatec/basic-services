package eu.freme.bservices.controllers.nifconverter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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

    private HashSet<String> internalizationContentTypes;
    private HashSet<String> rdfContentTypes;

    @Autowired
    RestHelper restHelper;

    @Autowired
    InternationalizationAPI internationalizationApi;

    @Autowired
    RDFConversionService rdfConversionService;

    @Autowired
    NIFParameterFactory nifParameterFactory;


    public NifConverterController() {
        internalizationContentTypes = new HashSet<>();
        internalizationContentTypes.add(InternationalizationAPI.MIME_TYPE_HTML.toLowerCase());
        internalizationContentTypes.add(InternationalizationAPI.MIME_TYPE_XLIFF_1_2
                .toLowerCase());
        internalizationContentTypes.add(InternationalizationAPI.MIME_TYPE_XML.toLowerCase());
        internalizationContentTypes.add(InternationalizationAPI.MIME_TYPE_ODT.toLowerCase());

        rdfContentTypes = new HashSet<>();
        rdfContentTypes.add(RDFConstants.RDFSerialization.JSON_LD.contentType());
        rdfContentTypes.add(RDFConstants.RDFSerialization.TURTLE.contentType());
        rdfContentTypes.add(RDFConstants.RDFSerialization.N3.contentType());
        rdfContentTypes.add(RDFConstants.RDFSerialization.N_TRIPLES.contentType());
        rdfContentTypes.add(RDFConstants.RDFSerialization.RDF_XML.contentType());
    }

    @RequestMapping(value = "/toolbox/nif-converter", method = RequestMethod.POST)
    public ResponseEntity<String> convert(
            @RequestHeader(value = "Accept") String acceptHeader,
            @RequestHeader(value = "Content-Type") String contentTypeHeader,
            @RequestBody String postBody,
            @RequestParam Map<String, String> allParams) {

        contentTypeHeader = contentTypeHeader.toLowerCase();
        try {
            Model model;

            // formats supported by e-Internalisation
            if (internalizationContentTypes.contains(contentTypeHeader)) {
                Reader nifReader;
                ByteArrayInputStream stream = new ByteArrayInputStream(postBody.getBytes(StandardCharsets.UTF_8));

                nifReader = internationalizationApi.convertToTurtle(stream,
                        contentTypeHeader);

                String nifString = new Scanner(nifReader).useDelimiter("\\Z").next();
                model = rdfConversionService.unserializeRDF(nifString, RDFConstants.RDFSerialization.TURTLE);

            // RDF formats
            } else if (rdfContentTypes.contains(contentTypeHeader)) {
                model = rdfConversionService.unserializeRDF(postBody, RDFConstants.RDFSerialization.fromValue(contentTypeHeader));

            // plaintext
            } else if (contentTypeHeader.equals(RDFConstants.RDFSerialization.PLAINTEXT.contentType())) {
                model = ModelFactory.createDefaultModel();
                rdfConversionService.plaintextToRDF(model, postBody, null, nifParameterFactory.getDefaultPrefix());
            } else {
                throw new BadRequestException("Can not convert from format: " + contentTypeHeader);
            }

            return restHelper.createSuccessResponse(model, RDFConstants.RDFSerialization.fromValue(acceptHeader));
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
