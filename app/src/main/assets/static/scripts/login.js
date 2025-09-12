let loginFailAlert;

$(document).ready(() => {

    loginFailAlert = $('#alert-login-failed');

    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    $("#btn-sign-in").on("click", function ()
    {
        loginFailAlert.hide();

        const username = $("#input-umail").val().trim();
        const password = $("#input-password").val().trim();

        if (!username || !password)
        {
            alertInvalidCredentials();
            return;
        }

        // console.log("Username:", username);
        // console.log("Password:", password);

        if (window.LoginBridge) {
            showLoading('action')
            window.LoginBridge.onSignIn(username, password);
        }
    });

    $('#btn-register').on('click', function()
    {
        if (window.LoginBridge)
            window.LoginBridge.onRegister();
    });
});

function alertInvalidCredentials()
{
    loginFailAlert.find('.message').html(`Login Failed!<br>Incorrect username or password.`);
    loginFailAlert.show();
    hideLoading()
}

function alertNetworkError() {
    loginFailAlert.find('.message').html(`Login Failed!<br>Looks like we lost connection! Please check your internet and try again.`);
    loginFailAlert.show();
    hideLoading();
}