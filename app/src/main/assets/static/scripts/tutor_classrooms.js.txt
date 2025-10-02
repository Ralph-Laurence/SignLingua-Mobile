const classroomData = {
	"learnerClassrooms": [
		{
			"classroomName": "Learn German",
			"classroomTheme": "dark_slate_2",
			"classroomCode": "GER-03",
			"classroomUid": "079b97b8-1983-495a-905c-c5811b412b34",
			"classroomId": "yrdJGDWmK4",
			"totalLearners": 3
		}
	],
	"classroomThemes": {
		"dark_slate_0": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/dark_slate_0.png",
			"bgcolor": "#264C57"
		},
		"dark_slate_1": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/dark_slate_1.png",
			"bgcolor": "#264C57"
		},
		"dark_slate_2": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/dark_slate_2.png",
			"bgcolor": "#264C57"
		},
		"dark_slate_3": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/dark_slate_3.png",
			"bgcolor": "#264C57"
		},
		"dark_slate_4": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/dark_slate_4.png",
			"bgcolor": "#264C57"
		},
		"orange_0": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/orange_0.png",
			"bgcolor": "#F3B530"
		},
		"orange_1": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/orange_1.png",
			"bgcolor": "#F3B530"
		},
		"orange_2": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/orange_2.png",
			"bgcolor": "#F3B530"
		},
		"orange_3": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/orange_3.png",
			"bgcolor": "#F3B530"
		},
		"orange_4": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/orange_4.png",
			"bgcolor": "#F3B530"
		},
		"red_0": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/red_0.png",
			"bgcolor": "#D73A33"
		},
		"red_1": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/red_1.png",
			"bgcolor": "#D73A33"
		},
		"red_2": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/red_2.png",
			"bgcolor": "#D73A33"
		},
		"red_3": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/red_3.png",
			"bgcolor": "#D73A33"
		},
		"red_4": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/red_4.png",
			"bgcolor": "#D73A33"
		},
		"teal_0": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/teal_0.png",
			"bgcolor": "#2DC19F"
		},
		"teal_1": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/teal_1.png",
			"bgcolor": "#2DC19F"
		},
		"teal_2": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/teal_2.png",
			"bgcolor": "#2DC19F"
		},
		"teal_3": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/teal_3.png",
			"bgcolor": "#2DC19F"
		},
		"teal_4": {
			"icon": "http://localhost:8000/assets/img/classroom_icons/teal_4.png",
			"bgcolor": "#2DC19F"
		}
	}
};

// Array of possible descriptions
const descriptions = [
    "Classrooms allow you to host one-on-one video sessions with learners, offering them personalized learning and mentorship.",
    "Connect with your learners in a Classroom, where you can schedule one-on-one video sessions for focused learning and mentorship.",
    "In Classrooms, you can arrange one-on-one video sessions with your learners, ensuring personalized guidance and a structured learning experience.",
    "Classrooms give you direct access to your learners, offering personalized video sessions that support their learning style and preferences.",
    "You can schedule one-on-one video sessions, giving your learners a dedicated space for personalized learning and mentorship."
];

const classsroomItemTemplate = `
<div class="classroom-item d-flex align-items-center w-100 border shadow-sm mb-2 py-2">
    <div class="w-30 p-3 me-auto classroom-icon-wrapper overflow-hidden">
        <img src="#icon#" class="classroom-icon rounded-circle shadow">
    </div>
    <div class="w-70 ms-auto classroom-info-wrapper overflow-hidden pe-3">
        <h6 class="text-truncate w-100 mb-1">#code#</h6>
        <p class="w-100 mb-0 text-14 text-truncate mb-2">#name#</p>
        <div class="d-flex align-items-center justify-content-between opacity-80">
            <p class="text-14 m-0"><i class="fas fa-user me-1"></i>#total# Learners</p>
            <button 
                data-classroom-id="#classid#"
                class="btn btn-sm btn-link box-shadow-0 text-color-primary-700 text-decoration-none btn-view-classroom">
                View
            </button>
        </div>
    </div>
</div>`;

// Function to select a random description
function getRandomDescription() {
    const randomIndex = Math.floor(Math.random() * descriptions.length);
    return descriptions[randomIndex];
}

// Insert the random description into the target `<p>` element
document.addEventListener("DOMContentLoaded", () => {
    
    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    $('#btn-back').on("click", function () {

        if (window.TutorClassroomsJsBridge) {
            window.TutorClassroomsJsBridge.onGoBack();
        }
    });

    document.querySelector(".short-caption").textContent = getRandomDescription();
    //renderClassrooms();
});

function renderClassrooms(json)
{
    if (!json)
        return;

    let classroomData = JSON.parse(json);

    const { learnerClassrooms, classroomThemes } = classroomData;

    if (learnerClassrooms && Array.isArray(learnerClassrooms) && learnerClassrooms.length > 0)
    {
        learnerClassrooms.forEach(classroom => {
            let template = classsroomItemTemplate;
            let theme = classroomThemes[classroom.classroomTheme];
    
            template = template.replace('#icon#', theme.icon)
                .replace('#code#', classroom.classroomCode)
                .replace('#name#', classroom.classroomName)
                .replace('#total#', classroom.totalLearners)
                .replace('#classid#', classroom.classroomId);
    
            $('.dynamic-content').append(template);
        });

        $('.empty-classrooms-indicator').hide();
    }
    else
    {
        $('.empty-classrooms-indicator').show();
    }
}