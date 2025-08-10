let $snackbarTemplate = $(`
<div class="w-100 position-fixed text-13 text-white start-0 end-0 px-4 py-3" id="success-snackbar">
    Success
</div>`);

$(document).ready(function(){

    // Insert the snackbar just before the very first div on the bottom of the page
    $('#jquery-script').before($snackbarTemplate);

});

function ShowSnackbar(msg)
{
    if (msg)
    {
        $snackbarTemplate
            .html(msg)
            .css({bottom: '-55px', display: 'block'})
            .animate({ bottom: '0' }, 400, function()
            {
                setTimeout(HideSnackbar, 3200);
            });
    }
}

function HideSnackbar()
{
    $snackbarTemplate
        .html('')
        .animate({ bottom: '-55px' }, 400, function()
        {
            $snackbarTemplate.css({bottom: '-55px', display: 'none'})
        });
}