package schoolmanagement.smproject.students.entity;

import java.time.LocalDate;
import schoolmanagement.smproject.parents.entity.Parent;

public class Student {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private LocalDate enrollmentDate;
    private String gradeLevel;
    private String classroom;        // ✅ ADDED - Required for bulletin page
    private String status;
    
    // Parent/Guardian Information (Primary)
    private Parent primaryParent;
    private Parent secondaryParent;
    
    // Emergency Contact (can be same as parent)
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;
    
    // Constructors
    public Student() {
        this.enrollmentDate = LocalDate.now();
        this.status = "Active";
    }
    
    public Student(String firstName, String lastName, String email, String phone, 
                   LocalDate dateOfBirth, String gender, String address, String gradeLevel) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.gradeLevel = gradeLevel;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getFullName() { return firstName + " " + lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    
    public String getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
    
    // ✅ ADDED: Classroom getter/setter
    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Parent getPrimaryParent() { return primaryParent; }
    public void setPrimaryParent(Parent primaryParent) { this.primaryParent = primaryParent; }
    
    public Parent getSecondaryParent() { return secondaryParent; }
    public void setSecondaryParent(Parent secondaryParent) { this.secondaryParent = secondaryParent; }
    
    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }
    
    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }
    
    public String getEmergencyContactRelationship() { return emergencyContactRelationship; }
    public void setEmergencyContactRelationship(String emergencyContactRelationship) { this.emergencyContactRelationship = emergencyContactRelationship; }
    
    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", grade='" + gradeLevel + '\'' +
                ", classroom='" + classroom + '\'' +
                ", primaryParent='" + (primaryParent != null ? primaryParent.getFullName() : "N/A") + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}