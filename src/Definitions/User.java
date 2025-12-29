package Definitions;

public class User {
    private int id;
    private String username;
    private String password;
    private int role;
    private String phone;
    private String email;
    private String location;

    public User() {
        this.role = 0; // default
    }

    public User(int id, String username, String password, int role, String phone, String email, String location) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.location = location;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getRole() { return role; }
    public void setRole(int role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }


    @Override
    public String toString() {
        return "Definitions.User{id=" + id + ", username='" + username + "', role=" + role + "}";
    }
}