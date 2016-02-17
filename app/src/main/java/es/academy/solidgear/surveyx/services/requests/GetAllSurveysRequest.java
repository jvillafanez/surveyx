package es.academy.solidgear.surveyx.services.requests;

import com.android.volley.Response;

import es.academy.solidgear.surveyx.model.SurveysModel;
import es.academy.solidgear.surveyx.services.requestparams.AllSurveysParams;

/**
 * Created by idiaz on 11/12/2014.
 */
public class GetAllSurveysRequest extends BaseJSONRequest<AllSurveysParams, SurveysModel> {
    public GetAllSurveysRequest(String token,
                                Response.Listener<SurveysModel> listener,
                                Response.ErrorListener errorListener) {
        super(Method.GET, "surveys/?token=" + token, SurveysModel.class, null,
                null, listener, errorListener);
    }
}
