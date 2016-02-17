package es.academy.solidgear.surveyx.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import es.academy.solidgear.surveyx.R;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();
        String token = null;
        if (extras != null) {
            token = extras.getString("token", null); /** TODO: Read token when it will be stored **/
        }

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
