package es.academy.solidgear.surveyx.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import es.academy.solidgear.surveyx.R;
import es.academy.solidgear.surveyx.model.GeofenceModel;
import es.academy.solidgear.surveyx.model.SurveyModel;
import es.academy.solidgear.surveyx.model.SurveysModel;
import es.academy.solidgear.surveyx.services.ReceiveTransitionsIntentService;
import es.academy.solidgear.surveyx.services.requests.GetAllSurveysRequest;
import es.academy.solidgear.surveyx.ui.adapter.SurveyListAdapter;
import es.academy.solidgear.surveyx.ui.fragments.ErrorDialogFragment;
import es.academy.solidgear.surveyx.ui.fragments.InformationDialogFragment;

/**********************************************
 * SurveyList Activity
 * Handle main view with the list of surveys
 */
public class SurveyListActivity extends BaseActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        SurveyListAdapter.OnClickSurvey, ErrorDialogFragment.OnClickClose {
    private static final String TAG = SurveyListActivity.class.getCanonicalName();
    private static final int MIN_DISTANCE_TO_LOCATED_SURVEY_METERS = 100;

    private RecyclerView mQuestionnaireList;
    private InformationDialogFragment mDialog;
    private AlertDialog mGpsEnabledDialog;

    private RequestQueue mRequestQueue;
    private SurveysModel mRequestResponse = null;

    private GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation = null;
    private List<Geofence> mCurrentGeofences = new ArrayList<Geofence>();

    private Boolean mGpsEnabled;
    private Boolean mAskedEnableGps = false;

    private boolean mSurveyListAlreadyShown = false;

    private Activity mActivity;

    private String mToken;

    Response.Listener<SurveysModel> mQuestionnaireListResponse = new Response.Listener<SurveysModel>() {
        @Override
        public void onResponse(SurveysModel response) {
            // Set new data and show them
            mRequestResponse = response;
            handleResponse();
        }
    };

