package domain;

import java.util.List;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
	public void enroll(Student s, List<Offering> offerings) throws EnrollmentRulesViolationException {
        checkDuplicatePassedCourse(offerings, s);
        checkPassedPrerequisites(offerings, s);
        checkConflictExamDate(offerings);
        checkDuplicateCourse(offerings);
        checkGPALimit(offerings, s);

        for (Offering o : offerings)
            s.takeCourse(o.getCourse(), o.getSection());
    }

    public void checkGPALimit(List<Offering> courses, Student s) throws EnrollmentRulesViolationException {
        int unitsRequested = courses.stream().mapToInt(o -> o.getCourse().getUnits()).sum();
        if ((s.getGPA() < 12 && unitsRequested > 14) ||
                (s.getGPA() < 16 && unitsRequested > 16) ||
                (unitsRequested > 20))
            throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, s.getGPA()));
    }

    public void checkDuplicateCourse(List<Offering> courses) throws EnrollmentRulesViolationException {
        for (Offering o : courses) {
            for (Offering o2 : courses) {
                if (o == o2)
                    continue;
                if (o.isSameCourse(o2))
                    throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", o.getCourse().getName()));
            }
        }
    }

    public void checkConflictExamDate(List<Offering> courses) throws EnrollmentRulesViolationException {
        for (Offering o : courses) {
            for (Offering o2 : courses) {
                if (o == o2)
                    continue;
                if (o.hasExamTimeConflict(o2))
                    throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", o, o2));
            }
        }
    }

    private void checkPassedPrerequisites(List<Offering> courses, Student s) throws EnrollmentRulesViolationException {
        for (Offering o : courses) {
            List<Course> prereqs = o.getCourse().getPrerequisites();
            for (Course pre : prereqs) {
                if (!s.hasPassed(pre)) {
                    throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), o.getCourse().getName()));
                }
            }
        }
    }

    public void checkDuplicatePassedCourse(List<Offering> courses, Student s) throws EnrollmentRulesViolationException {
        for (Offering o : courses) {
            if (s.hasPassed(o.getCourse()))
                throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", o.getCourse().getName()));
        }
    }

}

