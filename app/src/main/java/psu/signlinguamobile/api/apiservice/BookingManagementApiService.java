package psu.signlinguamobile.api.apiservice;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface BookingManagementApiService
{
    @POST("/booking-management/tutors/id")
    Call<Void> sendBookRequest(@Path("id") String tutorId);

    @DELETE("/booking-management/tutors/id")
    Call<Void> cancelBookRequest(@Path("id") String tutorId);

    @DELETE("/booking-management/tutors/id")
    Call<Void> leaveTutor(@Path("id") String tutorId);
}