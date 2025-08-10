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

        if (window.TutorProfilePasswordsJsBridge) {
            window.TutorProfilePasswordsJsBridge.onGoBack();
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
                'old'  : { control: $('#input-old-password'),     warning: "To confirm you're making this change, please enter your old password." },
                'new'  : { control: $('#input-new-password'),     warning: "Please enter your new password." },
                'conf' : { control: $('#input-confirm-password'), warning: "Please confirm your new password." }
            };            
    
            for (const [k, field] of Object.entries(fields))
            {
                if (field.control.val().trim() === '')
                {
                    AlertWarn(field.warning);
                    return;
                }
            }

            if (fields['new'].control.val().length < 8)
            {
                AlertWarn("Your new password must be at least 8 characters.");
                return;
            }

            if (fields['new'].control.val() !== fields['conf'].control.val())
            {
                AlertWarn("Your passwords didn't match. Please confirm your new password by retyping it.");
                return;
            }
    
            showLoading('action');
    
            if (window.TutorProfilePasswordsJsBridge) {
                window.TutorProfilePasswordsJsBridge.onSavePassword(
                    fields['old'].control.val(),
                    fields['new'].control.val(),
                    fields['conf'].control.val(),
                );
            }
        }, 100); // slight debounce
    });    

    $('.form-control')
        .on('focus', () => adjustToSoftKeyboard('expand'))
        .on('blur',  () => adjustToSoftKeyboard('collapse'));

    if (IS_FULLY_LOADED)
        hideLoading();
});

function adjustToSoftKeyboard(action = 'expand')
{
    let style = (action === 'expand') ? '300px' : '1rem';

    $('.container').attr('style', `padding-bottom: ${style} !important`);
    $('html, body').animate({ scrollTop: $(document).height() }, 300);
}

function notifySuccessfulPasswordUpdate(msg)
{
    if (msg)
        ShowSnackbar(msg);

    $('.form-control').val('');
}

function renderDetails(json)
{
    if (!json)
        return;

    showLoading('action');

    let obj = JSON.parse(json);

    $('#dynamic-tutor-username').text(obj.username);
    $('#dynamic-tutor-photo').attr('src', obj.photo);

    hideLoading();
}