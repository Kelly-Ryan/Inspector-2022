package models;

public class ModuleModel {
    private int moduleId, instructorId;
    private String moduleCode, moduleName;

    public ModuleModel(int moduleId, int instructorId, String moduleCode, String moduleName) {
        this.moduleId = moduleId;
        this.instructorId = instructorId;
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public int getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(int instructorId) {
        this.instructorId = instructorId;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public String toString() {
        return "ModuleModel{" +
                "moduleId=" + moduleId +
                ", instructorId=" + instructorId +
                ", moduleCode='" + moduleCode + '\'' +
                ", moduleName='" + moduleName + '\'' +
                '}';
    }
}
