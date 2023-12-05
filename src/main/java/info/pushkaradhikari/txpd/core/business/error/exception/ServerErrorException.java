package info.pushkaradhikari.txpd.core.business.error.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import info.pushkaradhikari.txpd.core.business.error.ErrorMessage;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class ServerErrorException extends AbstractException {
    private static final long serialVersionUID = 1L;

    public ServerErrorException(ErrorMessage errorMessage) {
        super(errorMessage);
    }

    public ServerErrorException(ErrorMessage errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

}
