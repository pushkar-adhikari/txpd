package info.pushkaradhikari.txpd.core.business.error;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class RestExceptionResponse {
	
	@Getter
	@Setter
    ErrorMessage errorMessage;
	
	@Getter
	@Setter
    List<String> errorMessages = new ArrayList<String>();

    public RestExceptionResponse(HttpStatus httpStatus, String message) {
        errorMessage = new ErrorMessage();
        errorMessage.setCode(httpStatus.value() + "");
        errorMessage.setMessage(message);
    }

    public RestExceptionResponse(ErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void add(String longMessage) {
        if (longMessage != null) {
            for (String message : longMessage.split("\n")) {
                errorMessages.add(message);
            }
        }
    }

}