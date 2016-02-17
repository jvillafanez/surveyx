package es.academy.solidgear.surveyx.services.requests;

import com.android.volley.Response;

import es.academy.solidgear.surveyx.model.QuestionModel;
import es.academy.solidgear.surveyx.services.requestparams.LoginRequestParams;

/**
 * Created by Siro on 03/02/2015.
 */
public class GetQuestionRequest extends BaseJSONRequest<LoginRequestParams, QuestionModel> {
    public GetQuestionRequest(int id, Response.Listener<QuestionModel> listener,
                              Response.ErrorListener errorListener) {
        super(Method.GET, "questions/" + String.valueOf(id), QuestionModel.class, null,
                null, listener, errorListener);
    }
}
