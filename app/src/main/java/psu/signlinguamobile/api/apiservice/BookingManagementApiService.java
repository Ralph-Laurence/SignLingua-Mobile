package psu.signlinguamobile.api.apiservice;

import java.util.List;

import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.models.HiringRequestItem;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface BookingManagementApiService
{
    //=================================================
    // <editor-fold desc="SECTION: GET ROUTES">
    //=================================================
    @POST("booking-management/learners/learner/{learnerId}")
    Call<CommonResponse<Void>> addLearner(@Path("learnerId") String learnerId);
    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="SECTION: GET ROUTES">
    //=================================================
    @GET("booking-management/learners/requests")
    Call<CommonResponse<List<HiringRequestItem>>> getHiringRequests();
    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="SECTION: PATCH ROUTES">
    //=================================================
    @PATCH("booking-management/learners/requests/{learnerId}")
    Call<CommonResponse<Void>> confirmHiringRequest(@Path("learnerId") String learnerId);
    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="SECTION: DELETE ROUTES">
    //=================================================
    @DELETE("booking-management/learners/learner/cancel/{learnerId}")
    Call<CommonResponse<Void>> cancelRequest(@Path("learnerId") String learnerId);

    @DELETE("booking-management/learners/learner/{learnerId}")
    Call<CommonResponse<Void>> dropLearner(@Path("learnerId") String learnerId);

    @DELETE("booking-management/learners/requests/{learnerId}")
    Call<CommonResponse<Void>> declineHiringRequest(@Path("learnerId") String learnerId);
    //=================================================
    // </editor-fold>
    //=================================================
}