    Response.ErrorListener mErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            // Handle error response from the server. Show error dialog
            mDialog.dismiss();
            ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(error.toString());
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            errorDialog.show(fragmentManager, "dialog");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_list);
        initToolbar();

        mActivity = this;

        buildGoogleApiClient();

        // Retrieve token from extras and store it for future requests
        Bundle extras = getIntent().getExtras();
        mToken = extras.getString("token", null);

        // Retrieve view from the UI where we are going to put the adapter with the list
        // of surveys
        mQuestionnaireList = (RecyclerView) findViewById(R.id.questionnaireList);

        mQuestionnaireList.setLayoutManager(new LinearLayoutManager(this));
    }

    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();

        // First of all, check gps status and store it
        mGpsEnabled = checkGpsEnabled();

        // Check if the user has been asked to enable his gps
        SharedPreferences sharedPref = this.getPreferences(this.MODE_PRIVATE);
        mAskedEnableGps = sharedPref.getBoolean("AskedEnableGps", false);

        // If not, and the gps is not enable, ask him to enable it. Only 1 time.
        if (!mGpsEnabled && !mAskedEnableGps) {
            showEnableGpsDialog();
        } else {
            // Show dialog of retrieving surveys
            if (mDialog == null) {
                mDialog = InformationDialogFragment.newInstance(R.string.dialog_getting_surveys);
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                mDialog.show(fragmentManager, "dialog");
            } else {
                mDialog.onResume();
            }

            request();
        }
    }

    protected void onPause() {
        mRequestResponse = null;
        mSurveyListAlreadyShown = false;
        super.onPause();
    }

    private void request() {
        mRequestQueue = Volley.newRequestQueue(this);
        GetAllSurveysRequest getallSurveysRequest = new GetAllSurveysRequest(mToken, mQuestionnaireListResponse, mErrorListener);
        mRequestQueue.add(getallSurveysRequest);
    }

    private void handleResponse() {
        // Show updated data retrieved from server
        if (mRequestResponse != null && mLastLocation != null && !mSurveyListAlreadyShown) {
            // If we have location, register the geofences of the surveys and show the surveys' data
            registerGeofences(getGeofences(mRequestResponse.getSurveys()));
            showSurveys(mRequestResponse.getSurveys());
            mSurveyListAlreadyShown = true;
            return;
        }

        if (mRequestResponse == null) {
            mDialog.setMessage(mActivity.getString(R.string.dialog_waiting_data));
        }

        if (mLastLocation == null) {
            // If gps is enabled and we don't have location, wait for it. If not, show the surveys
            // without geofencing data
            if (mGpsEnabled) {
                mDialog.setMessage(mActivity.getString(R.string.dialog_waiting_location));
            } else {
                showSurveys(mRequestResponse.getSurveys());
            }
        }
    }

    private List<GeofenceModel> getGeofences(List<SurveyModel> surveys) {
        // Return geofences info of the surveys so we can call register geofences with it
        List<GeofenceModel> geofenceModelList = new ArrayList<GeofenceModel>();

        for(SurveyModel survey: surveys) {
            GeofenceModel geofenceModel = new GeofenceModel(String.valueOf(survey.getId()),
                                                            survey.getLatitude(),
                                                            survey.getLongitude(),
                                                            GeofenceModel.RADIUS_BY_DEFAULT,
                                                            GeofenceModel.EXPIRATION_DURATION_BY_DEFAULT,
                                                            Geofence.GEOFENCE_TRANSITION_ENTER);

            if (!survey.getAlreadyDone()) { // && survey.getDistanceToCurrentPosition() > MIN_DISTANCE_TO_LOCATED_SURVEY_METERS) {
                geofenceModelList.add(geofenceModel);
            }
        }

        return  geofenceModelList;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void showSurveys(List<SurveyModel> surveyModelList) {
        mDialog.dismiss();

        // Loop over list of surveys and remove those already done
        // and the ones with gps info if we don't have gps enable
        for (int i=0; i<surveyModelList.size(); i++) {
            SurveyModel currentSurvey = surveyModelList.get(i);
            if (currentSurvey.getAlreadyDone()) {
            /* Remove done survey */
                surveyModelList.remove(i);
                i--;
                continue;
            }
            if (!mGpsEnabled) {
            /* Remove located survey */
                if (surveyModelList.get(i).hasCoordinates()){
                    surveyModelList.remove(i);
                    i--;
                }
            }
        }

        // Set adapter with the filtered survey list data
        mQuestionnaireList.setAdapter(new SurveyListAdapter(surveyModelList, this));

        // Add cool animation
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mQuestionnaireList.startAnimation(fadeInAnimation);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mQuestionnaireList.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            // In debug mode, log the status
            Log.d(TAG, "Play services available");

            // Continue
            return true;

            // Google Play services was not available for some reason
        } else {

            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorFragment errorFragment = new ErrorFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), TAG);
            }
            return false;
        }
    }

    private void registerGeofences(List<GeofenceModel> geofences) {
        // Google play services are needed
        if (!servicesConnected() || geofences.size() == 0) {
            return;
        }

        // Generate geofences from geofence models to pass them to the builder
        for (GeofenceModel geofence: geofences) {
            mCurrentGeofences.add(geofence.toGeofence());
        }

        // Create geofencing builder with the given geofences
        GeofencingRequest.Builder geofencingRequestBuilder = new GeofencingRequest.Builder();
        geofencingRequestBuilder.addGeofences(mCurrentGeofences);

        PendingIntent pendingIntent = PendingIntent.getService(this, 0,
                new Intent(this, ReceiveTransitionsIntentService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Removes all geofences associated with the given pendingIntent.
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, pendingIntent);

        // Pass geofencing builder to the API
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, geofencingRequestBuilder.build(), pendingIntent);
    }

    public boolean checkGpsEnabled() {
        LocationManager mlocManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        return mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showEnableGpsDialog() {
        final SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        // If dialog does not exist, create it
        if (mGpsEnabledDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_gps_title);
            builder.setMessage(R.string.dialog_gps_message);
            builder.setPositiveButton(R.string.dialog_gps_settings, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    editor.putBoolean("AskedEnableGps", true);
                    editor.commit();
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.dialog_gps_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    editor.putBoolean("AskedEnableGps", true);
                    editor.commit();
                    enableGpsRejected();
                    dialog.dismiss();
                }
            });
            mGpsEnabledDialog = builder.create();
        }
        mGpsEnabledDialog.show();
    }

    private void enableGpsRejected() {
        this.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            //mAuthManager.setAuthCredentials("", null);
            final SharedPreferences.Editor editor = mActivity.getPreferences(mActivity.MODE_PRIVATE).edit();
            editor.remove("AskedEnableGps");
            editor.remove("token");
            editor.commit();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.action_refresh) {
            refresh();
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        mSurveyListAlreadyShown = false;
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        mDialog.show(fragmentManager, "dialog");
        request();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mGpsEnabled) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                Log.d(TAG, String.valueOf(mLastLocation.getLatitude()) + ", " + String.valueOf(mLastLocation.getLongitude()));
            }
            handleResponse();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed");
        mDialog.dismiss();
    }

    @Override
    public void onClickSurvey(SurveyModel survey) {
        // Show activity with details of the survey
        Intent intent = new Intent(this, SurveyActivity.class);
        intent.putExtra("token", mToken);
        intent.putExtra(SurveyActivity.SURVEY_ID, survey.getId());
        startActivity(intent);
    }

    @Override
    public void onClickClose() {
    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
