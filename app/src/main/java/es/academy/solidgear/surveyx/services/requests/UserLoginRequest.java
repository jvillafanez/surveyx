package es.academy.solidgear.surveyx.services.requests;

import com.android.volley.Response;

import es.academy.solidgear.surveyx.model.LoginModel;
import es.academy.solidgear.surveyx.services.requestparams.LoginRequestParams;

/**
 * Created by Siro on 10/12/2014.
 */
public class UserLoginRequest extends BaseJSONRequest<LoginRequestParams, LoginModel> {
    public UserLoginRequest(String username, String password, Response.Listener<LoginModel> listener,
                            Response.ErrorListener errorListener) {
        super(Method.GET, "users/?username=" + username + "&password=" + password, LoginModel.class, null,
                null, listener, errorListener);
    }
}
