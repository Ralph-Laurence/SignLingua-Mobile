package psu.signlinguamobile.api.apiservice;

import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.models.ChatConvResponse;
import psu.signlinguamobile.models.ContactResponse;
import psu.signlinguamobile.models.RenewAuthTokenResponse;
import psu.signlinguamobile.models.SendChatMessageData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatApiService
{
    //=================================================
    // <editor-fold desc="SECTION: GET ROUTES">
    //=================================================
    @GET("slsocial/v1/contacts")
    Call<CommonResponse<ContactResponse>> getContacts();

    @GET("slsocial/v1/chat/{id}")
    Call<CommonResponse<ChatConvResponse>> loadConvo(@Path("id") String contactId);
    //=================================================
    // </editor-fold desc="SECTION: GET ROUTES">
    //=================================================

    //=================================================
    // <editor-fold desc="SECTION: POST ROUTES">
    //=================================================

    @POST("slsocial/v1/chat")
    Call<CommonResponse<Void>> sendMessage(@Body SendChatMessageData input);

    @GET("slsocial/v1/signalr/token")
    Call<RenewAuthTokenResponse> renewAuthToken();
    //=================================================
    // </editor-fold desc="SECTION: GET ROUTES">
    //=================================================
}
