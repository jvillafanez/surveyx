package es.academy.solidgear.surveyx.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;

import es.academy.solidgear.surveyx.R;
import es.academy.solidgear.surveyx.managers.Utils;
import es.academy.solidgear.surveyx.model.SurveyModel;
import es.academy.solidgear.surveyx.services.requestparams.SurveyPostParams;
import es.academy.solidgear.surveyx.services.requests.GetSurveyRequest;
import es.academy.solidgear.surveyx.services.requests.SendResponseRequest;
import es.academy.solidgear.surveyx.ui.fragments.ErrorDialogFragment;
import es.academy.solidgear.surveyx.ui.fragments.InformationDialogFragment;
import es.academy.solidgear.surveyx.ui.fragments.SurveyFragment;
import es.academy.solidgear.surveyx.ui.fragments.YesNoDialogFragment;
import es.academy.solidgear.surveyx.ui.views.CustomButton;

public class SurveyActivity extends BaseActivity implements ErrorDialogFragment.OnClickClose, View.OnClickListener,
        YesNoDialogFragment.OnClick {
    private static final String TAG = "SurveyActivity";
    public static final String SURVEY_ID = "surveyId";

    private CustomButton mButtonNext;
    private CustomButton mButtonCancel;
    private TextView mTextViewCurrentQuestion;
    private TextView mTextViewTotal;

    private SurveyFragment mSurveyFragment;
    private InformationDialogFragment mDialog;

    private int[] mQuestions;
    private RequestQueue mRequestQueue;
    private int mSurveyId;

    private Activity mActivity;
    private String mToken;

    Response.Listener<SurveyModel> mListener = new Response.Listener<SurveyModel>() {
        @Override
        public void onResponse(SurveyModel response) {
            // Handle response of the server with data of the survey
            mQuestions = response.getQuestions();
            // Show number of questions
            mTextViewTotal.setText(String.valueOf(mQuestions.length));
            // Start counter of questions
            updateCounter();
            mDialog.dismiss();
            // Show question data in its fragment
            Utils.showFragment(mActivity, mSurveyFragment, R.id.container);
        }
    };

    Response.ErrorListener mErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            // Handle error response of the server
            mDialog.dismiss();
            // Show error dialog
            ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(error.toString());
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            errorDialog.show(fragmentManager, "dialog");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        initToolbar();

        // Get auth token for further requests
        Bundle extras = getIntent().getExtras();
        mToken = extras.getString("token", null);

        // Get references to UI items
        mButtonNext = (CustomButton) findViewById(R.id.buttonNext);
        mButtonNext.setOnClickListener(this);
        mButtonNext.setEnabled(false);
        mButtonCancel = (CustomButton) findViewById(R.id.buttonCancel);
        mButtonCancel.setOnClickListener(this);
        mTextViewCurrentQuestion = (TextView) findViewById(R.id.textViewCurrentQuestion);
        mTextViewTotal = (TextView) findViewById(R.id.textViewTotal);

        // Init fragment for survey info
        mSurveyFragment = SurveyFragment.newInstance();

        // Show loading survey dialog
        mDialog = InformationDialogFragment.newInstance(R.string.dialog_getting_survey);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mDialog.show(fragmentManager, "dialog");

        mActivity = this;

        // Retrieve survey data
        mSurveyId = getIntent().getExtras().getInt(SURVEY_ID);
        getSurvey(mSurveyId);
    }

    private void getSurvey(int surveyId) {
        // Call server web service to retrieve survey data
        mRequestQueue = Volley.newRequestQueue(this);
        GetSurveyRequest surveyRequest = new GetSurveyRequest(surveyId, mListener, mErrorListener);
        mRequestQueue.add(surveyRequest);
    }

    public int[] getQuestions() {
        return mQuestions;
    }

    @Override
    public void onBackPressed() {
        performCancel();
    }

    private void performCancel() {
        // Show confirmation dialog for cancel survey
        YesNoDialogFragment yesNoDialogFragment = YesNoDialogFragment.newInstance(getString(R.string.dialog_finish_survey));
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        yesNoDialogFragment.show(fragmentManager, "dialog");
    }

    public void enableButton(boolean enabled)
    {
        if(enabled == false)
        {
            mButtonNext.setEnabled(enabled);
        }
    }

    public void setLabel(boolean isLast) {
        // In the last question show 'Submit' text instead of 'Next'
        if (isLast) {
            mButtonNext.setText(R.string.global_submit);
            mButtonNext.setContentDescription(getString(R.string.descriptor_survey_submit_button));
        } else {
            mButtonNext.setText(R.string.global_next);
            mButtonNext.setContentDescription(getString(R.string.descriptor_survey_next_button));
        }
    }

    private void performNext() {
        sendResponse();
    }

    private void responseSent() {
        // If last question, show the social networks activity. If not, show next question.
        if (mSurveyFragment.getCurrentQuestion() >= mQuestions.length-1) {
            Intent intent = new Intent(SurveyActivity.this,SocialNetworkActivity.class);
            startActivity(intent);
            finish();
        } else {
            mSurveyFragment.showNextQuestion();
            updateCounter();
        }
    }

    private void updateCounter() {
        // Increase counter of questions and show it
        int currentQuestion = mSurveyFragment.getCurrentQuestion();
        mTextViewCurrentQuestion.setText(String.valueOf(++currentQuestion));
    }

    private void sendResponse() {
        // Set params for the call to the post survey answer web service
        int questionId = mQuestions[mSurveyFragment.getCurrentQuestion()];
        SurveyPostParams surveyPostParams = new SurveyPostParams();
        surveyPostParams.setToken(mToken);
        surveyPostParams.setSurvey(mSurveyId);
        surveyPostParams.setQuestion(questionId);
        ArrayList<Integer> responseSelected = mSurveyFragment.getResponseSelected();
        surveyPostParams.setChoice(responseSelected);

        // Listener for the post survey answer request
        Response.Listener<JSONObject> mListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            if (response != null) {
                Toast.makeText(mActivity, "Response sent successfully", Toast.LENGTH_SHORT).show();
                responseSent();
            } else {
                Toast.makeText(mActivity, "Response NOT sent successfully", Toast.LENGTH_SHORT).show();
            }
            }
        };

        Response.ErrorListener mErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mActivity, error.toString(), Toast.LENGTH_LONG).show();
            }
        };

        // Call request
        SendResponseRequest sendResponseRequest = new SendResponseRequest(questionId, surveyPostParams, mListener, mErrorListener);
        mRequestQueue.add(sendResponseRequest);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
    }

    @Override
    public void onClickClose() {
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonNext:
                performNext();
                break;
            case R.id.buttonCancel:
                performCancel();
                break;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        performCancel();
        return true;
    }

    @Override
    public void onClickYes() {
        finish();
    }

    @Override
    public void onClickNo() {

    }

}
