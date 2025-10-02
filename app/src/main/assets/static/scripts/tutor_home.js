$(document).ready(() => {

    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    $("#btn-logout").on("click", function () {

        if (window.TutorHomeJsBridge) {
            window.TutorHomeJsBridge.onLogout();
        }
    });

    $('#btn-nav-find-learners').on("click", function () {

        if (window.TutorHomeJsBridge) {
            window.TutorHomeJsBridge.onNavFindLearners();
        }
    });

    $('#btn-nav-my-learners').on("click", function () {

        if (window.TutorHomeJsBridge) {
            window.TutorHomeJsBridge.onNavMyLearners();
        }
    });
    
    $('#btn-nav-scanner').on("click", function () {

        if (window.TutorHomeJsBridge) {
            window.TutorHomeJsBridge.onNavScanner();
        }
    });
    
    // $('#btn-nav-classrooms').on("click", function () {

    //     if (window.TutorHomeJsBridge) {
    //         window.TutorHomeJsBridge.onNavClassrooms();
    //     }
    // });
    
    $('#btn-nav-chat').on("click", function () {

        if (window.TutorHomeJsBridge) {
            window.TutorHomeJsBridge.onNavChat();
        }
    });

    $('#btn-nav-account').on('click', function()
    {
        if (window.TutorHomeJsBridge) {
            window.TutorHomeJsBridge.onNavMyProfile();
        }
    });

    $('#btn-nav-hire-reqs').on('click', function()
    {
        if (window.TutorHomeJsBridge) {
            window.TutorHomeJsBridge.onNavHireRequests();
        }
    });
});