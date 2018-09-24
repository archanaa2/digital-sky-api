package com.ispirit.digitalsky.document;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ispirit.digitalsky.domain.ApplicantType;
import com.ispirit.digitalsky.util.*;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Convert;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Document(collection = "flyDronePermissionApplications")
@TypeAlias("flyDronePermissionApplications")
public class FlyDronePermissionApplication extends BasicApplication {

    @Field("name")
    @NotNull
    private String pilotId;

    @Field("flyArea")
    @NotNull
    private List<LatLong> flyArea;

    @Field("droneId")
    @NotNull
    private long droneId;

    @Field("operatorId")
    @NotNull
    private long operatorId;


    @Field("payloadWeightInKg")
    @NotNull
    private double payloadWeightInKg;

    @Field("payloadDetails")
    @NotNull
    @Size(max = 500)
    private String payloadDetails;

    @Field("flightPurpose")
    @NotNull
    @Size(max = 500)
    private String flightPurpose;


    @Field("startDateTime")
    @NotNull
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeSerializer.class)
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime startDateTime;


    @Field("endDateTime")
    @NotNull
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeSerializer.class)
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime endDateTime;


    public FlyDronePermissionApplication() {
        setCreatedDate(new Date());
        setLastModifiedDate(new Date());
    }

    public String getPilotId() {
        return pilotId;
    }

    public void setPilotId(String pilotId) {
        this.pilotId = pilotId;
    }

    public List<LatLong> getFlyArea() {
        return flyArea;
    }

    public void setFlyArea(List<LatLong> flyArea) {
        this.flyArea = flyArea;
    }

    public long getDroneId() {
        return droneId;
    }

    public void setDroneId(long droneId) {
        this.droneId = droneId;
    }

    public double getPayloadWeightInKg() {
        return payloadWeightInKg;
    }

    public void setPayloadWeightInKg(double payloadWeightInKg) {
        this.payloadWeightInKg = payloadWeightInKg;
    }

    public String getPayloadDetails() {
        return payloadDetails;
    }

    public void setPayloadDetails(String payloadDetails) {
        this.payloadDetails = payloadDetails;
    }

    public String getFlightPurpose() {
        return flightPurpose;
    }

    public void setFlightPurpose(String flightPurpose) {
        this.flightPurpose = flightPurpose;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(long operatorId) {
        this.operatorId = operatorId;
    }
}
