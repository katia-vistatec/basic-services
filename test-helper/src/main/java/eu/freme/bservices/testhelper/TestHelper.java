package eu.freme.bservices.testhelper;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Component
public class TestHelper implements ApplicationContextAware{

	private ApplicationContext context;

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
	 * @return
	 */
	public String getAdminUsername(){
		return context.getEnvironment().getProperty("admin.username");
	}
	
	/**
	 * Return the password of the administrator user of the REST API.
	 *
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
