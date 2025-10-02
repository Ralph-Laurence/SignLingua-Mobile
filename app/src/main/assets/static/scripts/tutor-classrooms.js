const descriptions = [
    "Classrooms allow you to host one-on-one video sessions with learners, offering them personalized learning and mentorship.",
    "Connect with your learners in a Classroom, where you can schedule one-on-one video sessions for focused learning and mentorship.",
    "In Classrooms, you can arrange one-on-one video sessions with your learners, ensuring personalized guidance and a structured learning experience.",
    "Classrooms give you direct access to your learners, offering personalized video sessions that support their learning style and preferences.",
    "You can schedule one-on-one video sessions, giving your learners a dedicated space for personalized learning and mentorship."
];

/*
const sampleData = 
`{
    "content": [
        {
            "classroomId": "oXA2X429qv",
            "classroomName": "Russian Sign Language",
            "classroomCode": "RUS-001",
            "totalLearners": 0
        },
        {
            "classroomId": "kD0JDv19PY",
            "classroomName": "British BSL",
            "classroomCode": "BSL-09",
            "totalLearners": 0
        },
        {
            "classroomId": "yrdJGDWmK4",
            "classroomName": "Deutsche German",
            "classroomCode": "GER-03",
            "totalLearners": 3
        }
    ]
}`;*/

const template = `<div class="card w-100 classroom-card #classroom_card_icon# mb-2">
                    <div class="card-body">
                        <div class="d-flex w-100">
                            <div class="flex-fill overflow-hidden pt-2">
                                <h5 class="w-100 mx-0 mt-0 mb-2 text-truncate classroom-name">#classroom_name#</h5>
                                <p class="w-100 mx-0 mt-0 mb-4 text-truncate classroom-code">#classroom_code#</p>
                                <p class="w-100 m-0 text-truncate total-learners">#total_learners#</p>
                            </div>
                            <div class="option-menu-container ms-auto">
                                <button class="btn text-white ms-2">
                                    <i class="fas fa-ellipsis-vertical"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>`;

// Function to select a random description
function getRandomDescription() {
    const randomIndex = Math.floor(Math.random() * descriptions.length);
    return descriptions[randomIndex];
}

window.addEventListener('DOMContentLoaded', function()
{
    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    $('#classroom-random-hints').text(getRandomDescription());

    $('#btn-back').on("click", function () {

        if (window.TutorClassroomsJsBridge) {
            window.TutorClassroomsJsBridge.onGoBack();
        }
    });

    // renderDetails(sampleData);
});

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

    $('#list-classrooms .main-content').html('');
    
    for (let row of obj.content)
    {
        let templateCopy = template.replace('#classroom_name#', row.classroomName)
            .replace('#classroom_code#', row.classroomCode)
            .replace('#total_learners#', `${row.totalLearners} Learners`);

        if (row.classroomName && isAlphaNum(row.classroomName))
        {
            let iconName = row.classroomName.trim().charAt(0);
            let iconClass = (`classroom-card-icn-${iconName}`).toLowerCase();
            templateCopy = templateCopy.replace('#classroom_card_icon#', iconClass);
        }            

        $('#list-classrooms .main-content').append(templateCopy);
    }

    if (IS_FULLY_LOADED)
        hideLoading();
}

function isAlphaNum(str) {
    return str.length > 0 && /^[a-zA-Z0-9]/.test(str);
}
  