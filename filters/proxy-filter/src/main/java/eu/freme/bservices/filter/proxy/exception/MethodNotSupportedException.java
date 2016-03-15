package eu.freme.bservices.filter.proxy.exception;

import org.springframework.http.HttpStatus;

import eu.freme.common.exception.FREMEHttpException;

public class MethodNotSupportedException extends FREMEHttpException{

	public MethodNotSupportedException(String method){
		super("HTTP Method \"" + method + "\" not supported.", HttpStatus.BAD_REQUEST);
	}
}
