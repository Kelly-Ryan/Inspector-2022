package models;

public class InstructorModel {
    private String name, email, password;

    //called when existing user logs in
    public InstructorModel(String name, String email) {
        this.name = name;
        this.email = email;
    }

    //called when new user is registered
    public InstructorModel(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
