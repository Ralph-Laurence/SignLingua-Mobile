let modalTemplate = `
<!-- WARNING MODAL -->
<div class="modal" tabindex="-1" id="modal-err" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <div class="d-flex align-items-center">
                    <img src="../static/images/modal_icn_warning.png" class="me-2" width="32" height="32">
                    <h6 class="modal-title m-0">Oops!</h6>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <p>Modal body text goes here.</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn primary-button px-3 alert-modal-btn-ok">OK</button>
            </div>
        </div>
    </div>
</div>`;

let $modalErr;
let $modalElement;
let $modalElementBtnOK;

$(document).ready(() => initModalErr());

function initModalErr()
{
    // Check if modal is not yet injected, then inject it
    if (!$('#modal-err').length) {
        $('body').prepend(modalTemplate);
    }    
    
    $modalElement = $('#modal-err');
    $modalErr = new bootstrap.Modal($modalElement);

    $modalElementBtnOK = $modalElement.find('.alert-modal-btn-ok');
}

function AlertWarn(message, title = 'Oops!', options = {})
{
    if ($modalErr === null) return;

    $modalElementBtnOK.off('click').on('click', () => {

        if ('onOK' in options && typeof options.onOK === 'function') {
            options.onOK();
        }

        $modalErr.hide(); // manually close the modal
    });

    // Handle cases when the dialog was closed.
    if ('onClosed' in options && typeof options.onClosed === 'function') {
        options.onClosed();
    }

    $modalElement.find('.modal-title').text(title);
    $modalElement.find('.modal-body').html(message);

    $modalElement.off('hidden.bs.modal').on('hidden.bs.modal', () => {
        $modalElementBtnOK.off('click');
    });

    $modalErr.show();
}
