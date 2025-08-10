package psu.signlinguamobile.api.apiservice;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import psu.signlinguamobile.api.apirequest.UpdatePasswordRequest;
import psu.signlinguamobile.api.apirequest.UpdateProfileAccountRequest;
import psu.signlinguamobile.api.apirequest.UpdateProfileGeneralRequest;
import psu.signlinguamobile.api.apirequest.UpdateSkillsRequest;
import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.api.apiresponse.PaginatedResponse;
import psu.signlinguamobile.api.apiresponse.TutorClassroomsResponse;
import psu.signlinguamobile.api.apiresponse.TutorDetailsResponse;
import psu.signlinguamobile.api.apiresponse.TutorProfileDetailsResponse;
import psu.signlinguamobile.models.CertificationDocument;
import psu.signlinguamobile.models.EducationDocument;
import psu.signlinguamobile.models.TutorItem;
import psu.signlinguamobile.models.UpdateProfileGeneral;
import psu.signlinguamobile.models.UpdatedProfileAccount;
import psu.signlinguamobile.models.User;
import psu.signlinguamobile.models.WorkDocument;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface TutorManagementApiService
{
    //=================================================
    // <editor-fold desc="SECTION: GET ROUTES">
    //=================================================
    @GET("tutor-management/tutors/filter-results")
    Call<PaginatedResponse<TutorItem>> findTutors(
            @Query("page") int page,
            @Query("search") String keyword,
            @Query("disability") String disability,
            @Query("include") String include // 'all' -> fetch All tutors | 'friend' -> only those booked with | 'new' -> those who arent booked with
    );

    @GET("tutor-management/tutors/tutor/details")
    Call<TutorDetailsResponse> showTutor(
            @Query("id") String hashedId
    );

    @GET("tutor-management/tutors/classrooms")
    Call<TutorClassroomsResponse> fetchClassrooms();

    @GET("tutor-management/tutors/tutor/profile/account")
    Call<TutorProfileDetailsResponse> fetchAccountDetails();

    @GET("tutor-management/tutors/tutor/profile/general")
    Call<TutorProfileDetailsResponse> fetchGeneralDetails();

    @GET("tutor-management/tutors/tutor/profile/education")
    Call<TutorProfileDetailsResponse> fetchEducation();

    @GET("tutor-management/tutors/tutor/profile/certifications")
    Call<TutorProfileDetailsResponse> fetchCertifications();

    @GET("tutor-management/tutors/tutor/profile/experience")
    Call<TutorProfileDetailsResponse> fetchWorkExp();

    @GET("tutor-management/tutors/tutor/profile/skills")
    Call<TutorProfileDetailsResponse> fetchSkills();

    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="SECTION: UPDATE ROUTES">
    //=================================================
    @PUT("tutor-management/tutors/tutor/profile/account")
    Call<CommonResponse<UpdatedProfileAccount>> updateProfileAccount(@Body UpdateProfileAccountRequest body);

    @PUT("tutor-management/tutors/tutor/profile/certifications")
    Call<CommonResponse<CertificationDocument>> updateCertification(@Body CertificationDocument body);

    @PUT("tutor-management/tutors/tutor/profile/education")
    Call<CommonResponse<EducationDocument>> updateEducation(@Body EducationDocument body);

    @PUT("tutor-management/tutors/tutor/profile/general")
    Call<CommonResponse<UpdateProfileGeneral>> updateGeneral(@Body UpdateProfileGeneralRequest body);

    @PUT("tutor-management/tutors/tutor/profile/password")
    Call<CommonResponse<Void>> updatePassword(@Body UpdatePasswordRequest body);

    @PUT("tutor-management/tutors/tutor/profile/skills")
    Call<CommonResponse<Void>> updateSkills(@Body UpdateSkillsRequest body);

    @PUT("tutor-management/tutors/tutor/profile/work")
    Call<CommonResponse<WorkDocument>> updateWorkExperience(@Body WorkDocument body);

    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="SECTION: DELETE ROUTES">
    //=================================================

    @DELETE("tutor-management/tutors/tutor/profile/photo")
    Call<CommonResponse<User>> removeProfilePicture();

    @HTTP(method = "DELETE", path = "tutor-management/tutors/tutor/profile/certifications", hasBody = true)
    Call<CommonResponse<TutorProfileDetailsResponse>> deleteCertification(@Body CertificationDocument body);

    @HTTP(method = "DELETE", path = "tutor-management/tutors/tutor/profile/education", hasBody = true)
    Call<CommonResponse<TutorProfileDetailsResponse>> deleteEducation(@Body EducationDocument body);

    @HTTP(method = "DELETE", path = "tutor-management/tutors/tutor/profile/work", hasBody = true)
    Call<CommonResponse<TutorProfileDetailsResponse>> deleteWorkExp(@Body WorkDocument body);
    //=================================================
    // </editor-fold>
    //=================================================


    //=================================================
    // <editor-fold desc="SECTION: POST ROUTES">
    //=================================================
    @Multipart
    @POST("tutor-management/tutors/tutor/profile/photo")
    Call<CommonResponse<User>> uploadProfilePicture(@Part MultipartBody.Part image);
    //=================================================
    // </editor-fold>
    //=================================================
}