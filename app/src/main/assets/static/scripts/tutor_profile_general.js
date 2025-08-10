let fields = {};
let quill = null;
let lockedAbout;

const aboutMaxLength = 2000;

window.addEventListener('DOMContentLoaded', function()
{
    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    var icons = Quill.import("ui/icons");

    icons["undo"] = 
    `<svg viewbox="0 0 18 18">
      <polygon class="ql-fill ql-stroke" points="6 10 4 12 2 10 6 10"></polygon>
      <path class="ql-stroke" d="M8.09,13.91A4.6,4.6,0,0,0,9,14,5,5,0,1,0,4,9"></path>
    </svg>`;

    icons["redo"] = 
    `<svg viewbox="0 0 18 18">
        <polygon class="ql-fill ql-stroke" points="12 10 14 12 16 10 12 10"></polygon>
        <path class="ql-stroke" d="M9.91,13.91A4.6,4.6,0,0,1,9,14a5,5,0,1,1,5-5"></path>
    </svg>`;

    quill = new Quill('#about-me', {
        modules: {
            toolbar: [
                ['bold', 'italic', 'underline', 'strike'],
                [{'list': 'ordered'}, {'list': 'bullet'}], //, {'list': 'check'}],
                //[{'script': 'sub'}, {'script': 'super'}],
                ['clean', 'undo', 'redo'],
            ],
            history: {
                delay: 1000,
                maxStack: 100,
                userOnly: true
            }
        },
        placeholder: 'Tell us more about you... Share your personality, interests, and what drives you. Go beyond professional achievements—include hobbies, interests, and life outside tutoring to provide a well-rounded view.',
        theme: 'snow',
    });

    // QuillJS:
    // quill.getLength() "Retrieves the length of the editor contents. Note even when Quill is empty,
    // there is still a blank line represented by '\n', so getLength will return 1."
    updateCharLengthCounter(quill.getLength()-1, aboutMaxLength, '#about-char-counter');

    quill.on('text-change', function(delta, oldDelta, source)
    {
        if (quill.getLength() > aboutMaxLength)
        {
            quill.deleteText(aboutMaxLength, quill.getLength());
        }

        updateCharLengthCounter(quill.getLength()-1, aboutMaxLength, '#about-char-counter');
    });

    $(document).on('click', '.ql-undo', () => triggerHistory('undo'))
               .on('click', '.ql-redo', () => triggerHistory('redo'));

    $('#btn-back').on("click", function () {

        if (window.TutorProfileGeneralJsBridge) {
            window.TutorProfileGeneralJsBridge.onGoBack();
        }
    });

    $('#btn-save').on('click', handleSaveProfile);

    $('#btn-close-notice').on('click', function() {
        $('#notice').fadeOut(300);
    });

    fields = {
        'fname' : { 'label': 'firstname',        'input' : $('#input-firstname')   },
        'lname' : { 'label': 'lastname',         'input' : $('#input-lastname')    },
        'phone' : { 'label': 'contact number',   'input' : $('#input-contact')     },
        'addrs' : { 'label': 'address',          'input' : $('#input-address')     },
    };

    fields.phone.input.on('input', function () {
        // Remove all non-digits
        this.value = this.value.replace(/[^0-9]/g, '');
    
        // Remove leading zero if present
        if (this.value && this.value.startsWith('0')) {
            this.value = this.value.replace(/^0+/, '');
        }
    });

    lockedAbout = $('#locked-input-about');
});

function triggerHistory(history)
{
    if (quill === null)
        return;

    if (history)
    {
        if (history === 'undo')
            quill.history.undo();

        else if (history === 'redo')
            quill.history.redo();
    }
}

function handleSaveProfile()
{
    // Validate all fields first
    for (const [key, field] of Object.entries(fields))
    {
        if (field.input.val().trim() === '')
        {
            AlertWarn(`Please add your ${field.label}.`, 'Hold on!', {
                'onOK': function() 
                {
                    setTimeout( () => field.input.focus(), 800 )
                }
            });

            return;
        }
    }

    // Assume all fields are valid
    if (window.TutorProfileGeneralJsBridge)
    {
        window.TutorProfileGeneralJsBridge.onEditGeneral(
            fields.fname.input.val(),
            fields.lname.input.val(),
            fields.phone.input.val(),
            fields.addrs.input.val(),
            quill.root.innerHTML
        );
    }

    showLoading('action');
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
    
    fields.fname.input.val(obj.firstname);
    fields.lname.input.val(obj.lastname);
    fields.phone.input.val(obj.contact);
    fields.addrs.input.val(obj.address);

    setAboutContent(obj.about);

    if (IS_FULLY_LOADED)
        hideLoading();
}

function updateCharLengthCounter(current, max, el)
{
    $(el).text(`${current}/${max}`);
}

function setAboutContent(dangerousContent)
{
    let sanitizedContent = DOMPurify.sanitize(dangerousContent);

    lockedAbout.html(sanitizedContent);
    quill.clipboard.dangerouslyPasteHTML(sanitizedContent);
}

function notifySuccessfulUpdate(updatedData)
{
    if (!updatedData)
        return;

    let obj = JSON.parse(updatedData);
    // let doc = obj.content;

    // enqueueDocumentaryItem(doc.from, doc.certification, doc.description, doc.docId);
    // bottomSheet.hide();

    let dangerousContent = quill.root.innerHTML;
    let sanitizedContent = DOMPurify.sanitize(dangerousContent);

    lockedAbout.html(sanitizedContent);
    
    setTimeout(() => ShowSnackbar(obj.message), 1200);
}

function notifyFailedUpdate(msg)
{
    // closeBottomSheet();
    setTimeout(() => AlertWarn(msg), 1200);
}