package eu.freme.bservices.controllers.nifconverter;

import eu.freme.common.exception.BadRequestException;
import eu.freme.common.rest.NIFParameterSet;
import eu.freme.common.rest.RestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @Autowired
    RestHelper restHelper;

    @RequestMapping(value = "/toolbox/nif-converter", method = RequestMethod.POST)
    public ResponseEntity<String> convert(
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestBody String postBody,
            @RequestParam Map<String, String> allParams) {
        try {
            NIFParameterSet nifParameters = restHelper.normalizeNif(postBody, acceptHeader, contentTypeHeader, allParams, false);
            return restHelper.createSuccessResponse(restHelper.convertInputToRDFModel(nifParameters), nifParameters.getOutformat());
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
