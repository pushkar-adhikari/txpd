package info.pushkaradhikari.txpd.core.business.error;

import java.io.IOException;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import info.pushkaradhikari.txpd.core.business.error.exception.AbstractException;
import info.pushkaradhikari.txpd.core.business.error.exception.BadRequestException;
import info.pushkaradhikari.txpd.core.business.error.exception.IllegalArgumentException;
import info.pushkaradhikari.txpd.core.business.error.exception.NotFoundException;
import info.pushkaradhikari.txpd.core.business.error.exception.PreconditionFailedException;
import info.pushkaradhikari.txpd.core.business.error.exception.ServerErrorException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	private static final String PACKAGE_ROOT = "info.pushkaradhikari";

	// 400

	@ExceptionHandler({ BadRequestException.class })
	public ResponseEntity<Object> handleBadRequest(final BadRequestException ex, final WebRequest request) {
		log.debug("bad request", ex);

		return handleExceptionInternal(ex, ex.getErrorMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler({ IllegalArgumentException.class })
	public ResponseEntity<Object> handleIllegalArgument(final IllegalArgumentException ex, final WebRequest request) {
		log.debug("illegal argument", ex);

		return handleExceptionInternal(ex, ex.getErrorMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(final HttpMessageNotReadableException ex,
			final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
		RestExceptionResponse bodyOfResponse = new RestExceptionResponse(status, "HTTP message is not readable.");

		if (ex.getCause() instanceof JsonMappingException || ex.getCause() instanceof JsonParseException) {
			bodyOfResponse.add("Could not parse JSON");
			JsonProcessingException jsonException = (JsonProcessingException) ex.getCause();
			JsonLocation jsonLocation = jsonException.getLocation();
			if (jsonLocation != null) {
				bodyOfResponse.add("on line: " + jsonLocation.getLineNr() + " column: " + jsonLocation.getColumnNr());
				bodyOfResponse.add(jsonException.getOriginalMessage());
			}
		} else if (ex.getCause() instanceof IOException) {
			bodyOfResponse.add(ex.getClass().getSimpleName());
			bodyOfResponse.add(ex.getMessage());
		}

		log.debug("message not readable", ex);

		return handleExceptionInternal(ex, bodyOfResponse, headers, HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
			final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
		ErrorMessage errorMessage = new ErrorMessage(400, "Data validation errors");

		log.debug("method argument not valid", ex);

		return handleExceptionInternal(ex, errorMessage, headers, HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(final TypeMismatchException ex, final HttpHeaders headers,
			final HttpStatus status, final WebRequest request) {
		RestExceptionResponse bodyOfResponse = new RestExceptionResponse(status, "type mismatch");
		bodyOfResponse.add(ex.getMessage());
		bodyOfResponse.add("failing value: " + ex.getValue().toString());

		log.debug("type mismatch", ex);

		return handleExceptionInternal(ex, bodyOfResponse, headers, HttpStatus.BAD_REQUEST, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		ErrorMessage errorMessage = new ErrorMessage(400, "Missing required parameter: " + ex.getParameterName());
		
		log.debug("method argument missing", ex);
		
		return handleExceptionInternal(ex, errorMessage, headers, HttpStatus.BAD_REQUEST, request);
	}

	// 401

	@ExceptionHandler({ AuthenticationException.class })
	protected ResponseEntity<Object> handleAuthenticationException(final AuthenticationException ex,
			final WebRequest request) {
		RestExceptionResponse bodyOfResponse = new RestExceptionResponse(HttpStatus.UNAUTHORIZED, "Bad Credentials");

		log.debug("Bad Credentials", ex);

		return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
	}

	// 403

	@ExceptionHandler({ AccessDeniedException.class })
	protected ResponseEntity<Object> handleAccessDeniedException(final AccessDeniedException ex,
			final WebRequest request) {
		RestExceptionResponse bodyOfResponse = new RestExceptionResponse(HttpStatus.FORBIDDEN,
				"forbidden / access denied");

		log.debug("forbidden / access denied", ex);

		return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
	}

	// 404

	@ExceptionHandler({ NotFoundException.class })
	public ResponseEntity<Object> handleNotFound(final NotFoundException ex, final WebRequest request) {
		log.debug("not found", ex);

		return handleExceptionInternal(ex, ex.getErrorMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
	}

	// 412

	@ExceptionHandler({ PreconditionFailedException.class })
	protected ResponseEntity<Object> handlePreconditionFailed(final PreconditionFailedException ex,
			final WebRequest request) {
		RestExceptionResponse bodyOfResponse = new RestExceptionResponse(HttpStatus.PRECONDITION_FAILED,
				"Precondition failed");

		log.debug("precondition failed", ex);

		return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.PRECONDITION_FAILED, request);
	}

	// 500

	@ExceptionHandler({ ServerErrorException.class })
	public ResponseEntity<Object> handleServerError(final AbstractException ex, final WebRequest request) {
		Object body = null;
		Throwable cause = ex.getCause();
		if (cause != null) {
			RestExceptionResponse bodyOfResponse = new RestExceptionResponse(ex.getErrorMessage());
			while (cause != null) {
				bodyOfResponse.add(cause.getMessage());
				cause = cause.getCause();
			}
			body = bodyOfResponse;
		} else {
			body = ex.getErrorMessage();
		}

		log.debug("server error", ex);

		return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
	}

	// all other exceptions based on RuntimeException
	@ExceptionHandler({ RuntimeException.class })
	public ResponseEntity<Object> handleInternal(final RuntimeException ex, final WebRequest request) {
		RestExceptionResponse bodyOfResponse = new RestExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR,
				"internal server error.");

		getExceptionDetails(ex, bodyOfResponse, ex.getStackTrace());

		log.error("handleInternal", ex);

		return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR,
				request);
	}

	private void getExceptionDetails(final RuntimeException ex, RestExceptionResponse bodyOfResponse,
			StackTraceElement[] stackTrace) {
		bodyOfResponse.add(ex.getClass().getSimpleName());
		bodyOfResponse.add(ex.getMessage());
		Throwable cause = ex.getCause();
		while (cause != null) {
			bodyOfResponse.add(cause.getMessage());
			cause = cause.getCause();
		}
		bodyOfResponse.add(stackTrace[0].toString());
		for (int i = 1; i < stackTrace.length; i++) {
			String stackTraceLine = stackTrace[i].toString();
			if (stackTraceLine.startsWith(PACKAGE_ROOT)) {
				bodyOfResponse.add(stackTraceLine);
			}
		}
	}
}