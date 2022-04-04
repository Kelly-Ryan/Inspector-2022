package models;

import java.util.List;

public class InstructorModel {
    private final int instructorId;
    private final String name, email, importDirectory;
    private List<Module> modules;

    //called when existing user logs in
    public InstructorModel(int instructorId, String name, String email, String importDirectory) {
        this.instructorId = instructorId;
        this.name = name;
        this.email = email;
        this.importDirectory = importDirectory;
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

    public void addModule(String moduleCode) {

    }

    public List<Module> getModules() {
        return modules;
    }
}
