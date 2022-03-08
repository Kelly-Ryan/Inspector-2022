package models;

public class SubmissionModel {
    private String moduleId, assignmentId, studentId, gradingRubric, marksReceived,comments;
    private double maxMarks, totalMarks;

    public SubmissionModel(String moduleId, String assignmentId, String studentId) {
        this.moduleId = moduleId;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        gradingRubric = null;
        marksReceived =  null;
        comments = null;
        maxMarks = 0.0;
        totalMarks = 0.0;
    }

    public SubmissionModel(String moduleId, String assignmentId, String studentId, String gradingRubric,
                           String marksReceived, String comments, double maxMarks, double totalMarks) {
        this.moduleId = moduleId;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.gradingRubric = gradingRubric;
        this.marksReceived = marksReceived;
        this.comments = comments;
        this.maxMarks = maxMarks;
        this.totalMarks = totalMarks;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getGradingRubric() {
        return gradingRubric;
    }

    public void setGradingRubric(String gradingRubric) {
        this.gradingRubric = gradingRubric;
    }

    public String getMarksReceived() {
        return marksReceived;
    }

    public void setMarksReceived(String marksReceived) {
        this.marksReceived = marksReceived;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
    }

    public double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(double totalMarks) {
        this.totalMarks = totalMarks;
    }
}
