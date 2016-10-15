package com.fiuba.tallerii.lincedin.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.fiuba.tallerii.lincedin.R;
import com.fiuba.tallerii.lincedin.utils.SharedPreferencesKeys;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private static CallbackManager callbackManager = CallbackManager.Factory.create();

    private AccessTokenTracker accessTokenTracker;
    private AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setFacebookAccessTokenTracker();
        setFacebookLoginButtonListener();
    }

    private void setFacebookLoginButtonListener() {
        LoginButton fbLoginButton = (LoginButton) findViewById(R.id.login_button);
        if (fbLoginButton == null) {
            return;
        }

        fbLoginButton.setReadPermissions("email", "public_profile", "user_location", "user_birthday");
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Facebook login callback success.");
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject json, GraphResponse response) {
                                if (response.getError() != null) {
                                    Log.d(TAG, "Facebook profile Graph request error!");
                                } else {
                                    Log.d(TAG, "Facebook profile Graph request success.");
                                    String jsonResult = new Gson().toJson(json);
                                    Log.d(TAG, jsonResult);
                                    saveMyProfile(jsonResult);
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, first_name, last_name, email, gender, birthday, link, location");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Facebook login callback cancelled.");
                // TODO: 15/10/16 Decide what to do here.
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d(TAG, "Facebook login callback error!");
                exception.printStackTrace();
                // TODO: 15/10/16 Decide what to do here.
            }
        });
    }

    private void setFacebookAccessTokenTracker() {
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();
        Log.d(TAG, "Facebook AccessToken: " + (accessToken != null ? accessToken.getToken() : "null"));

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                Log.i(TAG, "Facebook access token changed.");
                accessToken = currentAccessToken;
                Log.d(TAG, "New Facebook AccessToken: " + (currentAccessToken != null ? currentAccessToken.getToken() : "null"));
            }
        };
    }

    private void saveMyProfile(String jsonMyProfile) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(SharedPreferencesKeys.FACEBOOK_MY_PROFILE, jsonMyProfile);
        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}