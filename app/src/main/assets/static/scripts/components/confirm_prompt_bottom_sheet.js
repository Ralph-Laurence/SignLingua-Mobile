const confirmPromptTemplate =
`<div class="modal fade bottom-sheet" id="confirmPromptBottomSheet" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-bottom-sheet sm pt-5">
        <div class="modal-content modal-dialog-scrollable">
            <div class="modal-header">
                <div class="d-flex align-items-center gap-2">
                    <img src="../static/images/logo-sm.png" width="24" height="24">
                    <h6 class="modal-title mb-0" id="confirm-prompt-title"></h6>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body pb-4">
                <div class="text-14 w-100 bg-light rounded p-2 mb-3" id="confirm-prompt-message"></div>
                <div class="d-flex w-100 align-items-center justify-content-end gap-2">
                    <button id="btn-confirm-cancel" type="button" class="btn secondary-button px-3" data-bs-dismiss="modal">
                        Cancel
                    </button>
                    <button id="btn-confirm-ok" type="button" class="btn primary-button px-4" data-bs-dismiss="modal">
                        OK
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>`;

let confirmPromptEl;
let confirmPrompt = null;
let confirmPromptOkButton;
let confirmPromptCancelButton;

window.addEventListener('DOMContentLoaded', function() {

    initConfirmPrompt();
});

function initConfirmPrompt()
{
    // Create the jQuery element first
    confirmPromptEl = $(confirmPromptTemplate);

    // Now check if it's already in the DOM.
    // If not, then add it
    if ($('#confirmPromptBottomSheet').length === 0)
        $('body').prepend(confirmPromptEl);

    // Then cache button references
    confirmPromptOkButton = confirmPromptEl.find('#btn-confirm-ok');
    confirmPromptCancelButton = confirmPromptEl.find('#btn-confirm-cancel');

    // Initialize Bootstrap modal
    if (confirmPrompt !== null) return;

    confirmPrompt = new bootstrap.Modal(confirmPromptEl);
}

function showConfirmPrompt(msg, options = {})
{
    if (confirmPrompt === null)
        initConfirmPrompt();

    // Sanitize and inject the message
    confirmPromptEl.find("#confirm-prompt-message").html(DOMPurify.sanitize(msg));

    // Resolve and apply modal title
    let modalTitle = 'Confirmation';
    if ('title' in options && typeof options.title === 'string' && options.title.length > 0)
        modalTitle = options.title;

    confirmPromptEl.find("#confirm-prompt-title").text(modalTitle);

    // Unbind previous button clicks
    confirmPromptCancelButton.off('click');
    confirmPromptOkButton.off('click');

    // Bind new button callbacks
    if ('onCancel' in options && typeof options.onCancel === 'function') {
        confirmPromptCancelButton.on('click', () => options.onCancel());
    }

    if ('onOK' in options && typeof options.onOK === 'function') {
        confirmPromptOkButton.on('click', () => options.onOK());
    }

    // Apply button texts
    if ('okText' in options && typeof options.okText === 'string')
        confirmPromptOkButton.text(options.okText)
    else
        confirmPromptOkButton.text("OK");

    if ('cancelText' in options && typeof options.cancelText === 'string')
        confirmPromptCancelButton.text(options.cancelText)
    else
        confirmPromptCancelButton.text("Cancel");


    // Optional cleanup after modal closes (just in case)
    confirmPromptEl.off('hidden.bs.modal').on('hidden.bs.modal', () => {
        confirmPromptCancelButton.off('click');
        confirmPromptOkButton.off('click');
    });

    // Show the modal
    confirmPrompt.show();
}