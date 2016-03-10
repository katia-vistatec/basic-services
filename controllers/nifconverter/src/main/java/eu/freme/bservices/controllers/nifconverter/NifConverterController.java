package eu.freme.bservices.controllers.nifconverter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.exception.BadRequestException;
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

    @RequestMapping(value = "/toolbox/nif-converter", method = RequestMethod.POST)
    public ResponseEntity<String> convert(
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestBody String postBody,
            @RequestParam Map<String, String> allParams) {

        NIFParameterSet nifParameters = this.normalizeNif(postBody, acceptHeader, contentTypeHeader, allParams, false);

        Model model;
        try {
            if (nifParameters.getInformat().equals(RDFConstants.RDFSerialization.PLAINTEXT)) {
                model = ModelFactory.createDefaultModel();
                Resource strRes = model.createResource(nifParameters.getPrefix()+"#char=0,"+nifParameters.getInput().length());
                strRes.addProperty(RDF.type, model.createResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context"));
                strRes.addProperty(RDF.type, model.createResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#RFC5147String"));
                strRes.addProperty(RDF.type, model.createResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#String"));
                strRes.addProperty(RDF.type, model.createResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Phrase"));
                strRes.addLiteral(model.createProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf"), nifParameters.getInput());
                strRes.addLiteral(model.createProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex"), 0);
                strRes.addLiteral(model.createProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#endIndex"), nifParameters.getInput().length());
                strRes.addProperty(model.createProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#referenceContext"), strRes);
                strRes.addLiteral(model.createProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#isString"), nifParameters.getInput());
            }else {
                model = rdfConversionService.unserializeRDF(nifParameters.getInput(), nifParameters.getInformat());
            }
            HttpHeaders responseHeaders = new HttpHeaders();
            String serialization = rdfConversionService.serializeRDF(model, nifParameters.getOutformat());
            responseHeaders.add("Content-Type", nifParameters.getOutformat().contentType());
            return new ResponseEntity<>(serialization, responseHeaders, HttpStatus.OK);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
