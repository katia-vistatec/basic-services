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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


public class ProxyServletRequest extends HttpServletRequestWrapper {

    private String requestUrl;


    public ProxyServletRequest(HttpServletRequest request, String requestUrl) {

        super(request);
        this.requestUrl = requestUrl;
    }




    @Override
    public String getParameter(String name) {
        if (name.toLowerCase().equals("requesturl")) {
            return this.requestUrl;
        } else {
            return super.getParameter(name);
        }
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        TreeMap<String, String[]> map = new TreeMap<String, String[]>();
        map.putAll(super.getParameterMap());
        map.remove("requestUrl");
        map.put("requestUrl", new String[] {requestUrl});


        return Collections.unmodifiableMap(map);
    }

}

