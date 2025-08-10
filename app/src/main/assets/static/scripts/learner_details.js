const disabilityBadges = {
    //0: {'class' : 'disability-', 'img' : '../static/images/badge_noimp_s.png'},
    1: {'class' : 'disability-deaf', 'img' : '../static/images/badge_deaf_s.png'},
    2: {'class' : 'disability-mute', 'img' : '../static/images/badge_mute_s.png'},
    3: {'class' : 'disability-both', 'img' : '../static/images/badge_deafmute_s.png'}
}

$(document).ready(function()
{
    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    $('#btn-back').on("click", function () {

        if (window.LearnerDetailsJsBridge) {
            window.LearnerDetailsJsBridge.onGoBack();
        }
    });

    // renderDetails(sampleData);
});

function renderDetails(json)
{
    if (!json)
    {
        return;
    }

    // If the page is fully loaded, it closes the loader automatically during first load
    // Now if we want to do long running action, we can manually show the loader
    // then hide it manually later on
    if (IS_FULLY_LOADED)
        showLoading();

    let profileDetails = JSON.parse(json);
    
    $('#dynamic-name').text(profileDetails.fullname);
    $('#dynamic-username').text(`@${profileDetails.username}`);
    $('#dynamic-bio').text(profileDetails.bio);
    $('#dynamic-photo').attr('src', profileDetails.photo);
    $('#dynamic-contact-no').text(profileDetails.contact);
    $('#dynamic-date-joined').text(profileDetails.dateJoined);
    $('#dynamic-tutor-count').text(formatNumber(profileDetails.totalTutors));
    $('#dynamic-classroom-count').text(formatNumber(profileDetails.totalClassrooms));
    $('#dynamic-email').text(profileDetails.email);
    $('#dynamic-address').text(profileDetails.address);
    
    if (profileDetails.disability > 0)
    {
        let disability = disabilityBadges[profileDetails.disability];
        let badge = $('#profile-disability-badge-wrapper');

        badge.attr('class', disability.class);
        badge.find('#dynamic-disability-badge').attr('src', disability.img);
        badge.show();
    }

    if (profileDetails.bookStatus)
    {
        switch(profileDetails.bookStatus)
        {
            case "booked":
                $('#btn-drop-learner').show();
                break;

            case "requested":
                $('#btn-cancel-request').show();
                break;

            case "new":
                $('#btn-add-learner').show();
                break;
        }
    }

    // If page is fully loaded, it closes the loader automatically during first load
    // Now if we want to do long running action, we can manually show the loader
    // then hide it manually
    if (IS_FULLY_LOADED)
        hideLoading();
}

function formatNumber(num)
{
    if (num < 1000) return num; // Show full number for values below 1000

    const suffixes = ["", "K", "M", "B", "T"]; // Thousand, Million, Billion, Trillion
    let suffixIndex = Math.floor(Math.log10(num) / 3); // Determine suffix index
    let shortNum = (num / Math.pow(1000, suffixIndex)).toFixed(1); // Format number
    return shortNum.replace(/\.0$/, '') + suffixes[suffixIndex]; // Remove trailing .0
}