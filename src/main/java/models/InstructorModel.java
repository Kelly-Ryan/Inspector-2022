package models;

public class InstructorModel {
    private final int instructorId;
    private final String name, email, importDirectory, resultsDirectory;
    private String lastUsedRubric;

    //called when existing user logs in
    public InstructorModel(int instructorId, String name, String email, String importDirectory, String resultsDirectory,
                           String lastUsedRubric) {
        this.instructorId = instructorId;
        this.name = name;
        this.email = email;
        this.importDirectory = importDirectory;
        this.resultsDirectory = resultsDirectory;
        this.lastUsedRubric = lastUsedRubric;
    }

    public String getInstructorId() {
        return Integer.toString(instructorId);
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getImportDirectory() {
        return importDirectory;
    }

    public String getResultsDirectory() {
        return resultsDirectory;
    }

    public void setLastUsedRubric(String lastUsedRubric) {
        this.lastUsedRubric = lastUsedRubric;
    }

    public String getLastUsedRubric() {
        return lastUsedRubric;
    }
}
