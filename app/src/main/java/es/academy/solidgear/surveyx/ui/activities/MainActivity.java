package es.academy.solidgear.surveyx.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import es.academy.solidgear.surveyx.R;

/**********************************************
 * Main Activity
 * Entry point of the app.
 * Handles session and shows login page in case
 * the user is not authenticated.
 */

public class MainActivity extends Activity {

    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Try to retrieve token from intent extras (Not a better method implemented yet)
        Bundle extras = getIntent().getExtras();
        String token = null;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        String tokenStore = settings.getString("token", null);

        if (extras != null) {
            if(tokenStore != null)
            token = tokenStore;
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
