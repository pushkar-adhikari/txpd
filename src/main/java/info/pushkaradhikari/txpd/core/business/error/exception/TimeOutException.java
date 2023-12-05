package info.pushkaradhikari.txpd.core.business.error.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import info.pushkaradhikari.txpd.core.business.error.ErrorMessage;

@ResponseStatus(code = HttpStatus.REQUEST_TIMEOUT)
public class TimeOutException extends AbstractException {
    private static final long serialVersionUID = 1L;

    public TimeOutException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
