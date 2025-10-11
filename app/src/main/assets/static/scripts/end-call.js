$(document).ready(() => {

    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    $('#btn-close').on('click', function() {

        if (window.EndCallJsBridge?.onClose)
            window.EndCallJsBridge.onClose();
    });

    $('#btn-call-again').on('click', function() {

        if (window.EndCallJsBridge?.onCallAgain)
            window.EndCallJsBridge.onCallAgain();
    });
});