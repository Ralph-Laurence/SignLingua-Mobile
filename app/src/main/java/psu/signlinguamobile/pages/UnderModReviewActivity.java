package psu.signlinguamobile.pages;
import psu.signlinguamobile.delegates.VerificationModReviewJsBridge;

public class UnderModReviewActivity
       extends BaseWebViewActivity
       implements VerificationModReviewJsBridge.VerificationModReviewBridgeListener
{
    @Override
    protected void onAwake()
    {
        shouldCheckAuth(false);
    }

    @Override
    protected void onInitialize()
    {
        registerJsBridge(new VerificationModReviewJsBridge(this), JS_BRIDGE_NAME);
        renderView("undermodreview");
    }

    @Override
    protected void onBackKey()
    {
        launch(TutorHomeActivity.class);
    }

    @Override
    protected void onViewLoaded()
    {

    }

    @Override
    protected void onDispose()
    {
        this.unregisterJsBridge(JS_BRIDGE_NAME);
    }

    @Override
    public void onGoHome()
    {
        launch(TutorHomeActivity.class);
    }

    @Override
    public void onTerminate()
    {
        finishAndRemoveTask();
    }
}
