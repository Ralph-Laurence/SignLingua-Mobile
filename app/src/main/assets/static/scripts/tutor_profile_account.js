let fields = {};
let bioMaxLength;

document.addEventListener('DOMContentLoaded', function()
{
    // If the page is fully loaded, it closes the loader automatically during first load
    // Now if we want to do long running action, we can manually show the loader
    // then hide it manually later on
    if (IS_FULLY_LOADED)
        showLoading();

    // renderDetails(details);

    $('#btn-back').on("click", function () {

        if (window.TutorProfileAccountJsBridge) {
            window.TutorProfileAccountJsBridge.onGoBack();
        }
    });

    $('#btn-save')

    // Necessary so that the soft keyboard wont obstruct the webview button
    .on('mousedown', (e) => e.preventDefault())
    
    .on('click', function () {
        
        const active = document.activeElement;
    
        // Force blur if an input is focused
        if (active && (active.tagName === 'INPUT' || active.tagName === 'TEXTAREA')) {
            active.blur();
        }
    
        // Delay execution slightly to ensure blur settles
        setTimeout(() => {
            let fields = {
                'bio'       : $('#input-bio'),
                'username'  : $('#input-username'),
                'email'     : $('#input-email')
            };
    
            for (const [k, v] of Object.entries(fields)) {
                if (v.val().trim() === '') {
                    AlertWarn(`Please add your ${k}.`);
                    v.focus();
                    return;
                }
            }
    
            showLoading('action');
    
            if (window.TutorProfileAccountJsBridge) {
                window.TutorProfileAccountJsBridge.onSaveProfile(
                    $('#input-email').val(),
                    $('#input-username').val(),
                    $('#input-bio').val()
                );
            }
        }, 100); // slight debounce
    });    

    bioMaxLength = parseInt($('#input-bio').attr('maxlength'), 10);

    $('#input-bio').on('input', function()
    {
        updateCharLengthCounter($('#input-bio').val().length, bioMaxLength);
    });

    $('#input-email, #input-username')
        .on('focus', () => adjustToSoftKeyboard('expand'))
        .on('blur',  () => adjustToSoftKeyboard('collapse'));

    $('#btn-remove-photo').on('click', function()
    {
        showConfirmPrompt('Are you sure, you want to remove your profile picture?', {
            'onOK': function()
            {
                showLoading('action');

                if (window.TutorProfileAccountJsBridge) {
                    window.TutorProfileAccountJsBridge.onRemoveProfilePic();
                }
            }
        });
    });

    let modalProfilePicSelection = new bootstrap.Modal(document.querySelector('#modal-choose-pic-option'));

    $('#change-photo-option-gallery').on('click', function()
    {
        modalProfilePicSelection.hide();

        if (window.TutorProfileAccountJsBridge) {
            window.TutorProfileAccountJsBridge.onSelectGalleryPhoto();
        }
    });

    $('#change-photo-option-capture').on('click', function()
    {
        modalProfilePicSelection.hide();
        
        if (window.TutorProfileAccountJsBridge) {
            window.TutorProfileAccountJsBridge.onCapturePhoto();
        }
    });

    if (IS_FULLY_LOADED)
        hideLoading();
});

function adjustToSoftKeyboard(action = 'expand')
{
    let style = (action === 'expand') ? '300px' : '1rem';

    $('.container').attr('style', `padding-bottom: ${style} !important`);
    $('html, body').animate({ scrollTop: $(document).height() }, 300);
}

function renderDetails(json)
{
    if (!json)
        return;

    showLoading('action');

    let obj = JSON.parse(json);

    $('#dynamic-tutor-username').text(obj.username);
    $('#dynamic-tutor-photo, #previewer-change-photo').attr('src', obj.photo);

    if (obj.photo.includes('default_avatar'))
        $('#btn-remove-photo').hide();
    else
        $('#btn-remove-photo').show();

    $('#input-username').val(obj.username);
    $('#input-email').val(obj.email);
    $('#input-bio').val(obj.bio);

   updateCharLengthCounter($('#input-bio').val().length, bioMaxLength);

    hideLoading();
}

function renderUpdatedDetails(json)
{
    if (!json)
    {
        hideLoading();
        return;
    }

    let obj = JSON.parse(json);

    $('#dynamic-tutor-username').text(obj.username);

    $('#input-username').val(obj.username);
    $('#input-email').val(obj.email);
    $('#input-bio').val(obj.bio);

    updateCharLengthCounter($('#input-bio').val().length, bioMaxLength);

    hideLoading();

    setTimeout( () => {
        // Allow the loading overlay to close first before showing the snackbar
        ShowSnackbar(obj.msg);
    }, 1200);
}

function renderProfilePicture(src)
{
    if (typeof src === 'undefined' || src === '')
        return;

    $('#dynamic-tutor-photo, #previewer-change-photo').attr('src', src);

    if (src.includes('default_avatar'))
        $('#btn-remove-photo').hide();
    else
        $('#btn-remove-photo').show();
}

function updateCharLengthCounter(current, max)
{
    $('#bio-char-counter').text(`${current}/${max}`);
}