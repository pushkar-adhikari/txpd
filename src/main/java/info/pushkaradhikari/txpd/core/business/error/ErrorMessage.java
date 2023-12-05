package info.pushkaradhikari.txpd.core.business.error;

import java.time.LocalDateTime;
import java.time.ZoneId;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ErrorMessage {
    private String code;
    private String message;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp = LocalDateTime.now(ZoneId.systemDefault());

    public ErrorMessage() {
    }

    public ErrorMessage(String errorCode, String errorMessage) {
        this.code = errorCode;
        this.message = errorMessage;
    }
    
    public ErrorMessage(int errorCode, String errorMessage) {
        this.code = errorCode + "";
        this.message = errorMessage;
    }

    public boolean hasErrors() {
        return true;
    }
}