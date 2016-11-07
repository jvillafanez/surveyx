package es.academy.solidgear.surveyx.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

import es.academy.solidgear.surveyx.R;
import es.academy.solidgear.surveyx.managers.Utils;
import es.academy.solidgear.surveyx.model.OptionModel;
import es.academy.solidgear.surveyx.model.QuestionModel;
import es.academy.solidgear.surveyx.services.requests.GetQuestionRequest;
import es.academy.solidgear.surveyx.ui.activities.SurveyActivity;
import es.academy.solidgear.surveyx.ui.views.AnswerRadioButton;

public class SurveyFragment extends Fragment implements RadioGroup.OnCheckedChangeListener{
    private static final int UNCHECKED_VALUE = -1;

    private ViewGroup.LayoutParams PADDING_LAYOUT_PARAMS;
    private RadioGroup mAnswersOutlet;
    private TextView mQuestionTextView;

    private ArrayList<Integer> mResponseSelected;
    private int[] mQuestionsId;
    private QuestionModel[] mQuestions;

    private int mLastRadioButtonId = 0;
    private int mIteration = 0;
    private boolean mIsLastQuestion = false;

    private RequestQueue mRequestQueue;

    private SurveyFragment mFragment = this;
    private SurveyActivity mActivity;

    Response.Listener<QuestionModel> mListener = new Response.Listener<QuestionModel>() {
        @Override
        public void onResponse(QuestionModel question) {
            mFragment.showQuestion(question);
        }
    };

    Response.ErrorListener mErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    };

    private void getQuestion(int questionId) {
        // Retrieve question info from the server
        mRequestQueue = Volley.newRequestQueue(this.getActivity());
        GetQuestionRequest questionRequest = new GetQuestionRequest(questionId, mListener, mErrorListener);
        mRequestQueue.add(questionRequest);
    }

    public static SurveyFragment newInstance() {
        SurveyFragment fragment =  new SurveyFragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = (SurveyActivity) getActivity();

        // Generate layout params with padding
        int paddingInPixels = (int) Utils.dimenToPixels(getActivity(), TypedValue.COMPLEX_UNIT_DIP, 20);
        PADDING_LAYOUT_PARAMS = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, paddingInPixels);

        // Get questions info from parent activity
        mQuestionsId = ((SurveyActivity)getActivity()).getQuestions();
        mQuestions = new QuestionModel[mQuestionsId.length];

        mResponseSelected = new ArrayList<Integer>();

        // show first question
        getQuestion(mQuestionsId[0]);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_survey, null);

        // Reference UI Items
        mQuestionTextView = (TextView) root.findViewById(R.id.question_text);

        mAnswersOutlet = (RadioGroup) root.findViewById(R.id.answers_outlet);
        mAnswersOutlet.setOnCheckedChangeListener(this);

        return root;
    }

    public ArrayList<Integer> getResponseSelected() {
        return mResponseSelected;
    }

    public ArrayList<Integer> getResponses() {
        return mResponseSelected;
    }

    public void showNextQuestion() {
        // Check is not last question
        if (mIteration >= mQuestionsId.length - 1) {
            return;
        }

        // Increment counter and get new Question data
        mIteration++;
        mIsLastQuestion = mIteration == (mQuestionsId.length - 1);

        // function randomize questions
        ArrayList<Integer> orderedQuestion=new ArrayList<Integer>();
        ArrayList<Integer> unorderedQuestion=new ArrayList<Integer>();

        for(int i=0;i<mQuestionsId.length;i++){
            orderedQuestion.add(mQuestionsId[i]);
        }

        while(orderedQuestion.size()>1){
            unorderedQuestion.add(orderedQuestion.remove((int)(Math.random()*orderedQuestion.size())));
        }
        getQuestion(unorderedQuestion.get(mIteration));
        // end function randomize question
    }

    private void showQuestion(QuestionModel currentQuestion) {

        // Set new id for radio button
        if ( mIteration > 0 && mQuestions[mIteration-1] != null ) {
            mLastRadioButtonId = mLastRadioButtonId + mQuestions[mIteration-1].getChoices().size();
        }

        // Clear previous answer
        mAnswersOutlet.clearCheck();
        mAnswersOutlet.removeAllViews();

        mQuestions[mIteration] = currentQuestion;
        mQuestionTextView.setText(currentQuestion.getText());

        List<OptionModel> orderAnswer = currentQuestion.getChoices();
        List<OptionModel> randomAnswer = new ArrayList<>();

        while(orderAnswer.size()>0){
            randomAnswer.add(orderAnswer.remove((int) (Math.random()*orderAnswer.size())));
        }

        for (OptionModel option : randomAnswer) {
            // Create radio button with answer
            AnswerRadioButton radioButton = new AnswerRadioButton(getActivity(), option.getText());
            radioButton.setTag(option.getId());
            mAnswersOutlet.addView(radioButton);

            // Add padding for each answer
            // There is a bug in API 16 and below that with padding method of RadioButton
            View paddingView = new View(getActivity());
            paddingView.setLayoutParams(PADDING_LAYOUT_PARAMS);
            mAnswersOutlet.addView(paddingView);
        }
    }

    public int getCurrentQuestion() {
        return mIteration;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        boolean enabled = checkedId != UNCHECKED_VALUE;

        if (enabled) {
            mResponseSelected.clear();
            View radioButton = group.findViewById(group.getCheckedRadioButtonId());
            mResponseSelected.add((int)radioButton.getTag());
        }

        mActivity.enableButton(enabled);
        mActivity.setLabel(mIsLastQuestion);
    }
}
