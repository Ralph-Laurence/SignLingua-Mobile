package psu.signlinguamobile.api.apiservice;

import java.util.List;

import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.models.ChatConvResponse;
import psu.signlinguamobile.models.ContactItem;
import psu.signlinguamobile.models.ContactResponse;
import retrofit2.Call;
import retrofit2.http.GET;
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
}
