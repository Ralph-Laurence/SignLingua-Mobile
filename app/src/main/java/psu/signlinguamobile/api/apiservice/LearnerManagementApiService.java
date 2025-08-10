package psu.signlinguamobile.api.apiservice;

import psu.signlinguamobile.api.apiresponse.LearnerClassroomsResponse;
import psu.signlinguamobile.api.apiresponse.LearnerProfileDetailsResponse;
import psu.signlinguamobile.api.apiresponse.PaginatedResponse;
import psu.signlinguamobile.api.apiresponse.LearnerDetailsResponse;
import psu.signlinguamobile.models.LearnerItem;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LearnerManagementApiService
{
    @GET("learner-management/learners/filter-results")
    Call<PaginatedResponse<LearnerItem>> findLearners(
            @Query("page") int page,
            @Query("search") String keyword,
            @Query("disability") String disability,
            @Query("include") String include // 'all' -> fetch All learners | 'friend' -> only those booked with | 'new' -> those who arent booked with
    );

    @GET("learner-management/learners/classrooms")
    Call<LearnerClassroomsResponse> fetchClassrooms();

    @GET("user-profile-management/learner/profile")
    Call<LearnerProfileDetailsResponse> fetchLearnerProfileDetails();

    @GET("learner-management/learners/learner")
    Call<LearnerDetailsResponse> showLearner(
            @Query("id") String hashedId
    );
}