package com.amsavarthan.hify.models;

/*
 * Created by amsavarthan on 13/3/18.
 */

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MySingelton {

    private static MySingelton mInstance;
    private static Context context;
    private RequestQueue requestQueue;

    public MySingelton(Context context) {
        requestQueue = getRequestQueue();
        this.context = context;
    }

    public static synchronized MySingelton getmInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MySingelton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        return requestQueue;
    }

    public void setRequestQueue(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    public <T> void addToQueueRequest(Request<T> request) {
        getRequestQueue().add(request);
    }

}
