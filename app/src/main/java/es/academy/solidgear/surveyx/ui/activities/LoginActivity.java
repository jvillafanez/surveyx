package es.academy.solidgear.surveyx.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import es.academy.solidgear.surveyx.R;
import es.academy.solidgear.surveyx.model.LoginModel;
import es.academy.solidgear.surveyx.services.requests.UserLoginRequest;
import es.academy.solidgear.surveyx.ui.fragments.ErrorDialogFragment;

/**********************************************
 * Login Activity
 * Handle login functionality.
 */
public class LoginActivity extends BaseActivity implements ErrorDialogFragment.OnClickClose, View.OnClickListener {
    private static final String AUTH_ERROR = "com.android.volley.AuthFailureError";

    private ProgressBar mProgressBar;
    private Button mLoginButton;
    private EditText mUsername;
    private EditText mPassword;
    private CheckBox mCheckBox;

    private LoginActivity mActivity = this;
    private Context mContext = this;

    private Response.ErrorListener mLoginErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            // Handle error response from the server
            if (error.toString().equals(AUTH_ERROR)) {
                // Show authentication error
                Toast.makeText(mContext, getString(R.string.incorrect_login), Toast.LENGTH_LONG).show();
            } else {
                // Show other kind of errors
                ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(error.toString());
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                errorDialog.show(fragmentManager, "dialog");
            }

            // Re-activate login button
            mLoginButton.setEnabled(true);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    };

    private Response.Listener<LoginModel> mLoginListener = new Response.Listener<LoginModel>() {
        @Override
        public void onResponse(LoginModel response) {
            // Handle correct auth response
            // Launch MainActivity again with the retrieved token
            mActivity.getApplication();
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra("token", response.getToken());

            if(mCheckBox.isChecked()) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("token", response.getToken());
                editor.commit();
            }

            startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Don't show action bar on login
        getSupportActionBar().hide();

        // Retrieve login form elements from the UI
        mProgressBar = (ProgressBar) findViewById(R.id.progressBarLogin);
        mUsername = (EditText) findViewById(R.id.userLoginText);
        mPassword = (EditText) findViewById(R.id.passLoginText);
        mLoginButton = (Button) findViewById(R.id.login_button);
        TextView sglogintext = (TextView) findViewById(R.id.sgLoginText);
        mCheckBox = (CheckBox) findViewById(R.id.checkBox);

        // Set typefaces to the text
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/KGSecondChancesSketch.ttf");
        sglogintext.setTypeface(tf);
        mLoginButton.setTypeface(tf);

        Typeface tfmuseum = Typeface.createFromAsset(getAssets(), "fonts/Museo300-Regular.otf");
        mUsername.setTypeface(tfmuseum);
        mPassword.setTypeface(tfmuseum);

        // Add listener to the login button
        mLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // Check that the clicked element is the login button
        if (view == mLoginButton) {
            // Disable the button temporarily to avoid double missclick
            mLoginButton.setEnabled(false);
            // Create login request against the server with the login data
            UserLoginRequest request = new UserLoginRequest(mUsername.getText().toString(),
                    mPassword.getText().toString(), mLoginListener, mLoginErrorListener);

            RequestQueue mRequestQueue = Volley.newRequestQueue(this);
            mRequestQueue.add(request);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClickClose() {
    }
}
