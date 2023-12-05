package info.pushkaradhikari.txpd.core.mapper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JacksonObjectMapper extends ObjectMapper {
    private static final long serialVersionUID = 1L;

    // default date format for java.time.LocalDate
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public JacksonObjectMapper() {
        // write ISO dates
    	dateFormat.setLenient(false);
    	setDateFormat(dateFormat);
        setTimeZone(TimeZone.getDefault());
        // JavaTimeModule module = new JavaTimeModule();
        // module.addSerializer(new CustomLocalDateTimeSerializer());
        // registerModule(module);
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // prevent writing BigDecimals in scientific notation
        configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);

        // suppress nulls
        setDefaultPropertyInclusion(JsonInclude.Value.construct(Include.ALWAYS, Include.NON_NULL));
        setSerializationInclusion(Include.NON_NULL);

        // output formatting
        configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        configure(SerializationFeature.INDENT_OUTPUT, true);
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    class CustomLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value != null) {
                String formattedDate = String.format("%04d-%02d-%02d %02d:%02d:%02d",
                        value.getYear(), value.getMonthValue(), value.getDayOfMonth(),
                        value.getHour(), value.getMinute(), value.getSecond());
                gen.writeString(formattedDate);
            } else {
                gen.writeNull();
            }
        }

        @Override
        public Class<LocalDateTime> handledType() {
            return LocalDateTime.class;
        }
    }
}
