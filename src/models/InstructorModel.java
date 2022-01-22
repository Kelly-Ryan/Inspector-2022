package models;

public class InstructorModel {
    private int instructorId;
    private String name, email, password;

    public InstructorModel() {
    }

    public InstructorModel(int instructorId, String name, String email, String password) {
        this.instructorId = instructorId;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public int getInstructorId() {
        return instructorId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "InstructorModel{" +
                "instructorId=" + instructorId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
