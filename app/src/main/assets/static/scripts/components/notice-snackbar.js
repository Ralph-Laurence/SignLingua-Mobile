/**
 * This script must be placed after jquery script tag.
 * This prepends itself above the jquery script tag
 * and assumes the jquery script to be the very first
 * tag from the scripts section
 */

let $btnCloseNoticeTemplate = $(`    
    <div class="w-100 position-fixed bg-primary-900 text-13 text-white bottom-0 start-0 end-0 px-4 py-3 shadow-sm" id="notice">
        <div class="d-flex w-100 align-items-center">
            <div class="ms-start flex-fill">Don't forget to save your changes.</div>
            <button id="btn-close-notice" type="button" class="btn secondary-button small-button me-auto px-3">
                OK
            </button>
        </div>
    </div>`);

$(document).ready(function()
{
    // Insert the snackbar just before the very first div on the bottom of the page
    $('#jquery-script').before($btnCloseNoticeTemplate);

    $btnCloseNoticeTemplate.on('click', function ()
    {
        // Dismiss the bottom reminder
        $('#notice').fadeOut(300);

        const saveBtn = document.getElementById('btn-save');

        if (!saveBtn)
            return;

        // Scroll smoothly to the "Save" button
        saveBtn.scrollIntoView({ behavior: 'smooth', block: 'center' });

        // Sequence: After scroll → pulse → bind click logic
        setTimeout(() => {
            
            // Restart pulse animation
            saveBtn.classList.remove('pulse-3x');
            void saveBtn.offsetWidth;
            saveBtn.classList.add('pulse-3x');

        }, 600);
    });
})