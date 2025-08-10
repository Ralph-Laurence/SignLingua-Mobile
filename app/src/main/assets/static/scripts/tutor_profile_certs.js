let yearToday = new Date().getFullYear();
let inputDescr;
let inputTitle;
let selectYearFrom;
let bottomSheet;
let errorMessage;
let documentaryCollection;

let certificationsQueue = [];

window.addEventListener('DOMContentLoaded', function()
{
    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    $('#btn-back').on("click", function () {

        if (window.TutorProfileCertsJsBridge) {
            window.TutorProfileCertsJsBridge.onGoBack();
        }
    });

    documentaryCollection = $('#documentary-collection');

    selectYearFrom = buildYearSelect('#select-from-year');
    
    let modalElement = document.querySelector('#addCertBottomSheet');
    bottomSheet = new bootstrap.Modal(modalElement);
    modalElement.addEventListener('hidden.bs.modal', cleanupBottomSheet);

    errorMessage = $('#error-message');

    inputTitle = $('#input-title');
    inputDescr = $('#input-desc');

    $('#btn-save').on('click', handleSave);

    $('#btn-close-notice').on('click', function() {
        $('#notice').fadeOut(300);
    });

    $(document).on('click', '.documentary-proof-item-delete', function()
    {
        let root  = $(this).closest('.documentary-proof-item');
        let docId = root.data('doc-id');
        let title = root.find('.cert-title').text();

        showConfirmPrompt(`Are you sure, you want to remove your certification "<b>${title}</b>"?`, {
            'onOK': function()
            {
                showLoading('action');

                if (window.TutorProfileCertsJsBridge) {
                    window.TutorProfileCertsJsBridge.onRemoveCertification(docId);
                }
            }
        });
    });
});

function buildYearSelect(selector)
{
    let select = $(selector);

    for (let i = yearToday; i >= 1980; i--)
        select.append(`<option value="${i}">${i}</option>`);

    select.selectmenu({ width: 150 });

    return select;
}

function handleSave()
{
    if (inputTitle.val().trim() === '')
    {
        showError('Please enter the certification title or the provider.');
        inputTitle.focus();
        return;
    }

    if (inputDescr.val().trim() === '')
    {
        showError('Please describe what was the certification about.');
        inputDescr.focus();
        return;
    }

    if (window.TutorProfileCertsJsBridge)
    {
        window.TutorProfileCertsJsBridge.onEditCertification(
            selectYearFrom.val(),
            inputTitle.val(),
            inputDescr.val()
        );
    }

    hideError();
    showLoading('action');
}

function showError(err)
{
    errorMessage.text(err);
    errorMessage.show();
}

function hideError()
{
    errorMessage.hide();
    errorMessage.text('');
}

function closeBottomSheet()
{
    bottomSheet.hide();
    cleanupBottomSheet();
}

function cleanupBottomSheet()
{
    hideError();
    inputTitle.val('');
    inputDescr.val('');
    selectYearFrom.val(yearToday).selectmenu('refresh');
}

function enqueueDocumentaryItem(from, certification, description, docId)
{
    certificationsQueue.push({
        "docId":            docId,
        "from":             from,
        "certification":    certification,
        "description":      description,
    });

    let template = `
    <div class="w-100 d-flex gap-2 border mb-2 px-2 documentary-proof-item" data-doc-id="${docId}">
        <div class="w-20 me-auto p-3">
            <h6 class="text-muted text-14">${from}</h6>
        </div>
        <div class="flex-fill py-3">
            <h6 class="text-14 flex-fill cert-title">${certification}</h6>
            <p class="text-13 m-0 opacity-60">${description}</p>
        </div>
        <div class="w-10 ms-auto py-2">
            <button class="btn btn-sm ms-auto documentary-proof-item-delete">
                <i class="fas fa-close text-20 text-muted"></i>
            </button>
        </div>
    </div>`;

    console.log('Enqueue Item with id: ' + docId);
    documentaryCollection.append(template);
}

function renderDetails(json)
{
    if (!json)
        return;

    // If the page is fully loaded, it closes the loader automatically during first load
    // Now if we want to do long running action, we can manually show the loader
    // then hide it manually later on
    if (IS_FULLY_LOADED)
        showLoading();

    let obj = JSON.parse(json);
    
    certificationsQueue = [];
    documentaryCollection.html('');
    
    for (let doc of obj.certificationDocuments)
    {
        enqueueDocumentaryItem(doc.from, doc.certification, doc.description, doc.docId);
    }

    if (IS_FULLY_LOADED)
        hideLoading();
}

function notifySuccessfulUpdate(updatedData)
{
    if (!updatedData)
        return;

    let obj = JSON.parse(updatedData);
    let doc = obj.content;

    enqueueDocumentaryItem(doc.from, doc.certification, doc.description, doc.docId);
    bottomSheet.hide();

    setTimeout(() => ShowSnackbar(obj.message), 1200);
}

function notifyFailedUpdate(msg)
{
    closeBottomSheet();
    setTimeout(() => AlertWarn(msg), 1200);
}

function notifySuccessfulDelete(updatedData)
{
    if (!updatedData)
        return;

    let obj   = JSON.parse(updatedData);
    let certs = {'certificationDocuments' : obj.content.certificationDocuments};

    renderDetails(JSON.stringify(certs));

    setTimeout(() => ShowSnackbar(obj.message), 1200);
}

function notifyFailedDelete(msg)
{
    setTimeout(() => AlertWarn(msg), 1200);
}