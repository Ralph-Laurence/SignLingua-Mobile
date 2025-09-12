$(document).ready(() => {

    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function ()
    {
        history.pushState(null, "", window.location.href);
    };

    $('#btn-home').on('click', function()
    {
        if (window.VerificationModReviewBridge)
            window.VerificationModReviewBridge.onGoHome();
    });

    $('#btn-exit').on('click', function()
    {
        if (window.VerificationModReviewBridge)
            window.VerificationModReviewBridge.onTerminate();
    });
});