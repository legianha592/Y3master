package com.y3technologies.masters.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice(basePackages = "com.y3technologies")
public class RestExceptionHandler {
	private static final String UNEXPECTED_ERROR = "exception.unexpected";

	private final MessageSource messageSource;

	@Autowired
	public RestExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(TransactionException.class)
	public ResponseEntity<RestErrorMessage> handleIllegalArgument(TransactionException ex, Locale locale) {
		log.error(ex.getMessage());
		String errorMessage = messageSource.getMessage(ex.getMessage(), ex.getArguments(), locale);
		RestErrorMessage restMessage = new RestErrorMessage(errorMessage, HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(restMessage, restMessage.getStatus());
	}

	@ExceptionHandler(FeignException.class)
	public ResponseEntity<RestErrorMessage> handleFeignException(FeignException ex, Locale locale) {
		log.error(ex.getLocalizedMessage());
		RestErrorMessage restMessage = new RestErrorMessage(ex.getMessages(), ex.getStatus());
		return new ResponseEntity<>(restMessage, restMessage.getStatus());
	}

	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<RestErrorMessage> handleIllegalArgument(ValidationException ex, Locale locale) {
		log.error(ex.getMessage());
		ArrayList<String> errors = new ArrayList<>();
		for (String resourceKey : ex.getValidationExceptions().keySet()) {
			Object[] args = ex.getValidationExceptions().get(resourceKey);
			errors.add(messageSource.getMessage(resourceKey, args, locale));
		}
		RestErrorMessage restMessage = new RestErrorMessage(errors, HttpStatus.UNPROCESSABLE_ENTITY);
		return new ResponseEntity<>(restMessage, restMessage.getStatus());
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<RestErrorMessage> handleExceptions(Exception ex, Locale locale) {
		log.error(ex.getMessage(), ex);
		String errorMessage = messageSource.getMessage(UNEXPECTED_ERROR, null, locale);
		RestErrorMessage restMessage = new RestErrorMessage(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		return new ResponseEntity<>(restMessage, restMessage.getStatus());
	}

	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<RestErrorMessage> handleIllegalArgument(MethodArgumentNotValidException ex, Locale locale) {
		log.error(ex.getMessage());
		BindingResult result = ex.getBindingResult();
		java.util.List<FieldError> fieldErrors = result.getFieldErrors();
		RestErrorMessage restMessage = new RestErrorMessage(new ArrayList<String>(), HttpStatus.UNPROCESSABLE_ENTITY);
		for (FieldError fieldError : fieldErrors) {
			restMessage.addErrorMessage(fieldError.getDefaultMessage());
		}
		return new ResponseEntity<>(restMessage, restMessage.getStatus());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(BindingResultException.class)
	public ResponseEntity<RestErrorMessage> handleBindingResultException(BindingResultException ex, Locale locale) {

		BindingResult bindingResult = ex.getBindingResult();

		ArrayList<String> errorList = new ArrayList<>();

		if (bindingResult.hasErrors()) {
			List<ObjectError> objectErros = bindingResult.getAllErrors();
			for (ObjectError objectError : objectErros) {

				errorList.add(messageSource.getMessage(objectError, locale));
			}
		}

		RestErrorMessage restMessage = new RestErrorMessage(errorList, HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(restMessage, restMessage.getStatus());
	}
}
