package info.pushkaradhikari.txpd.core.mapper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

public class JacksonObjectMapper extends ObjectMapper {
    private static final long serialVersionUID = 1L;

    // default date format for java.time.LocalDate
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    DateTimeFormatter format = new DateTimeFormatterBuilder().appendPattern(DATETIME_FORMAT)
            .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd()
            .toFormatter();
    private LocalDateTimeSerializer LOCAL_DATETIME_SERIALIZER = new LocalDateTimeSerializer(format);
    private LocalDateTimeDeserializer LOCAL_DATETIME_DESERIALIZER = new LocalDateTimeDeserializer(format);

    public JacksonObjectMapper() {
        // write ISO dates
        dateFormat.setLenient(false);
        setDateFormat(dateFormat);
        setTimeZone(TimeZone.getDefault());
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LOCAL_DATETIME_SERIALIZER);
        module.addDeserializer(LocalDateTime.class, LOCAL_DATETIME_DESERIALIZER);
        registerModule(module);
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
}
