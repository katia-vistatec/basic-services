/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.freme.bservices.filters.proxy;


import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.*;

@Component
public class ProxyFilter extends GenericFilterBean {


    @Value("${proxy.enabled:true}")
    boolean proxyEnabled;


    @Value("${proxy.yaml:proxy.yaml}")
    String proxyYaml;

    Logger logger = Logger.getLogger(ProxyFilter.class);
    Properties proxyProperties;
    String path;

    public void clear() {};


    @PostConstruct
    public void setup() {
        try {
            refresh();
        } catch (IOException e) {
            logger.error("Caught IOException: "+proxyYaml+" for YAML Configuration of Proxy was not found." +
                    "The Proxy Filter was turned off.");
            proxyEnabled=false;
        }
    }


    public void refresh () throws IOException {
        clear();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        URL yamlfile = getClass().getClassLoader().getResource(proxyYaml);
        try {
            FileSystemResource fileSystemResource = new FileSystemResource(yamlfile.getFile());
            yaml.setResources(fileSystemResource);
            proxyProperties = yaml.getObject();
        } catch (NullPointerException e) {
            throw new IOException(proxyYaml+ "not Found in Filesystem");
        }

    }


    public ProxyFilter(){
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (!(req instanceof HttpServletRequest) || !(res instanceof HttpServletResponse)) {
            chain.doFilter(req, res);
            return;
        }
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (proxyEnabled) {
            path=request.getServletPath();

            if (proxyProperties.containsKey(path)) {
                RequestWrapper wrappedRequest = (RequestWrapper)(HttpServletRequest) new RequestWrapper(request);




                RequestWrapper wrapper = new RequestWrapper(request);
                String body = wrapper.getReader().readLine();

                String url = ((String) proxyProperties.get(path)) + "?" + request.getQueryString();
                try {
                    HttpResponse<String> proxiedResponse = Unirest.post(url)
                            .headers(getAllHeaders(request))
                            .body(body)
                            .asString();
                    Headers headers = proxiedResponse.getHeaders();

                    String contentType=headers.getFirst("Content-Type");
                    response.setContentType(contentType);
                    response.setContentLength(Integer.parseInt(headers.getFirst("Content-Length")));
                    response.setStatus(proxiedResponse.getStatus());
                    response.getWriter().write(proxiedResponse.getBody());

                } catch (UnirestException e) {
                    e.printStackTrace();
                    throw new ServletException("External Service failed after proxying with proxyUrl:" + url);
                }
                System.out.println(wrappedRequest);
            }

        } else {
            chain.doFilter(req, res);
        }
    }

    private boolean isMultipart(final HttpServletRequest request) {
        return request.getContentType()!=null && request.getContentType().startsWith("multipart/form-data");
    }

    public HashMap<String,String> getAllHeaders(HttpServletRequest r) {
      //  Headers headers= new Headers();
        HashMap<String,String> headers = new HashMap<>();
        List<String> current;
        final Set<String> relevant = new HashSet<>(Arrays.asList(new String[] {"content-type","informat","outformat","accept","x-auth-token","x-auth-password","x-auth-username"}));
        Enumeration<String> iterator = r.getHeaderNames();
        while (iterator.hasMoreElements()) {
            String s=iterator.nextElement();
            current= Collections.list(r.getHeaders(s));
            String header="";
            if (relevant.contains(s)) {
                for (String h : current){
                    header+=h+" ";
                }
            }
            if (!header.equals("")) {
                headers.put(s,header);
            }
        }
        return headers;
    }


    public void destroy() {}


}
