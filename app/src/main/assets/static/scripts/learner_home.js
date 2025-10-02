$(document).ready(() => {

    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    $("#btn-logout").on("click", function () {

        if (window.LearnerHomeJsBridge) {
            window.LearnerHomeJsBridge.onLogout();
        }
    });

    $('#btn-nav-find-learners').on("click", function () {

        if (window.LearnerHomeJsBridge) {
            window.LearnerHomeJsBridge.onNavFindTutors();
        }
    });

    // $('#btn-nav-classrooms').on("click", function () {

    //     if (window.LearnerHomeJsBridge) {
    //         window.LearnerHomeJsBridge.onNavClassrooms();
    //     }
    // });

    $('#btn-nav-chat').on("click", function () {

        if (window.LearnerHomeJsBridge) {
            window.LearnerHomeJsBridge.onNavChat();
        }
    });
    
    $('#btn-nav-my-tutors').on("click", function () {

        if (window.LearnerHomeJsBridge) {
            window.LearnerHomeJsBridge.onNavMyTutors();
        }
    });

    $('#btn-nav-account').on("click", function () {

        if (window.LearnerHomeJsBridge) {
            window.LearnerHomeJsBridge.onNavMyProfile();
        }
    });

    $('#btn-nav-scanner').on("click", function () {

        if (window.LearnerHomeJsBridge) {
            window.LearnerHomeJsBridge.onNavScanner();
        }
    });
});