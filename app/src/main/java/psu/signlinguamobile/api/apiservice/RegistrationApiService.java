package psu.signlinguamobile.api.apiservice;

import okhttp3.MultipartBody;
import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.api.apiresponse.LoginResponse;
import psu.signlinguamobile.models.RegistrationDetails;
import psu.signlinguamobile.models.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RegistrationApiService
{
    //=================================================
    // <editor-fold desc="SECTION: POST ROUTES">
    //=================================================

    @POST("signlingua/registration/basic")
    Call<LoginResponse> registerBasic(@Body RegistrationDetails inputs);

    @DELETE("signlingua/registration/tutor")
    Call<CommonResponse<Void>> cancelRegistration();

//    @Multipart
//    @POST("signlingua/registration/tutor")
//    Call<CommonResponse<User>> registerTutor(@PartMap Map<String, RequestBody> fields,
//                                             @Part MultipartBody.Part image);

    @Multipart
    @POST("signlingua/registration/tutor")
    Call<CommonResponse<Void>> verifyAccountByID
    (
        @Part MultipartBody.Part frontId,
        @Part MultipartBody.Part backId
    );
    //=================================================
    // </editor-fold>
    //=================================================
}