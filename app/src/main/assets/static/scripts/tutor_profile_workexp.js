let yearToday = new Date().getFullYear();
let inputRole;
let inputCompany;
let selectYearFrom;
let selectYearTo;
let bottomSheet;
let errorMessage;
let documentaryCollection;

window.addEventListener('DOMContentLoaded', function()
{
    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    $('#btn-back').on("click", function () {

        if (window.TutorProfileWorkExpJsBridge) {
            window.TutorProfileWorkExpJsBridge.onGoBack();
        }
    });

    documentaryCollection = $('#documentary-collection');

    selectYearFrom = buildYearSelect('#select-from-year');
    selectYearTo = buildYearSelect('#select-to-year');
    
    let modalElement = document.querySelector('#addWorkExpBottomSheet');
    bottomSheet = new bootstrap.Modal(modalElement);
    modalElement.addEventListener('hidden.bs.modal', cleanupBottomSheet);

    errorMessage = $('#error-message');

    inputCompany = $('#input-company');
    inputRole = $('#input-role');

    $('#btn-save').on('click', handleSave);

    $('#btn-close-notice').on('click', function() {
        $('#notice').fadeOut(300);
    });

    $(document).on('click', '.documentary-proof-item-delete', function()
    {
        let root    = $(this).closest('.documentary-proof-item');
        let docId   = root.data('doc-id');
        let company = root.find('.work-company').text();
        let role    = root.find('.work-role').text();

        showConfirmPrompt(`Are you sure you want to remove your work experience "<b>${role}</b>" from "<b>${company}</b>"?`, {
            'onOK': function()
            {
                showLoading('action');

                if (window.TutorProfileWorkExpJsBridge) {
                    window.TutorProfileWorkExpJsBridge.onRemoveWorkExp(docId);
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
    if (inputCompany.val().trim() === '')
    {
        showError('Please enter the name of the company you worked for.');
        inputCompany.focus();
        return;
    }

    if (inputRole.val().trim() === '')
    {
        showError('Please provide a valid job title.');
        inputRole.focus();
        return;
    }

    let val_yearTo   = parseInt(selectYearTo.val(), 10);
    let val_yearFrom = parseInt(selectYearFrom.val(), 10);
    
    if (val_yearFrom > val_yearTo)
    {
        showError("The start year cannot be later than the end year.");
        return;
    }    

    if (window.TutorProfileWorkExpJsBridge)
    {
        window.TutorProfileWorkExpJsBridge.onEditWorkExp(
            inputCompany.val(),
            inputRole.val(),
            val_yearFrom,
            val_yearTo
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
    inputCompany.val('');
    inputRole.val('');
    selectYearFrom.val(yearToday).selectmenu('refresh');
    selectYearTo.val(yearToday).selectmenu('refresh');
}


function enqueueDocumentaryItem(duration, company, role, docId)
{
    let template = `
    <div class="w-100 d-flex gap-2 border mb-2 px-2 documentary-proof-item" data-doc-id="${docId}">
        <div class="w-20 me-auto p-3">
            <h6 class="text-muted text-14">${duration}</h6>
        </div>
        <div class="flex-fill py-3">
            <h6 class="text-14 flex-fill work-company">${company}</h6>
            <p class="text-13 m-0 opacity-60 work-role">${role}</p>
        </div>
        <div class="w-10 ms-auto py-2">
            <button class="btn btn-sm ms-auto documentary-proof-item-delete">
                <i class="fas fa-close text-20 text-muted"></i>
            </button>
        </div>
    </div>`;

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

    documentaryCollection.html('');
    
    for (let doc of obj.workExpDocuments)
    {
        enqueueDocumentaryItem(doc.duration, doc.company, doc.role, doc.docId);
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

    enqueueDocumentaryItem(doc.duration, doc.company, doc.role, doc.docId);
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

    let obj  = JSON.parse(updatedData);
    let work = {'workExpDocuments' : obj.content.workExpDocuments};

    renderDetails(JSON.stringify(work));

    setTimeout(() => ShowSnackbar(obj.message), 1200);
}

function notifyFailedDelete(msg)
{
    setTimeout(() => AlertWarn(msg), 1200);
}