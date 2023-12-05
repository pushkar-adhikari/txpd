package info.pushkaradhikari.txpd.core.business.error;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import info.pushkaradhikari.txpd.core.business.error.exception.BadRequestException;
import info.pushkaradhikari.txpd.core.business.error.exception.ClientErrorException;
import info.pushkaradhikari.txpd.core.business.error.exception.NotFoundException;
import info.pushkaradhikari.txpd.core.business.error.exception.PreconditionFailedException;
import info.pushkaradhikari.txpd.core.business.error.exception.ServerErrorException;
import info.pushkaradhikari.txpd.core.business.error.exception.TimeOutException;
import info.pushkaradhikari.txpd.core.business.error.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RestResponseErrorHandler implements ResponseErrorHandler {

	@Override
	public boolean hasError(final ClientHttpResponse response) throws IOException {
		return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
	}

	@Override
	public void handleError(final ClientHttpResponse response) throws IOException {
		final String apiError = "Error from api server " + "{" + response.getStatusCode().getReasonPhrase() + "-"
				+ response.getStatusCode().value() + "}";

		final ErrorMessage errorMessage = new ErrorMessage(response.getStatusCode().value(), "API Error");
		final HttpStatus responseStatus = response.getStatusCode();

		log.error(apiError);

		if (responseStatus.is4xxClientError()) {
			switch (responseStatus) {
			case BAD_REQUEST:
				throw new BadRequestException(errorMessage);
			case UNAUTHORIZED:
			case FORBIDDEN:
				throw new UnauthorizedException(errorMessage);
			case NOT_FOUND:
				throw new NotFoundException(errorMessage);
			case PRECONDITION_FAILED:
				throw new PreconditionFailedException(errorMessage);
			case REQUEST_TIMEOUT:
				throw new TimeOutException(errorMessage);
			default:
				throw new ClientErrorException(errorMessage);
			}

		}

		if (responseStatus.is5xxServerError()) {
			if (responseStatus.equals(HttpStatus.GATEWAY_TIMEOUT)) {
				throw new TimeOutException(errorMessage);
			} else {
				throw new ServerErrorException(errorMessage);
			}
		}

	}
}
