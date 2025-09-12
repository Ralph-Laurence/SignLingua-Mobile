$(document).ready(() => {
    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    $("#btn-cancel").on("click", promptCancelRegistration);

    $('#btn-proceed').on('click', function()
    {
        showLoading('action');
        
        if (window.CaptureIDBoardingBridge)
            window.CaptureIDBoardingBridge.onProceedRegistration();
    });
});

function promptCancelRegistration()
{
    showConfirmPrompt('Are you sure, you want to cancel your registration?', {
        'okText': 'Yes',
        'cancelText': 'No',
        'onOK': function()
        {
            showLoading('action');

            if (window.CaptureIDBoardingBridge)
                window.CaptureIDBoardingBridge.onCancelRegistration(); 
        }
    });
}

function exitOnNullUserID()
{
    let err = 'Sorry, we encountered a technical issue and had to close the app.';

    AlertWarn(err, 'Critical Error', {
        'onOK': function()
        {
            if (window.CaptureIDBoardingBridge)
                window.CaptureIDBoardingBridge.onTerminateAppOnCriticalError();
        }
    });
}