package info.pushkaradhikari.txpd.core.business.error.exception;

import org.apache.commons.lang3.StringUtils;

import info.pushkaradhikari.txpd.core.business.error.ErrorMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	protected final ErrorMessage errorMessage;

	public AbstractException(ErrorMessage errorMessage) {
		super();
		this.errorMessage = errorMessage;
	}

	public AbstractException(ErrorMessage errorMessage, Throwable cause) {
		super(cause);
		this.errorMessage = errorMessage;
	}

	public ErrorMessage getErrorMessage() {
		return errorMessage;
	}

	public void logErrorMessage() {
		log.error(this.getClass().getSimpleName() + ": " + this.errorMessage.toString());
		if (StringUtils.isNotBlank(getMessage())) {
			log.error(getMessage());
		}
	}
}