public class Employee {
    // all user attributes are final because of assumption that nothing can be changed
    private final String email;
    private final String password;
    private final String fullname;

    public Employee(String email, String password, String fullname) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;

    }


    public String getPassword(){
        return password;
    }

    public String getEmail(){
        return email;
    }

    public String getFullname(){
        return fullname;
    }

}
