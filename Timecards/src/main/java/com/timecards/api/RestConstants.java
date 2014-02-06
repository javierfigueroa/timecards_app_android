package com.timecards.api;

/**
 * Created by javier on 9/16/13.
 */
public class RestConstants {

    public static final String TAG = RestConstants.class.getSimpleName();

    private final String BASE_URL = ".firehound.co:3000/";

    public static final String LOGIN_URL = "/users/sign_in.json";

    public static final String TODAY_TIMECARD = "/timecards/today.json";

    public static final String CLOCK_IN = "/timecards.json";

    public static final String CLOCK_OUT = "/timecards/{id}.json";

    public static final String PROJECTS = "/projects.json";

    public static final String SET_PROJECT = "/timecards/{id}.json";

    private String mCompany;

    private static RestConstants instance;

    public static RestConstants getInstance(){
        if (instance == null) {
            instance = new RestConstants();
        }

        return instance;
    }

    public String getBaseUrl() {
        return "http://" + mCompany + BASE_URL;
    }

    public void setCompany(String company) {
        mCompany = company;
    }
}
