package eu.freme.bservices.testhelper;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import eu.freme.common.rest.BaseRestController;

import static org.junit.Assert.assertTrue;

@Component
public class TestHelper implements ApplicationContextAware{

	ApplicationContext context;

	Logger logger = Logger.getLogger(TestHelper.class);
	
	/**
	 * Returns the base url of the API given the spring application context, e.g. http://localhost:8080
	 * @return
	 */
	public String getAPIBaseUrl(){
		String port = context.getEnvironment().getProperty("server.port");
		if( port == null){
			port = "8080";
		}
		return "http://localhost:" + port;
	}
	
	/**
	 * Return the username of the administrator user of the REST API.
	 * 
	 * @param context
	 * @return
	 */
	public String getAdminUsername(){
		return context.getEnvironment().getProperty("admin.username");
	}
	
	/**
	 * Return the password of the administrator user of the REST API.
	 * 
	 * @param context
	 * @return
	 */
	public String getAdminPassword(){
		return context.getEnvironment().getProperty("admin.password");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = applicationContext;
		
	}
}
