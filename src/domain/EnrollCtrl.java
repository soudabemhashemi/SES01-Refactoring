package domain;

import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
	public void enroll(Student s, List<CSE> courses) throws EnrollmentRulesViolationException {
        Map<Term, Map<Course, Double>> transcript = s.getTranscript();
        for (CSE o : courses) {
            for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
                for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                    if (r.getKey().equals(o.getCourse()) && r.getValue() >= 10)
                        throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", o.getCourse().getName()));
                }
            }
        }
        for (CSE o : courses) {
            List<Course> prereqs = o.getCourse().getPrerequisites();
            nextPre:
            for (Course pre : prereqs) {
                for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
                    for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                        if (r.getKey().equals(pre) && r.getValue() >= 10)
                            continue nextPre;
                    }
                }
                throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), o.getCourse().getName()));
            }
        }

        checkConflictExamDate(courses);
        checkDuplicateCourse(courses);
        checkGPALimit(courses, s);
    }

    public void checkGPALimit(List<CSE> courses, Student s) throws EnrollmentRulesViolationException {
        int unitsRequested = courses.stream().mapToInt(o -> o.getCourse().getUnits()).sum();
        if ((s.getGPA() < 12 && unitsRequested > 14) ||
                (s.getGPA() < 16 && unitsRequested > 16) ||
                (unitsRequested > 20))
            throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, s.getGPA()));
        for (CSE o : courses)
            s.takeCourse(o.getCourse(), o.getSection());
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

}

