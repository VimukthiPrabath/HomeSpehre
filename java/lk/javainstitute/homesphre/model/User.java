package lk.javainstitute.homesphre.model;

public class User {

  private String email;
  private String mobile;
  private String username;
  private String address;
  private boolean active;



    public User(){

  }

    public User(String email, String mobile, String username, String address, boolean active) {
        this.email = email;
        this.mobile = mobile;
        this.username = username;
        this.address = address;
        this.active = active;
    }

    public User(String email, String mobile, String username, String address) {
        this.email = email;
        this.mobile = mobile;
        this.username = username;
        this.address = address;
        this.active = true;
    }

    public String getEmail() {
        return email;
    }
    public String getMobile() {
        return mobile;
    }
    public String getUsername() {
        return username;
    }
    public String getAddress() {
        return address;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
