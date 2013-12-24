package com.timecards.api;

import com.timecards.api.model.Project;
import com.timecards.api.model.Timecard;
import com.timecards.api.model.User;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;


/**
 * Created by javier on 9/23/13.
 */
public interface Endpoints {

    @GET(RestConstants.TODAY_TIMECARD)
    public void getToday(Callback<Timecard> callback);


    @GET(RestConstants.PROJECTS)
    public void getProjects(Callback<List<Project>> callback);

    @Multipart
    @PUT(RestConstants.SET_PROJECT)
    public void setProject(@Path("id") String timecardId,
                           @Part("timecard[project_id]") TypedString projectId,
                           Callback<Timecard> callback);

    @FormUrlEncoded
    @POST(RestConstants.LOGIN_URL)
    public void signIn(@Field("user[email]") String email,
                       @Field("user[password]") String password,
                       Callback<User> callback);

    @Multipart
    @POST(RestConstants.CLOCK_IN)
    public void clockIn(@Part("timecard[photo_in];") retrofit.mime.TypedFile photo,
                        @Part("timecard[latitude_in]") TypedString latitude,
                        @Part("timecard[longitude_in]") TypedString longitude,
                        @Part("timecard[timestamp_in(1i)]") TypedString year,
                        @Part("timecard[timestamp_in(2i)]") TypedString month,
                        @Part("timecard[timestamp_in(3i)]") TypedString day,
                        @Part("timecard[timestamp_in(4i)]") TypedString hour,
                        @Part("timecard[timestamp_in(5i)]") TypedString minute,
                        Callback<Timecard> callback);

    @Multipart
    @PUT(RestConstants.CLOCK_OUT)
    public void clockOut(@Path("id") String timecardId,
                         @Part("timecard[photo_out];") TypedFile photo,
                         @Part("timecard[latitude_out]") TypedString latitude,
                         @Part("timecard[longitude_out]") TypedString longitude,
                         @Part("timecard[timestamp_out(1i)]") TypedString year,
                         @Part("timecard[timestamp_out(2i)]") TypedString month,
                         @Part("timecard[timestamp_out(3i)]") TypedString day,
                         @Part("timecard[timestamp_out(4i)]") TypedString hour,
                         @Part("timecard[timestamp_out(5i)]") TypedString minute,
                         Callback<Timecard> callback);
}
