package psu.signlinguamobile.api.apiservice;

import java.util.List;

import psu.signlinguamobile.api.apirequest.LoginRequest;
import psu.signlinguamobile.api.apiresponse.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthApiService {
    // The login endpoint: note that your laravel controller expects 'umail' and 'password'
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("logout")
    Call<Void> logout(); // or a suitable response type

    // For example, an endpoint to fetch classrooms (requires token)
//    @GET("classrooms")
//    Call<List<Classroom>> getClassrooms();
}
