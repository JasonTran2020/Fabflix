/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {
    // all user attributes are final because of assumption that nothing can be changed
    private final String username;
    private final int id;
    private final String firstname;
    private final String lastname;
    private final String ccId;
    private final String address;
    private final String expiryDate;
    public User(String username, int id, String firstname, String lastname, String ccId, String expiryDate, String address) {
        this.id = id;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.ccId = ccId;
        this.address = address;
        this.expiryDate = expiryDate;
    }

    public String getexpiryDate() {
        System.out.println(expiryDate);
        return expiryDate;
    }
    public String getUsername() {
        return username;
    }

    public int getUserId() {
        return id;
    }

    public String getFirstname() {
        System.out.println(firstname);
        return firstname;
    }

    public String getLastname() {
        System.out.println(lastname);
        return lastname;
    }

    public String getCcId() {
        System.out.println(ccId);
        return ccId;
    }

    public String getAddress() {
        return address;
    }


}