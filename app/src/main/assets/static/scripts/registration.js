const TUTOR_REGISTRATION = 1;
const LEARNER_REGISTRATION = 2;

let inputs = {
    'fname'     : {'field': '', 'warning' : 'Please enter your firstname'   , 'page': 1},
    'lname'     : {'field': '', 'warning' : 'Please enter your lastname'    , 'page': 1},
    'contact'   : {'field': '', 'warning' : 'Please add your contact number', 'page': 1},
    'address'   : {'field': '', 'warning' : 'Please add your address'       , 'page': 1},

    'impairment' : {'field': '', 'page': 2},

    'email'     : {'field': '', 'warning' : 'Please add your email'         , 'page': 3},
    'username'  : {'field': '', 'warning' : 'Please enter your username'    , 'page': 3},
    'newpassw'  : {'field': '', 'warning' : 'Please add your password'      , 'page': 3},
    'repassw'   : {'field': '', 'warning' : 'Please confirm your password'  , 'page': 3},
}

let carousel;
let registrationType = -1;

$(document).ready(() => {

    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    inputs.fname.field   = $('#input-fname');
    inputs.lname.field   = $('#input-lname');
    inputs.contact.field = $('#input-contact');
    inputs.address.field = $('#input-address');

    inputs.impairment.field = getSelectedImpairmentOption(); //$('input[name="impairment_options"]:checked');

    inputs.email.field      = $('#input-email'); 
    inputs.username.field   = $('#input-username'); 
    inputs.newpassw.field   = $('#input-password');
    inputs.repassw.field    = $('#input-retype');

    const carouselElement = document.querySelector('#registration-carousel')

    carousel = new bootstrap.Carousel(carouselElement);
    // carouselElement.addEventListener('slide.bs.carousel', event => {
        
    // });

    $(".btn-next").on("click", function (e)
    {
        let targetPage = parseInt($(this).data('page-target'), 10) - 1;

        if (!validate(targetPage))
            return;

        carousel.to(targetPage);
    });

    $('#btn-submit').on('click', function()
    {
        if (!validate(3))
            return;

        if (inputs.newpassw.field.val() !== inputs.repassw.field.val())
        {
            AlertWarn("Your passwords didn't match. Please try again.");
            return;
        }

        handleSubmit();
    });

    $('#btn-back').on('click', promptCancelRegistration);
    prefillForTest();
});

function prefillForTest()
{
    inputs.fname.field.val('Katya');
    inputs.lname.field.val('Lel');
    inputs.contact.field.val('9111111111');
    inputs.address.field.val('#123 ABC Town');

    inputs.email.field.val('test@gmail.com'); 
    inputs.username.field.val('MoiMarmeladni'); 
    inputs.newpassw.field.val('12345678');
    inputs.repassw.field.val('12345678');

}

function cancelRegistration()
{
    if (window.RegistrationBridge) {
        window.RegistrationBridge.onCancelRegistration();
    }
}

function promptCancelRegistration()
{
    showConfirmPrompt('Are you sure, you want to cancel the registration?', {
        'onOK': function()
        {
            showLoading('action');
            cancelRegistration();
        }
    });
}

function validate(page)
{
    if (![LEARNER_REGISTRATION, TUTOR_REGISTRATION].includes(registrationType))
    {
        AlertWarn("Something went wrong while identifying your role. Please go back and restart the registration.");
        return false;
    }

    // Find all entries in the inputs object where page is equal to 'page'
    let targetFields = Object.fromEntries(
        Object.entries(inputs).filter( ([k,v]) => v.page === page)
    );

    for (const[k,v] of Object.entries(targetFields))
    {
        if (v.field.val().trim() === '')
        {
            AlertWarn(v.warning);
            return false;
        }
    }

    return true;
}

function handleSubmit()
{
    if (window.RegistrationBridge)
    {
        showLoading('action')

        const payload = {
            'role'      : registrationType,
            'firstname' : inputs.fname.field.val(),
            'lastname'  : inputs.lname.field.val(),
            'contact'   : inputs.contact.field.val(),
            'address'   : inputs.address.field.val(),
        
            'impairment': getSelectedImpairmentOption().val(),
        
            'email'     : inputs.email.field.val(),
            'username'  : inputs.username.field.val(),
            'password'  : inputs.newpassw.field.val(),
            'confirm'   : inputs.repassw.field.val(),
        };

        // Send to Android as a JSON string
        window.RegistrationBridge.onRegister(JSON.stringify(payload));
    }
}

function getSelectedImpairmentOption()
{
    return $('input[name="impairment_options"]:checked');
}

function setRegistrationMode(role)
{
    if ( ![TUTOR_REGISTRATION, LEARNER_REGISTRATION].includes(role) )
    {
        AlertWarn("Sorry, we ran into a technical issue. Don't worry, it's not you, it's on us.", 'Failure', {
            onOK: cancelRegistration
        });

        return;
    }

    registrationType = role;
    console.log(`Registration Mode set to: ${registrationType}`);
}