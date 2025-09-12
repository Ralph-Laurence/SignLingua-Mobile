package psu.signlinguamobile.utilities;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

import okhttp3.ResponseBody;
import psu.signlinguamobile.pages.CaptureIDBoardingActivity;
import psu.signlinguamobile.pages.UnderModReviewActivity;
import retrofit2.Response;

// This isn't a real middleware class.
// This just checks the response code after a successful REST call.
public class VerificationMiddlewareChecker
{
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = -1;
    public static final int STATUS_PENDING = 2;         // <- For newly created accounts
    public static final int STATUS_UNDER_REVIEW = 3;    // <- Received by admins but isnt approved yet

    private final WeakReference<Activity> parentActivity;

    public VerificationMiddlewareChecker(Activity parentActivity)
    {
        this.parentActivity = new WeakReference<>(parentActivity);
    }

    private Activity getParentActivity()
    {
        return this.parentActivity.get();
    }

    public boolean IsAllowed(Response<?> response)
    {
        int code = response.code();

        if (code != HttpCodes.FORBIDDEN && code != HttpCodes.UNAUTHORIZED) {
            return true;
        }

        try (ResponseBody errorJson = response.errorBody()) {
            JSONObject obj = new JSONObject(errorJson.string());

            int status = obj.optInt("registrationStatus", -999);

            switch (status) {
                case STATUS_PENDING:
                    launch(CaptureIDBoardingActivity.class);
                    break;

                case STATUS_UNDER_REVIEW:
                    launch(UnderModReviewActivity.class);
                    break;
            }
        }
        catch (Exception e)
        {
            Log.e("VerificationMiddleware", "Failed to parse error response", e);
        }

        return false;
    }

    protected <T> void launch(Class<T> activity)
    {
        Activity parent = getParentActivity();
        if (parent == null) return;

        Intent intent = new Intent(parent, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        parent.finish();
        parent.startActivity(intent);
    }
}
