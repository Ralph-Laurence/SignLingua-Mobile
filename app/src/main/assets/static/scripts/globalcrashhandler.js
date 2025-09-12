$(document).ready(() => {

    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function ()
    {
        history.pushState(null, "", window.location.href);
    };

    $('#btn-exit').on('click', function()
    {
        if (window.GlobalCrashHandlerBridge)
            window.GlobalCrashHandlerBridge.onTerminate();
    });
});