package es.academy.solidgear.surveyx.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import es.academy.solidgear.surveyx.R;

public class SocialNetworkActivity extends BaseActivity implements View.OnClickListener {

    private Button mFinishButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_network);
        initToolbar();

        mFinishButton = (Button) findViewById(R.id.finishButton);
        mFinishButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == mFinishButton) {
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
