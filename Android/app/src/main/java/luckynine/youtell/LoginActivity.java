package luckynine.youtell;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import luckynine.youtell.data.UserStatus;


public class LoginActivity extends AppCompatActivity {

    public static CallbackManager callbackManager;
    private ProfileTracker profileTracker;
    private AccessTokenTracker accessTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();
        TextView skipTextView = (TextView) findViewById(R.id.skip_textview);
        skipTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishLogin(false);
            }
        });

        LoginButton facebook_login_button = (LoginButton) findViewById(R.id.facebook_login_button);
        facebook_login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                profileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                        profileTracker.stopTracking();
                        Profile profile = Profile.getCurrentProfile();

                        Context context = getApplicationContext();
                        UserStatus.setUserLoggedInStatus(context, true);
                        UserStatus.setUserId(context, profile.getId());
                        UserStatus.setFirstName(context, profile.getFirstName());
                        UserStatus.setLastName(context, profile.getLastName());
                        UserStatus.setProfilePictureUri(context, profile.getProfilePictureUri(50, 50));
                        UserStatus.setAccessToken(context, AccessToken.getCurrentAccessToken().getToken());
                    }
                };
                profileTracker.startTracking();

                accessTokenTracker = new AccessTokenTracker() {
                    @Override
                    protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                        accessTokenTracker.stopTracking();

                        Context context = getApplicationContext();
                        if (currentAccessToken == null) {
                            UserStatus.clearUserInfo(context);
                            Toast.makeText(getApplicationContext(), "You have logged out.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        UserStatus.setAccessToken(context, currentAccessToken.getToken());
                    }
                };
                accessTokenTracker.startTracking();

                finishLogin(true);
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Login attempt cancelled.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Login attempt failed.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(AccessToken.getCurrentAccessToken()!=null)
            finishLogin(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void finishLogin(boolean loginSuccess){
        if(getIntent().getAction() != null)
            startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
        if(loginSuccess) setResult(RESULT_OK);
        else setResult(RESULT_CANCELED);
        finish();
    }
}
