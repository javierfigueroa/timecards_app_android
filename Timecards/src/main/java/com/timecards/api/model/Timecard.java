package com.timecards.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;

/**
 * Created by javier on 9/18/13.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Timecard {
//    2013-09-23 15:21:59.764 JAFTimecardPrototype[1400:907] {
//        "created_at" = "2013-09-23T19:21:51.101Z";
//        id = 7;
//        "latitude_in" = "25.97236888951247";
//        "latitude_out" = "<null>";
//        "longitude_in" = "-80.34612609081002";
//        "longitude_out" = "<null>";
//        "photo_in_content_type" = "image/jpeg";
//        "photo_in_file_name" = "clock_in.jpeg";
//        "photo_in_file_size" = 14202;
//        "photo_in_updated_at" = "2013-09-23T19:21:51.098Z";
//        "photo_out_content_type" = "<null>";
//        "photo_out_file_name" = "<null>";
//        "photo_out_file_size" = "<null>";
//        "photo_out_updated_at" = "<null>";
//        "timestamp_in" = "2013-09-23T19:21:00.000Z";
//        "timestamp_out" = "<null>";
//        "updated_at" = "2013-09-23T19:21:51.101Z";
//        "user_id" = 2;
//    }

    @JsonProperty(value = "id")
    String id;

    @JsonProperty(value = "project_id")
    String projectId;
    @JsonProperty(value = "photo_in_url")
    String photoInUrl;
    @JsonProperty(value = "photo_out_url")
    String photoOutUrl;
    @JsonProperty(value = "timestamp_in")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss", timezone="UTC")
    Date timestampIn;
    @JsonProperty(value = "timestamp_out")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss", timezone="UTC")
    Date timestampOut;
    @JsonProperty(value = "latitude_in")
    Double latitudeIn;
    @JsonProperty(value = "latitude_out")
    Double latitudeOut;
    @JsonProperty(value = "longitude_in")
    Double longitudeIn;
    @JsonProperty(value = "longitude_out")
    Double longitudeOut;
    @JsonProperty(value = "user_id")
    int userId;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public int getUserId() {
        return userId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotoOutUrl() {
        return photoOutUrl;
    }

    public void setPhotoOutUrl(String photoOutUrl) {
        this.photoOutUrl = photoOutUrl;
    }

    public String getPhotoInUrl() {
        return photoInUrl;
    }

    public void setPhotoInUrl(String photoInUrl) {
        this.photoInUrl = photoInUrl;
    }

    public void setTimestampIn(Date timestampIn) {
        this.timestampIn = timestampIn;
    }

    public void setTimestampOut(Date timestampOut) {
        this.timestampOut = timestampOut;
    }

    public void setLatitudeIn(Double latitudeIn) {
        this.latitudeIn = latitudeIn;
    }

    public void setLatitudeOut(Double latitudeOut) {
        this.latitudeOut = latitudeOut;
    }

    public void setLongitudeIn(Double longitudeIn) {
        this.longitudeIn = longitudeIn;
    }

    public void setLongitudeOut(Double longitudeOut) {
        this.longitudeOut = longitudeOut;
    }

    public String getId() {
        return id;
    }

    public Double getLongitudeOut() {
        return longitudeOut;
    }

    public Date getTimestampIn() {
        return timestampIn;
    }

    public Date getTimestampOut() {
        return timestampOut;
    }

    public Double getLatitudeIn() {
        return latitudeIn;
    }

    public Double getLatitudeOut() {
        return latitudeOut;
    }

    public Double getLongitudeIn() {
        return longitudeIn;
    }
}
