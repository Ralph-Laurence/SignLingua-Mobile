package psu.signlinguamobile.pages;

import psu.signlinguamobile.delegates.GlobalCrashHandlerJsBridge;

public class GlobalCrashHandler
        extends BaseWebViewActivity
        implements GlobalCrashHandlerJsBridge.GlobalCrashHandlerJsBridgeListener
{
    @Override
    protected void onAwake()
    {
        shouldCheckAuth(false);
    }

    @Override
    protected void onInitialize()
    {
        registerJsBridge(new GlobalCrashHandlerJsBridge(this), JS_BRIDGE_NAME);
        renderView("globalcrashhandler");
    }

    @Override
    protected void onBackKey()
    {
        finishAndRemoveTask();
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
    public void onTerminate()
    {
        finishAndRemoveTask();
    }
}
