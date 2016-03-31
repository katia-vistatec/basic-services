package eu.freme.bservices.filter.proxy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import eu.freme.common.exception.FREMEHttpException;

@SuppressWarnings("serial")
@ResponseStatus(value=HttpStatus.BAD_GATEWAY, reason="")
public class BadGatewayException extends FREMEHttpException{

	public BadGatewayException(){
		super();
	}
	
	public BadGatewayException(String msg){
		super(msg);
	}
}
