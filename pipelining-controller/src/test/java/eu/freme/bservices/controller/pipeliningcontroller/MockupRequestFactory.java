package eu.freme.bservices.controller.pipeliningcontroller;

import eu.freme.bservices.controller.pipeliningcontroller.requests.RequestBuilder;
import eu.freme.bservices.controller.pipeliningcontroller.requests.RequestFactory;
import eu.freme.bservices.controller.pipeliningcontroller.requests.ServiceConstants;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.persistence.model.SerializedRequest;

/**
 * Created by Arne on 21.01.2016.
 */
public class MockupRequestFactory {
    static final String mockupEntitySpotlight = "/";
    static final String mockupEntityFremeNER = "/";
    static final String mockupLink = "/";
    static final String mockupTranslation = "/";
    static final String mockupTerminology = "/";

    private final String baseURL;

    public MockupRequestFactory(String baseURL){
        this.baseURL = baseURL;
    }

    @SuppressWarnings("unused")
    public SerializedRequest createEntitySpotlight(final String text, final String language) {
        SerializedRequest request = RequestFactory.createEntitySpotlight(text, language);
        request.setEndpoint(baseURL+mockupEntitySpotlight);
        return request;
    }

    @SuppressWarnings("unused")
    public SerializedRequest createEntitySpotlight(final String language) {
        SerializedRequest request = RequestFactory.createEntitySpotlight(language);
        request.setEndpoint(baseURL+mockupEntitySpotlight);
        return request;
    }

    @SuppressWarnings("unused")
    public SerializedRequest createEntityFremeNER(final String text, final String language, final String dataSet) {
        SerializedRequest request = RequestFactory.createEntityFremeNER(text, language, dataSet);
        request.setEndpoint(baseURL+mockupEntityFremeNER);
        return request;
    }

    @SuppressWarnings("unused")
    public SerializedRequest createEntityFremeNER(final String language, final String dataSet) {
        SerializedRequest request = RequestFactory.createEntityFremeNER(language, dataSet);
        request.setEndpoint(baseURL+mockupEntityFremeNER);
        return request;
    }

    @SuppressWarnings("unused")
    public SerializedRequest createLink(final String templateID) {
        SerializedRequest request = RequestFactory.createLink(templateID);
        request.setEndpoint(baseURL+mockupLink);
        return request;
    }

    @SuppressWarnings("unused")
    public SerializedRequest createTranslation(final String text, final String sourceLang, final String targetLang) {
        SerializedRequest request = RequestFactory.createTranslation(text, sourceLang, targetLang);
        request.setEndpoint(baseURL+mockupTranslation);
        return request;
    }

    @SuppressWarnings("unused")
    public SerializedRequest createTranslation(final String sourceLang, final String targetLang) {
        SerializedRequest request = RequestFactory.createTranslation(sourceLang, targetLang);
        request.setEndpoint(baseURL+mockupTranslation);
        return request;
    }

    @SuppressWarnings("unused")
    public SerializedRequest createTerminology(final String sourceLang, final String targetLang) {
        SerializedRequest request = RequestFactory.createTerminology(sourceLang, targetLang);
        request.setEndpoint(baseURL+mockupTerminology);
        return request;
    }



}
