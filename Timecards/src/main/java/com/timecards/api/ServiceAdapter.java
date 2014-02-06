package com.timecards.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timecards.app.LoginActivity;
import com.timecards.api.model.User;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.ApacheClient;

/**
 * Created by javier on 9/23/13.
 */
public class ServiceAdapter {

    private RestAdapter restAdapter;
    private RequestInterceptor requestInterceptor;
    private ObjectMapper objectMapper;

    public void setContext(Context context) {
        this.context = context;
    }

    private Context context;

    private static ServiceAdapter serviceAdapter;

    public static RestAdapter getServiceAdapter(Context context) {
        return getServiceAdapter(context, false);
    }

    public static RestAdapter getServiceAdapter(Context context, boolean reset) {
        if (serviceAdapter == null || reset) {
            serviceAdapter = new ServiceAdapter();
        }

        serviceAdapter.setContext(context);
        return serviceAdapter.getRestAdapter();
    }

    private ServiceAdapter() {
    }

    public RestAdapter getRestAdapter() {
        if (restAdapter == null) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 100 * 1000);
            final DefaultHttpClient client = new DefaultHttpClient(httpParams);

            restAdapter = new RestAdapter.Builder()
                    .setServer(RestConstants.getInstance().getBaseUrl())
                    .setConverter(new JacksonConverter(getObjectMapper()))
                    .setRequestInterceptor(getRequestInterceptor())
                    .setClient(new ApacheClient(client))
                    .build();

            restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
        }

        return restAdapter;
    }

    private ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            objectMapper.setDateFormat(df);
        }

        return objectMapper;
    }

    private RequestInterceptor getRequestInterceptor() {
        if (requestInterceptor == null) {
            requestInterceptor = new RequestInterceptor()
            {
                @Override
                public void intercept(RequestFacade request) {
                    SharedPreferences savedSession = context.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
                    String json = savedSession.getString(LoginActivity.USER, "");
                    if (!TextUtils.isEmpty(json)) {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            User user = (User)mapper.readValue(json, User.class);
                            String auth =new String(Base64.encode((user.getEmail() + ":" + user.getPassword()).getBytes(), Base64.URL_SAFE | Base64.NO_WRAP));
                            request.addHeader("Authorization", "Basic " + auth);
                            request.addHeader("Accept", "application/json");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        }

        return requestInterceptor;
    }
}
