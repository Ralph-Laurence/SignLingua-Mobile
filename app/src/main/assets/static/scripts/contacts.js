// const data = `{
// 	"status": 200,
// 	"message": "OK",
// 	"content": {
// 		"contactsList": [],
// 		"userInformation": {
// 			"photo": "http://localhost:8000/storage/uploads/profiles/27e485fa-6be1-47e9-8945-3221d567a746.jpg",
// 			"role": 1
// 		}
// 	}
// }`;

const ROLE_TUTOR   = 1;
const ROLE_LEARNER = 2;

let contactItemTemplate = `<button class="btn contact-item p-3 mb-2 shadow-sm rounded d-flex w-100 align-items-center gap-2"  data-contact-id="#contact_id#">
    <div class="contact-photo user-photo">
        <img src="#contact_photo#">
    </div>
    <div class="flex-fill overflow-hidden">
        <h6 class="contact-name w-100 m-0 text-start text-truncate text-14">#contact_name#</h6>
        #last_message#
    </div>
</button>`;

let lastMessageTemplate = 
`<div class="w-100 d-flex align-items-center text-13 gap-2 overflow-hidden">
    <div class="text-truncate text-start flex-fill contact-last-message">#last_message#</div>
    <div class="ms-auto contact-last-message-time">#time#</div>
</div>`;

$(document).ready(() => {

    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    $(document).on('click', '.contact-item', function(e)
    {
        let contactId = $(e.currentTarget).data('contact-id');

        if (window.ContactsJsBridge)
            window.ContactsJsBridge.onContactItemSelected(contactId);
    });

    $('#user-profile-link').on('click', function()
    {
        if (window.ContactsJsBridge)
            window.ContactsJsBridge.onProfileLink();
    });

    //renderDetails(data)
});

function renderDetails(data)
{
    if (data === '' || data === undefined || data === null)
    {
        return;
    }

    // If the page is fully loaded, it closes the loader automatically during first load
    // Now if we want to do long running action, we can manually show the loader
    // then hide it manually later on
    if (IS_FULLY_LOADED)
        showLoading();

    let obj = JSON.parse(data);

    $('.contact-list').empty();

    if (obj.content.userInformation.photo)
        renderProfilePicture( obj.content.userInformation.photo );

    if (obj.content.contactsList && obj.content.contactsList.length > 0)
    {
        $('.contact-list').append(`<h6 class="opacity-70">Contacts (${obj.content.contactsList.length})</h6>`);

        obj.content.contactsList.forEach(contact => {
            let template = contactItemTemplate;
    
            template = template.replace('#contact_photo#', contact.photo)
                .replace('#contact_name#', contact.name)
                .replace('#contact_id#', contact.id);
    
            let lastMessage = '';

            if ('last_message_body' in contact && contact.last_message_body.trim() !== '')
            {
                lastMessage = lastMessageTemplate.replace('#last_message#', contact.last_message_body)
                    .replace('#time#', contact.last_message_time);
            }

            template = template.replace('#last_message#', lastMessage);
            
            $('.contact-list').append( renderEmojis(template) );
        });

        $('.contact-list').show();
    }
    else
    {
        // console.log('aint an array');
        //$('.empty-classrooms-indicator').show();
        let descriptor = '';
        let role = obj.content.userInformation.role;

        if (role === ROLE_LEARNER)
            descriptor = "You haven't booked a session yet. Once you connect with a tutor, they'll appear here so you can chat and learn together.";

        else if (role === ROLE_TUTOR)
            descriptor = "No learners have booked with you yet. Once you connect with a learner, they'll appear here so you can chat and learn together.";

        $('#empty-contacts-descriptor').text(descriptor);
        $('.indicator-empty-contacts').show();
    }

    if (IS_FULLY_LOADED)
        hideLoading();
}

function renderEmojis(text) {
    const icons = $.emojiarea.icons;
    const basePath = $.emojiarea.path;

    const rawHtml = text.replace(/:\w+:/g, function (match) {
        if (icons[match]) {
            return `<img src="${basePath}/${icons[match]}" alt="${match}" class="emoji-img">`;
        }
        return match; // leave unknown shortcodes untouched
    });

    // Sanitize the final HTML before returning
    return DOMPurify.sanitize(rawHtml, {
        ALLOWED_TAGS: ['img', 'div', 'h6', 'button'],
        ALLOWED_ATTR: ['src', 'alt', 'class']
    });
}

function renderProfilePicture(src)
{
    if (src && typeof src === 'string' && src.trim() !== '')
        $('#user-profile-link img').attr('src', src);
}