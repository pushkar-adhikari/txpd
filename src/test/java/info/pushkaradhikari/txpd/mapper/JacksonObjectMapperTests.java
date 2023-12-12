package info.pushkaradhikari.txpd.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import info.pushkaradhikari.txpd.core.mapper.JacksonObjectMapper;
import info.pushkaradhikari.txpd.processor.dto.EventDTO;

public class JacksonObjectMapperTests {


    @Test
    void jsonMapsCorrectly() throws Exception {

        JacksonObjectMapper mapper = new JacksonObjectMapper();
        List<EventDTO> events = Arrays.asList(mapper.readValue(input, EventDTO[].class));

        String format = "yyyy-MM-dd HH:mm:ss[.SSSSSSSSS]";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		LocalDateTime t = LocalDateTime.parse("2023-10-28 17:25:37.380000000", formatter);
        EventDTO event = events.get(0);
        assertEquals(t, event.getPackageLogDetailStart());
        assertEquals(10984753, event.getPackageLogDetailId());
        assertEquals("Execute Execution Package course_registration", event.getPackageLogDetailName());
    }
    
    
    
    String input = "[\n" + //
            "  {\n" + //
            "    \"PackageLogDetailId\": 10984753,\n" + //
            "    \"PackageLogDetailName\": \"Execute Execution Package course_registration\",\n" + //
            "    \"PackageLogDetailObjectName\": \"course_registration\",\n" + //
            "    \"PackageLogDetailStart\": \"2023-10-28 17:25:37.380000000\",\n" + //
            "    \"PackageLogDetailEnd\": \"2023-10-28 17:26:30.767000000\",\n" + //
            "    \"PackageLogDetailEndStatus\": 0,\n" + //
            "    \"PackageLogId\": \"{62D2CCA8-DBDA-4A38-A751-DB35EC3216FE}\",\n" + //
            "    \"PackageLogName\": \"ADK_5m\",\n" + //
            "    \"PackageLogStart\": \"2023-10-28 17:25:37.040000000\",\n" + //
            "    \"PackageLogEnd\": \"\",\n" + //
            "    \"PackageLogEndStatus\": 0,\n" + //
            "    \"PackageId\": \"{5B018014-D37F-4B49-A921-A0E27A2677DB}\",\n" + //
            "    \"PackageName\": \"ADK_5m\",\n" + //
            "    \"ProjectId\": \"{B2FD6808-4A72-441C-9AC2-910D90F7355D}\",\n" + //
            "    \"ProjectName\": \"DSA_ADK\"\n" + //
            "  },\n" + //
            "  {\n" + //
            "    \"PackageLogDetailId\": 10984774,\n" + //
            "    \"PackageLogDetailName\": \"Execute Execution Package offered_course\",\n" + //
            "    \"PackageLogDetailObjectName\": \"offered_course\",\n" + //
            "    \"PackageLogDetailStart\": \"2023-10-28 17:26:31.530000000\",\n" + //
            "    \"PackageLogDetailEnd\": \"2023-10-28 17:27:39.543000000\",\n" + //
            "    \"PackageLogDetailEndStatus\": 0,\n" + //
            "    \"PackageLogId\": \"{62D2CCA8-DBDA-4A38-A751-DB35EC3216FE}\",\n" + //
            "    \"PackageLogName\": \"ADK_5m\",\n" + //
            "    \"PackageLogStart\": \"2023-10-28 17:25:37.040000000\",\n" + //
            "    \"PackageLogEnd\": \"\",\n" + //
            "    \"PackageLogEndStatus\": 0,\n" + //
            "    \"PackageId\": \"{5B018014-D37F-4B49-A921-A0E27A2677DB}\",\n" + //
            "    \"PackageName\": \"ADK_5m\",\n" + //
            "    \"ProjectId\": \"{B2FD6808-4A72-441C-9AC2-910D90F7355D}\",\n" + //
            "    \"ProjectName\": \"DSA_ADK\"\n" + //
            "  },\n" + //
            "  {\n" + //
            "    \"PackageLogDetailId\": 10984785,\n" + //
            "    \"PackageLogDetailName\": \"Execute Execution Package course\",\n" + //
            "    \"PackageLogDetailObjectName\": \"course\",\n" + //
            "    \"PackageLogDetailStart\": \"2023-10-28 17:27:39.607000000\",\n" + //
            "    \"PackageLogDetailEnd\": \"2023-10-28 17:28:09.260000000\",\n" + //
            "    \"PackageLogDetailEndStatus\": 0,\n" + //
            "    \"PackageLogId\": \"{62D2CCA8-DBDA-4A38-A751-DB35EC3216FE}\",\n" + //
            "    \"PackageLogName\": \"ADK_5m\",\n" + //
            "    \"PackageLogStart\": \"2023-10-28 17:25:37.040000000\",\n" + //
            "    \"PackageLogEnd\": \"2023-10-28 17:28:09.263000000\",\n" + //
            "    \"PackageLogEndStatus\": 0,\n" + //
            "    \"PackageId\": \"{5B018014-D37F-4B49-A921-A0E27A2677DB}\",\n" + //
            "    \"PackageName\": \"ADK_5m\",\n" + //
            "    \"ProjectId\": \"{B2FD6808-4A72-441C-9AC2-910D90F7355D}\",\n" + //
            "    \"ProjectName\": \"DSA_ADK\"\n" + //
            "  }\n" + //
            "]";
}
