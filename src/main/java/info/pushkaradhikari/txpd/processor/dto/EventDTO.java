package info.pushkaradhikari.txpd.processor.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("PackageLogDetailId")
    private Long packageLogDetailId;
    @JsonProperty("PackageLogDetailName")
    private String packageLogDetailName;
    @JsonProperty("PackageLogDetailObjectName")
    private String packageLogDetailObjectName;
    @JsonProperty("PackageLogDetailStart")
    private LocalDateTime packageLogDetailStart;
    @JsonProperty("PackageLogDetailEnd")
    private LocalDateTime packageLogDetailEnd;
    @JsonProperty("PackageLogDetailEndStatus")
    private Long packageLogDetailEndStatus;
    @JsonProperty("PackageLogId")
    private String packageLogId;
    @JsonProperty("PackageLogName")
    private String packageLogName;
    @JsonProperty("PackageLogStart")
    private LocalDateTime packageLogStart;
    @JsonProperty("PackageLogEnd")
    private LocalDateTime packageLogEnd;
    @JsonProperty("PackageLogEndStatus")
    private Long packageLogEndStatus;
    @JsonProperty("PackageId")
    private String packageId;
    @JsonProperty("PackageName")
    private String packageName;
    @JsonProperty("ProjectId")
    private String projectId;
    @JsonProperty("ProjectName")
    private String projectName;
}
