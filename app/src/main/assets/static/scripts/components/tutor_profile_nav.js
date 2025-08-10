let $templateWrapper = $(`
    <div>
        <button class="btn btn-light w-100 text-start" type="button" data-bs-toggle="dropdown"
            aria-expanded="false">
            <div class="w-100 d-flex align-items-center text-16 py-1">
                <span class="me-auto current-nav-label"></span>
                <span class="ms-auto"><i class="fas fa-chevron-down"></i></span>
            </div>
        </button>
        <ul class="dropdown-menu"></ul>
    </div>
`);

const navItems = {
    acc:   { id: 'option-manage-account',   label: 'User Account'           , 'jsBridge' : 'onNavUserAccount' },
    cert:  { id: 'option-manage-certs',     label: 'Certifications'         , 'jsBridge' : 'onNavCertifications' },
    educ:  { id: 'option-manage-education', label: 'Educational Background' , 'jsBridge' : 'onNavEducationalBackground' },
    genrl: { id: 'option-manage-general',   label: 'General Information'    , 'jsBridge' : 'onNavGeneralInformation' },
    passw: { id: 'option-manage-password',  label: 'Password & Security'    , 'jsBridge' : 'onNavPasswordSecurity' },
    skl:   { id: 'option-manage-skills',    label: 'Skills & Accessibility' , 'jsBridge' : 'onNavSkillsAccessibility' },
    work:  { id: 'option-manage-workexp',   label: 'Work Experience'        , 'jsBridge' : 'onNavWorkExperience' },
};

$(document).ready(function ()
{
    let currentNav = $('meta[name="navpage"]').attr('content');
    let items = '';

    for (const [k, v] of Object.entries(navItems))
    {
        if (k === currentNav) {
            items += `<li><span id="${v.id}" class="dropdown-item active" data-js-bridge="${v.jsBridge}">${v.label}</span></li>`;
            $templateWrapper.find('.current-nav-label').text(v.label);
        } else {
            items += `<li><button id="${v.id}" class="dropdown-item tutor-profile-nav-item" data-js-bridge="${v.jsBridge}">${v.label}</button></li>`;
        }
    }

    $templateWrapper.find('.dropdown-menu').html(items);

    // 💡 Append the contents (not the wrapper) to #navmenu
    $('#navmenu').append($templateWrapper.children());

    $(document).on('click', '.tutor-profile-nav-item', function(e)
    {
        let bridgeName = $(this).data('js-bridge');

        if (window.TutorProfileNavJsBridge
            && bridgeName
            && typeof window.TutorProfileNavJsBridge[bridgeName] === 'function'
        )
        {
            window.TutorProfileNavJsBridge[bridgeName]();
        }
    });
});
