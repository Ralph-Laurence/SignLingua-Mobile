const skills = {
    '0': 'Accepting Criticism',
    '1': 'Adaptability',
    '2': 'Analytical Thinking',
    '3': 'Assertiveness',
    '4': 'Attitude',
    '5': 'Communication',
    '6': 'Confidence',
    '7': 'Creative Thinking',
    '8': 'Critical Thinking',
    '9': 'Decision Making',
    '10': 'Discipline',
    '11': 'Empathy',
    '12': 'Flexibility',
    '13': 'Innovation',
    '14': 'Listening',
    '15': 'Negotiation',
    '16': 'Organization',
    '17': 'Persuasion',
    '18': 'Problem Solving',
    '19': 'Responsibility',
    '20': 'Self Assessment',
    '21': 'Self Management',
    '22': 'Stress Management',
    '23': 'Team Building',
    '24': 'Tolerance',
    '25': 'Time Management',
    '26': 'Willing to Learn',
};

let skillPickerTbody;
let skillsContainer;
let btnModifySkill;
let disability = 0;
let profileSkills = [

    // The skills selected from the database can go here too

    // Selected profile skills will go here after selecting from the picker.
];

window.addEventListener('DOMContentLoaded', function()
{
    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    skillPickerTbody = $('#skill-picker-body');
    skillsContainer  = $('#skills-container');
    btnModifySkill   = $('#btn-modify-skill');
    
    let modalElement = document.querySelector('#skillsPickerModal');
    let modal = new bootstrap.Modal(modalElement);
    
    // Check all checkboxes that are intentionally selected (via OK)
    // then uncheck those selected but canceled
    modalElement.addEventListener('show.bs.modal', function()
    {
        if (typeof profileSkills === 'object')
        {
            //profileSkills.forEach(skillId => {
            for (const [skillId, label] of Object.entries(skills))
            {
                let checkState = false;

                if (profileSkills.includes(skillId))
                    checkState = true;

                $(`.skill-checkbox[value="${skillId}"]`).prop('checked', checkState);
            }
        }
    });

    $('.disability-option').on('change', function(e) {
        let impairment = $(this).val();

        disability = impairment;
        console.log(`Disability (${typeof(disability)}): ${disability}`);
    });

    $('#btn-ok-skills-picker').on('click', collectSelectedSkills);
    $('#btn-cancel-skills-picker').on('click', modal.hide());

    $('#btn-back').on("click", function () {

        if (window.TutorProfileSkillsJsBridge) {
            window.TutorProfileSkillsJsBridge.onGoBack();
        }
    });

    $('#btn-close-notice').on('click', function() {
        $('#notice').fadeOut(300);
    });

    $('#btn-save').on('click', handleSave);

    populateSkillsSelector(); 
});

function collectSelectedSkills()
{
    // $('.skill-checkbox:checked').each(function() {
    //     console.log(this.value);
    // });

    /*
    let selectedSkills = {};

    $.each($('.skill-checkbox:checked'), (idx, obj) => {
        selectedSkills[obj.value] = obj.dataset.label;
    });
    */

    let selectedSkills = [];

    $.each($('.skill-checkbox:checked'), (idx, obj) => {
        selectedSkills.push(obj.value);
    });

    enqueueSkills(selectedSkills);
}

function populateSkillsSelector()
{
    const colsLength = 2;
    let tr;

    let index = 0;

    for (const [key, value] of Object.entries(skills))
    {
        if (index % colsLength === 0) {
            tr = $('<tr>');
            skillPickerTbody.append(tr); // Append the row immediately for cleaner logic
        }

        tr.append(`
            <td>
                <div class="form-check">
                    <input class="form-check-input skill-checkbox" type="checkbox" value="${key}" data-label="${value}" id="skill_${key}">
                    <label class="form-check-label text-13" for="skill_${key}">
                        ${value}
                    </label>
                </div>
            </td>`);

        index++;
    }

    // Optionally: Fill the last row with empty <td>s if it's incomplete
    const remainingCells = Object.keys(skills).length % colsLength;
    if (remainingCells !== 0) {
        for (let i = 0; i < colsLength - remainingCells; i++) {
            tr.append($('<td>'));
        }
    }
}

function enqueueSkills(targetSkills)
{
    $('.skill-checkbox').prop('checked', false); // clear all checked skills

    let template = "";
    profileSkills = [];

    //for (const [key, value] of Object.entries(skills))
    targetSkills.forEach(skillId =>
    {
        let skillLabel = skills[skillId];

        template += `<span class="badge text-dark px-3 py-2 bg-light border rounded-2rem">${skillLabel}</span>`;
        profileSkills.push( String(skillId) );

        $(`.skill-checkbox[value="${skillId}"]`).prop('checked', true);
    });

    if (typeof profileSkills === 'object' && profileSkills.length > 0)
    {
        skillsContainer.html(template);
        btnModifySkill.find('.button-label').html('<i class="fas fa-pen me-1"></i>Edit Skills');
    }
    else
    {
        skillsContainer.html('');
        btnModifySkill.find('.button-label').html('<i class="fas fa-plus me-1"></i>Add Skills');
    }
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

    enqueueSkills(obj.skillAndAccessibility.skills);

    disability = obj.skillAndAccessibility.disability;
    console.log(`Recieved: (${typeof obj.skillAndAccessibility.skills}) ${obj.skillAndAccessibility.skills} >>> (${typeof disability}) ${disability}`)
    $(`.disability-option[value="${disability}"`).prop('checked', true);

    if (IS_FULLY_LOADED)
        hideLoading();
}

function handleSave()
{
    // console.log(`Disability (${typeof(disability)}): ${disability} --> ProfileSkills (${typeof(profileSkills)}): ${profileSkills}`);
    if (window.TutorProfileSkillsJsBridge) {
        let disabilityVal = parseInt(disability) || 0;
        console.log(`Saving::>> disability: ${typeof disabilityVal} = ${disability}`)
        window.TutorProfileSkillsJsBridge.onEditSkills(disabilityVal, profileSkills);
    }

    showLoading('action');
}
// function showError(err)
// {
//     errorMessage.text(err);
//     errorMessage.show();
// }

// function hideError()
// {
//     errorMessage.hide();
//     errorMessage.text('');
// }
