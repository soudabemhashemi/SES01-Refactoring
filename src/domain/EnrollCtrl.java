package domain;

import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
	public void enroll(Student s, List<CSE> courses) throws EnrollmentRulesViolationException {
        for (CSE o : courses) {
            if (s.hasPassed(o.getCourse()))
                throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", o.getCourse().getName()));
        }
        checkPassedPrerequisites(courses, s);

        checkConflictExamDate(courses);
        checkDuplicateCourse(courses);
        checkGPALimit(courses, s);

        for (CSE o : courses)
            s.takeCourse(o.getCourse(), o.getSection());
    }

    public void checkGPALimit(List<CSE> courses, Student s) throws EnrollmentRulesViolationException {
        int unitsRequested = courses.stream().mapToInt(o -> o.getCourse().getUnits()).sum();
        if ((s.getGPA() < 12 && unitsRequested > 14) ||
                (s.getGPA() < 16 && unitsRequested > 16) ||
                (unitsRequested > 20))
            throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, s.getGPA()));
    }

    public void checkDuplicateCourse(List<CSE> courses) throws EnrollmentRulesViolationException {
        for (CSE o : courses) {
            for (CSE o2 : courses) {
                if (o == o2)
                    continue;
                if (o.getCourse().equals(o2.getCourse()))
                    throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", o.getCourse().getName()));
            }
        }
    }

    public void checkConflictExamDate(List<CSE> courses) throws EnrollmentRulesViolationException {
        for (CSE o : courses) {
            for (CSE o2 : courses) {
                if (o == o2)
                    continue;
                if (o.getExamTime().equals(o2.getExamTime()))
                    throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", o, o2));
            }
        }
    }

    private void checkPassedPrerequisites(List<CSE> courses, Student s) throws EnrollmentRulesViolationException {
        for (CSE o : courses) {
            List<Course> prereqs = o.getCourse().getPrerequisites();
            for (Course pre : prereqs) {
                if (!s.hasPassed(pre)) {
                    throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), o.getCourse().getName()));
                }
            }
        }
    }

}

