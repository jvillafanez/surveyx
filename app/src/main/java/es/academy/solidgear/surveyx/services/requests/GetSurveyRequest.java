package es.academy.solidgear.surveyx.services.requests;

import com.android.volley.Response;

import es.academy.solidgear.surveyx.model.SurveyModel;
import es.academy.solidgear.surveyx.services.requestparams.LoginRequestParams;

public class GetSurveyRequest extends BaseJSONRequest<LoginRequestParams, SurveyModel> {
    public GetSurveyRequest(int id, Response.Listener<SurveyModel> listener,
                            Response.ErrorListener errorListener) {
        super(Method.GET, "surveys/" + String.valueOf(id), SurveyModel.class, null,
                null, listener, errorListener);
    }
}
