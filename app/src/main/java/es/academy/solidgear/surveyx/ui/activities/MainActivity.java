package es.academy.solidgear.surveyx.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import es.academy.solidgear.surveyx.R;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import io.fabric.sdk.android.Fabric;



/**********************************************
 * Main Activity
 * Entry point of the app.
 * Handles session and shows login page in case
 * the user is not authenticated.
 */

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers());
        setContentView(R.layout.activity_main);

        /*Answers.getInstance().logLogin(new LoginEvent()
                .putMethod("Digits")
                .putSuccess(true));*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Try to retrieve token from intent extras (Not a better method implemented yet)
        Bundle extras = getIntent().getExtras();
        String token = null;
        if (extras != null) {
            token = extras.getString("token", null); /** TODO: Read token when it will be stored **/
        }

        // If there's no token, redirect to login. If there is a token, redirect to the survey list
        if (token == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, SurveyListActivity.class);
            intent.putExtra("token", token);
            startActivity(intent);
        }

        finish();
    }

}
