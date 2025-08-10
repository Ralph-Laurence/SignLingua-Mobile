package psu.signlinguamobile.api.apiresponse;

import java.util.List;
import java.util.Map;

import psu.signlinguamobile.models.ClassroomTheme;
import psu.signlinguamobile.models.ClassroomsData;

public class LearnerClassroomsResponse
{
    public List<ClassroomsData> tutorClassrooms;
    public Map<String, ClassroomTheme> classroomThemes;
}
