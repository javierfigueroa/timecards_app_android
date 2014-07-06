package com.timecards.api;

import android.content.Context;

import com.timecards.api.model.Project;
import com.timecards.api.model.Timecard;
import com.timecards.api.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import retrofit.Callback;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by javier on 9/16/13.
 */
public class Service {

    public static void signIn(Context context,
                              String username,
                              String password,
                              String company,
                              Callback<User> callback) {

        RestConstants.getInstance().setCompany(company);

        Endpoints service = ServiceAdapter.getServiceAdapter(context, true).create(Endpoints.class);
        service.signIn(username, password, callback);
    }

    public static void forgotPassword(Context context,
                              String username,
                              String company,
                              Callback<String> callback) {

        RestConstants.getInstance().setCompany(company);

        Endpoints service = ServiceAdapter.getServiceAdapter(context, true).create(Endpoints.class);
        service.forgotPassword(username, company, callback);
    }

    public static void getToday(Context context,
                                Callback<Timecard> callback) {

        Endpoints service = ServiceAdapter.getServiceAdapter(context).create(Endpoints.class);
        service.getToday(callback);
    }

    public static void getProjects(Context context,
                                   Callback<List<Project>> callback) {
        Endpoints service = ServiceAdapter.getServiceAdapter(context).create(Endpoints.class);
        service.getProjects(callback);
    }

    public static void setProject(Context context,
                                  Timecard timecard,
                                  Project project,
                                  Callback<Timecard> callback) {

        TypedString projectId = new TypedString(String.valueOf(project.getId()));
        Endpoints service = ServiceAdapter.getServiceAdapter(context).create(Endpoints.class);
        service.setProject(timecard.getId(), projectId, callback);
    }

    public static void clockIn(final Context context,
                               Timecard timecard,
                               byte[] photo,
                               Callback<Timecard> callback) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(timecard.getTimestampIn());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TypedFile image = new TypedFile("image/jpeg", getFile(context, photo, "clock_in.jpeg"));
        TypedString latitude = new TypedString(String.valueOf(timecard.getLatitudeIn()));
        TypedString longitude = new TypedString(String.valueOf(timecard.getLongitudeIn()));
        TypedString yearString = new TypedString(String.valueOf(year));
        TypedString monthString = new TypedString(String.valueOf(month));
        TypedString dayString = new TypedString(String.valueOf(day));
        TypedString hourString = new TypedString(String.valueOf(hour));
        TypedString minuteString = new TypedString(String.valueOf(minute));

        Endpoints service = ServiceAdapter.getServiceAdapter(context).create(Endpoints.class);
        service.clockIn(image, latitude, longitude, yearString, monthString, dayString, hourString, minuteString, callback);
    }

    public static void clockOut(final Context context,
                               Timecard timecard,
                               byte[] photo,
                               Callback<Timecard> callback) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(timecard.getTimestampOut());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TypedFile image = new TypedFile("image/jpeg", getFile(context, photo, "clock_out.jpeg"));
        TypedString latitude = new TypedString(String.valueOf(timecard.getLatitudeOut()));
        TypedString longitude = new TypedString(String.valueOf(timecard.getLongitudeOut()));
        TypedString yearString = new TypedString(String.valueOf(year));
        TypedString monthString = new TypedString(String.valueOf(month));
        TypedString dayString = new TypedString(String.valueOf(day));
        TypedString hourString = new TypedString(String.valueOf(hour));
        TypedString minuteString = new TypedString(String.valueOf(minute));

        Endpoints service = ServiceAdapter.getServiceAdapter(context).create(Endpoints.class);
        service.clockOut(timecard.getId(), image, latitude, longitude, yearString, monthString, dayString, hourString, minuteString, callback);
    }

    private static File getFile(Context context, byte[] photo, String filename) {
        File file = new File(context.getCacheDir(), filename);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(photo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}