package es.academy.solidgear.surveyx.services.requests;

import com.android.volley.Response;

import es.academy.solidgear.surveyx.model.LoginModel;
import es.academy.solidgear.surveyx.services.requestparams.LoginRequestParams;


import static es.academy.solidgear.surveyx.utils.RequestUtils.encode;

/**
 * Created by Siro on 10/12/2014.
 */
public class UserLoginRequest extends BaseJSONRequest<LoginRequestParams, LoginModel>  {
    public UserLoginRequest(String username, String password, Response.Listener<LoginModel> listener,
                            Response.ErrorListener errorListener) {

        super(Method.GET, "users/?username=" + encode(username) + "&password=" + encode(password), LoginModel.class, null,
                null, listener, errorListener);
    }


}
