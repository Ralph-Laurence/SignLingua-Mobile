let badges = {
    '1': '../static/images/new_badge_deaf.png',
    '2': '../static/images/new_badge_mute.png',
    '3': '../static/images/new_badge_deaf_mute.png',
}

let disabilityClass = {
    '1': 'deaf',
    '2': 'mute',
    '3': 'deafmute',
}
let lblTotalRequests = null;
let template = 
`<div data-learner-id="#itemid#" class="request-item w-100 d-flex gap-3 p-3">
    <div class="profile-wrapper #disabilityclass#">
        <div class="profile-pic">
            <img src="#profilepic#" alt="">
        </div>
        <div class="status-badge">
            <img src="#badge#" alt="status">
        </div>
    </div>   
    <div class="flex-fill">
        <h6 class="w-100 text-16 learner-name" data-fullname="#fullname#">#fullname#</h6>
        <h6 class="w-100 text-16 text-truncate opacity-45">@#username#</h6>
        <div class="d-flex align-items-center justify-content-around gap-2 w-100">
            <button type="button" class="btn small-button primary-button w-100 px-3 btn-confirm-request">
                Confirm
            </button>
            <button type="button" class="btn small-button secondary-button w-100 px-3 btn-delete-request">
                Delete
            </button>
        </div>
    </div>             
</div>`;

$(document).ready(() => {

    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    lblTotalRequests = $('#total_requests');
    $("#btn-back").on("click", function () {

        if (window.TutorHireRequestsActivityJsBridge) {
            window.TutorHireRequestsActivityJsBridge.onGoBack();
        }
    });

    $('#btn-find-learners').on('click', function()
    {
        if (window.TutorHireRequestsActivityJsBridge) {
            window.TutorHireRequestsActivityJsBridge.onFindLearners();
        }
    });
    
    $(document).on('click', '.btn-confirm-request', function(e)
    {
        e.stopPropagation();

        let learnerId = $(this).closest('.request-item').data('learner-id');

        if (window.TutorHireRequestsActivityJsBridge)
        {
            showLoading('action');
            window.TutorHireRequestsActivityJsBridge.onConfirmRequest(learnerId);
        }
    });

    $(document).on('click', '.btn-delete-request', function(e) {

        e.stopPropagation();

        let learnerId   = $(this).closest('.request-item').data('learner-id');
        let learnerName = $(this).closest('.request-item').find('.learner-name').data('fullname');
        let promptMsg   = `Are you sure, you want to decline the hire request for learner <strong>${learnerName}</strong>? The learner won't be notified, and they can still reach out again later.`;
        
        showConfirmPrompt(promptMsg, {
            'onOK': function()
            {
                if (window.TutorHireRequestsActivityJsBridge)
                {
                    showLoading('action');
                    window.TutorHireRequestsActivityJsBridge.onDeclineRequest(learnerId);
                }  
            }
        });
    });

    $(document).on('click', '.request-item', function(e)
    {
        let learnerId = $(this).data('learner-id');
        
        if (window.TutorHireRequestsActivityJsBridge)
            window.TutorHireRequestsActivityJsBridge.onShowLearnerInfo(learnerId);
    });
});

function showEmptyContentWrapper(){
    $('#tutor-on-empty-friend-requests').show();
    $('#list-friend-requests').hide();
}

function hideEmptyContentWrapper() {
    $('#tutor-on-empty-friend-requests').hide();
    $('#list-friend-requests').show();
}

function renderContent(content)
{
    const container = $('#list-friend-requests .main-content');
    container.empty();

    if (!content || content.trim() === '') {
        showEmptyContentWrapper();
        return;
    }

    let obj;
    try {
        obj = JSON.parse(content);
    } catch (e) {
        console.error('Invalid JSON:', e);
        showEmptyContentWrapper();
        return;
    }

    if (!Array.isArray(obj) || obj.length === 0)
    {
        showEmptyContentWrapper();
        return;
    }

    let total = 0;

    for (const item of obj)
    {
        const username            = item.username || 'unknown';
        const name                = item.name || 'Unnamed';
        const photo               = item.photo || '../static/images/default_avatar.png';
        const disability          = String(item.disability ?? '0'); // Normalize to string
        const badge               = badges.hasOwnProperty(disability) ? badges[disability] : '';
        const disabilityClassName = disabilityClass.hasOwnProperty(disability) ? disabilityClass[disability] : '';

        let html = template
            .replace('#username#', username)
            .replaceAll('#fullname#', name)
            .replace('#disabilityclass#', disabilityClassName)
            .replace('#badge#', badge)
            .replace('#profilepic#', photo)
            .replace('#itemid#', item.learnerId);

        const elem = $(html);

        if (disability === '0')
        {
            elem.find('.status-badge').remove();
            elem.find('.profile-wrapper').addClass('no-border');
        }

        container.append(elem);
        total++;
    }

    lblTotalRequests.text(total === 1 ? '1 Request' : `${total} Requests`);
    hideEmptyContentWrapper();
}

function dequeueRequestItem(learnerId)
{
    let rootElements = $('#list-friend-requests .main-content .request-item');

    if (!rootElements.length) return;

    rootElements.each(function(index, element)
    {
        let targetId = $(element).data('learner-id');

        if (targetId == learnerId)
        {
            $(element).remove();
            return false; // break loop
        }
    });

    // Re-check after removal
    const remaining = $('#list-friend-requests .main-content .request-item').length;

    if (remaining === 0)
    {
        showEmptyContentWrapper();
        lblTotalRequests.text('0 Requests');
    }
    else
    {
        lblTotalRequests.text(`${remaining} Requests`);
    }
}
