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

    $('#btn-confirm-request').on('click', function() {
        if (window.LearnerDetailsJsBridge) {
            showLoading('action');
            window.LearnerDetailsJsBridge.onConfirmRequest();
        }
    });

    $('#btn-decline-request').on('click', function()
    {
        let learnerName = $('#dynamic-name').text();
        let promptMsg   = `Are you sure, you want to decline the hire request for learner <strong>${learnerName}</strong>? The learner won't be notified, and they can still reach out again later.`;

        showConfirmPrompt(promptMsg, {
            'onOK': function()
            {
                if (window.LearnerDetailsJsBridge)
                {
                    showLoading('action');
                    window.LearnerDetailsJsBridge.onDeclineRequest();
                }  
            }
        });
    });

    $('#btn-drop-learner').on('click', function()
    {
        let learnerName = $('#dynamic-name').text();
        let promptMsg = `Are you sure you want to drop <strong>${learnerName}</strong> from your learners? The learner won't be notified, and they can still reach out again later.`;

        showConfirmPrompt(promptMsg, {
            'onOK': function()
            {
                if (window.LearnerDetailsJsBridge)
                {
                    showLoading('action');
                    window.LearnerDetailsJsBridge.onDropLearner();
                }  
            }
        });
    });

    $('#btn-add-learner').on('click', function()
    {
        if (window.LearnerDetailsJsBridge) {
            showLoading('action');
            window.LearnerDetailsJsBridge.onAddLearner();
        }
    });

    $('#btn-cancel-request').on('click', function()
    {
        let learnerName = $('#dynamic-name').text();
        let promptMsg = `Are you sure you want to cancel your request to add <strong>${learnerName}</strong> as one of your learners?`;

        showConfirmPrompt(promptMsg, {
            'okText': 'Yes',
            'cancelText': 'No',
            'onOK': function()
            {
                if (window.LearnerDetailsJsBridge)
                {
                    showLoading('action');
                    window.LearnerDetailsJsBridge.onCancelRequest();
                }  
            }
        });
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

            // Learner sent request to tutor
            case "request_recieved":
                $('#learner-request-controls').show();
                break;

            // Tutor sent request to learner
            case "request_sent":
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

function handleRequestDeclined()
{
    $('#learner-request-controls').hide();
    $('#btn-add-learner').show();
}

function handleRequestConfirmed()
{
    $('#learner-request-controls').hide();
    $('#btn-drop-learner').show();
}

function handleRequestCanceled()
{
    // Cancelled is different from declined.
    // Decline is called when learner sents the request.
    // Canceled is called when tutor sents the request.
    $('#booking-controls-container .btn').hide();
    $('#btn-add-learner').show();
}

function handleLearnerDropped()
{
    $('#booking-controls-container .btn').hide();
    $('#btn-add-learner').show();
}

function handleLearnerAdded()
{
    $('#booking-controls-container .btn').hide();
    $('#btn-cancel-request').show();
}

function formatNumber(num)
{
    if (num < 1000) return num; // Show full number for values below 1000

    const suffixes = ["", "K", "M", "B", "T"]; // Thousand, Million, Billion, Trillion
    let suffixIndex = Math.floor(Math.log10(num) / 3); // Determine suffix index
    let shortNum = (num / Math.pow(1000, suffixIndex)).toFixed(1); // Format number
    return shortNum.replace(/\.0$/, '') + suffixes[suffixIndex]; // Remove trailing .0
}