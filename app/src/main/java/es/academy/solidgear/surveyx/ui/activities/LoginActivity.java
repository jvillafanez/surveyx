package es.academy.solidgear.surveyx.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

/**
 * Created by Siro on 10/12/2014.
 */
public class LoginActivity extends BaseActivity implements ErrorDialogFragment.OnClickClose, View.OnClickListener {
    private static final String AUTH_ERROR = "com.android.volley.AuthFailureError";

    private ProgressBar mProgressBar;
    private Button mLoginButton;
    private EditText mUsername;
    private EditText mPassword;

    private LoginActivity mActivity = this;
    private Context mContext = this;

    private Response.ErrorListener mLoginErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (error.toString().equals(AUTH_ERROR)) {
                Toast.makeText(mContext, getString(R.string.incorrect_login), Toast.LENGTH_LONG).show();
            } else {
                ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(error.toString());
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                errorDialog.show(fragmentManager, "dialog");
            }

            mLoginButton.setEnabled(true);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    };

    private Response.Listener<LoginModel> mLoginListener = new Response.Listener<LoginModel>() {
        @Override
        public void onResponse(LoginModel response) {

            mActivity.getApplication();
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra("token", response.getToken());
            startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBarLogin);
        mUsername = (EditText) findViewById(R.id.userLoginText);
        mPassword = (EditText) findViewById(R.id.passLoginText);
        mLoginButton = (Button) findViewById(R.id.login_button);
        TextView sglogintext = (TextView) findViewById(R.id.sgLoginText);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/KGSecondChancesSketch.ttf");
        sglogintext.setTypeface(tf);
        mLoginButton.setTypeface(tf);

        Typeface tfmuseum = Typeface.createFromAsset(getAssets(), "fonts/Museo300-Regular.otf");
        mUsername.setTypeface(tfmuseum);
        mPassword.setTypeface(tfmuseum);

        mLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == mLoginButton) {
            mLoginButton.setEnabled(false);
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
