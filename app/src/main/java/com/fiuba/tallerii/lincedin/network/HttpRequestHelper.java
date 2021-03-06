package com.fiuba.tallerii.lincedin.network;

import android.content.Context;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.fiuba.tallerii.lincedin.utils.SharedPreferencesKeys;
import com.fiuba.tallerii.lincedin.utils.SharedPreferencesUtils;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The class wraps the instantiation of JsonObjectRequest{@link JsonObjectRequest}s and the way that Volley library uses them for sending asynchronous HTTP requests.
 */
public class HttpRequestHelper {

    private static RequestQueue mRequestQueue;
    private static Context context;

    /**
     * Creates RequestQueue{@link RequestQueue} for sending asynchronous HTTP requests with Volley.
     * The methods will use this queue as a Singleton.
     * @param appContext is the application context
     */
    public static void initialize(Context appContext) {
        mRequestQueue = Volley.newRequestQueue(appContext);
        context = appContext;
    }

    /**
     * Adds to the RequestQueue{@link RequestQueue} the request to perform.
     * @param method is the HTTP Method
     * @param url is the url pointed by the request
     * @param requestParams are the key-value parameters added to the url
     * @param jsonRequest is the JSON body of the request
     * @param successListener is the listener that triggers the actions to perform when the response is successful
     * @param errorListener is the listener that triggers the actions to perform when the response fails
     * @param requestTag the tag associated to the request to enqueue
     * @return the enqueued Request{@link Request} or null{@code null} if the RequestQueue couldn't be initialized.
     */
    private static Request<JSONObject> enqueue(int method, String url, @Nullable final Map<String, String> requestParams, JSONObject jsonRequest,
                                               final Response.Listener<JSONObject> successListener, final Response.ErrorListener errorListener, String requestTag) {
        if (mRequestQueue == null)
            return null;

        Response.ErrorListener errorListenerThatHandlesTokenExpiration = extendErrorListenerForHandlingSessionTokenExpiration(successListener, errorListener);

        JsonObjectRequest request = new JsonObjectRequest(method, url, jsonRequest, successListener, errorListenerThatHandlesTokenExpiration) {
            @Override
            protected Map<String, String> getParams() {
                return requestParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", UserAuthenticationManager.getSessionToken(context));
                headers.put("Connection", "close"); // MARTÍN TE AMO COMO NUNCA AMÉ A NADIE!
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    if (response.data.length == 0) {
                        byte[] responseData = "{}".getBytes("UTF8");
                        response = new NetworkResponse(response.statusCode, responseData, response.headers, response.notModified);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return super.parseNetworkResponse(response);
            }
        };
        request.setTag(requestTag);
        request.setRetryPolicy(new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(3), 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return mRequestQueue.add(request);
    }

    private static Response.ErrorListener extendErrorListenerForHandlingSessionTokenExpiration(final Response.Listener<JSONObject> successListener, final Response.ErrorListener errorListener) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == LincedInHttpStatus.UNAUTHORIZED) {
                    if (UserAuthenticationManager.isUserLoggedInWithFacebookAccount(context)) {
                        UserAuthenticationManager.facebookLogIn(
                                context,
                                AccessToken.getCurrentAccessToken().getToken(),
                                successListener,
                                errorListener
                        );
                    } else if (UserAuthenticationManager.isUserLoggedInWithLincedInAccount(context)) {
                        UserAuthenticationManager.lincedInLogIn(
                                context,
                                SharedPreferencesUtils.getStringFromSharedPreferences(context, SharedPreferencesKeys.USER_EMAIL, null),
                                SharedPreferencesUtils.getDecryptedStringFromSecurePreferences(context, SharedPreferencesKeys.USER_PASSWORD, null),
                                successListener,
                                errorListener
                        );
                    }
                } else {
                    errorListener.onErrorResponse(error);
                }
            }
        };
    }

    /**
     * Cancels all the pending requests with the specified tag.
     * @param tag is the tag associated to the requests that must be canceled
     */
    public static void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null)
            mRequestQueue.cancelAll(tag);
    }

    /**
     * Enqueues a POST request.
     * @param url is the url pointed by the request
     * @param requestParams are the key-value parameters added to the url
     * @param jsonRequest is the JSON body of the request
     * @param listener is the listener that triggers the actions to perform when the response is successful
     * @param errorListener is the listener that triggers the actions to perform when the response fails
     * @param requestTag the tag associated to the request to enqueue
     * @return the enqueued Request{@link Request}
     */
    public static Request<JSONObject> post(String url, @Nullable final Map<String, String> requestParams, JSONObject jsonRequest,
                       Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, String requestTag) {
        return enqueue(Method.POST, url, requestParams, jsonRequest, listener, errorListener, requestTag);
    }

    /**
     * Enqueues a PUT request.
     * @param url is the url pointed by the request
     * @param requestParams are the key-value parameters added to the url
     * @param jsonRequest is the JSON body of the request
     * @param listener is the listener that triggers the actions to perform when the response is successful
     * @param errorListener is the listener that triggers the actions to perform when the response fails
     * @param requestTag the tag associated to the request to enqueue
     * @return the enqueued Request{@link Request}
     */
    public static Request<JSONObject> put(String url, @Nullable final Map<String, String> requestParams, JSONObject jsonRequest,
                                          Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, String requestTag) {
        return enqueue(Method.PUT, url, requestParams, jsonRequest, listener, errorListener, requestTag);
    }

    /**
     * Enqueues a GET request.
     * @param url is the url pointed by the request
     * @param requestParams are the key-value parameters added to the url
     * @param listener is the listener that triggers the actions to perform when the response is successful
     * @param errorListener is the listener that triggers the actions to perform when the response fails
     * @param requestTag the tag associated to the request to enqueue
     * @return the enqueued Request{@link Request}
     */
    public static Request<JSONObject> get(String url, @Nullable final Map<String, String> requestParams, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, String requestTag) {
        return enqueue(Method.GET, url, requestParams, null, listener, errorListener, requestTag);
    }

    /**
     * Enqueues a DELETE request.
     * @param url is the url pointed by the request
     * @param requestParams are the key-value parameters added to the url
     * @param listener is the listener that triggers the actions to perform when the response is successful
     * @param errorListener is the listener that triggers the actions to perform when the response fails
     * @param requestTag the tag associated to the request to enqueue
     * @return the enqueued Request{@link Request}
     */
    public static Request<JSONObject> delete(String url, @Nullable final Map<String, String> requestParams, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, String requestTag) {
        return enqueue(Method.DELETE, url, requestParams, null, listener, errorListener, requestTag);
    }
}
