package schoolmanagement.smproject.parents.entity;

public class Parent {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String phoneAlternate;
    private String relationship;
    private String occupation;
    private String address;
    private boolean primaryContact;

    public Parent() {
    }

    public Parent(String firstName, String lastName, String email, String phone,
            String relationship, String occupation, String address, boolean primaryContact) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.relationship = relationship;
        this.occupation = occupation;
        this.address = address;
        this.primaryContact = primaryContact;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoneAlternate() {
        return phoneAlternate;
    }

    public void setPhoneAlternate(String phoneAlternate) {
        this.phoneAlternate = phoneAlternate;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(boolean primaryContact) {
        this.primaryContact = primaryContact;
    }

    @Override
    public String toString() {
        return "Parent{" +
                "id=" + id +
                ", name='" + getFullName() + '\'' +
                ", relationship='" + relationship + '\'' +
                ", phone='" + phone + '\'' +
                ", primary=" + primaryContact +
                '}';
    }
}