$(document).ready(() => {
    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    $("#btn-login").on("click", function ()
    {
        if (window.RegistrationLandingBridge)
            window.RegistrationLandingBridge.onLoginPage();
    });

    $('#btn-become-tutor').on('click', function()
    {
        if (window.RegistrationLandingBridge)
            window.RegistrationLandingBridge.onRegisterTutorPage();
    });

    $('#btn-join-learner').on('click', function()
    {
        if (window.RegistrationLandingBridge)
            window.RegistrationLandingBridge.onRegisterLearnerPage();
    });
});