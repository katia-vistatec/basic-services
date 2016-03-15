//package eu.freme.bservices.filter.proxy;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//
//import javax.servlet.ServletOutputStream;
//import javax.servlet.WriteListener;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpServletResponseWrapper;
//
//import org.apache.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import com.mashape.unirest.http.exceptions.UnirestException;
//import com.mashape.unirest.request.HttpRequest;
//
//import eu.freme.common.exception.InternalServerErrorException;
//
//public class ProxyResponseWrapper extends HttpServletResponseWrapper{
//
//	Logger logger = Logger.getLogger(ProxyResponseWrapper.class);
//	
//	public ProxyResponseWrapper(HttpServletResponse response, HttpRequest proxy) {
//		super(response);
//		
//		try {
//			ProxyService.writeProxyToResponse(response, proxy);
//		} catch (UnirestException | IOException e) {
//			logger.error("failed", e);
//			throw new InternalServerErrorException("proxy failed");
//		}
//	}
//	
//	
//}
