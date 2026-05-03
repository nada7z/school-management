package schoolmanagement.smproject.teachers.entity;

import schoolmanagement.smproject.classes.entity.Classroom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a teaching staff member.
 * Maps to the 'teachers' database table.
 */
public class Teacher {
    private int id;
    private String employeeId;          // e.g., "T-2026-001"
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String subjectSpecialization; // e.g., "Mathematics", "French", "Science"
    private String qualification;         // e.g., "B.Ed", "M.Sc", "PhD"
    private LocalDate hireDate;
    private String status;                // "Active", "Inactive", "On Leave"
    private String emergencyContactName;
    private String emergencyContactPhone;
    
    // Optional: For future UI/Repo joins
    private List<Classroom> assignedClassrooms = new ArrayList<>();

    // 🔹 Constructors
    public Teacher() {
        this.hireDate = LocalDate.now();
        this.status = "Active";
    }

    public Teacher(String firstName, String lastName, String email, String phone, 
                   String subject, String qualification) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.subjectSpecialization = subject;
        this.qualification = qualification;
    }

    // 🔹 Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

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

    public String getSubjectSpecialization() { return subjectSpecialization; }
    public void setSubjectSpecialization(String subjectSpecialization) { this.subjectSpecialization = subjectSpecialization; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public List<Classroom> getAssignedClassrooms() { return assignedClassrooms; }
    public void setAssignedClassrooms(List<Classroom> assignedClassrooms) { this.assignedClassrooms = assignedClassrooms; }

    // 🔹 UI / Business Helpers
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getProfessionalTitle() {
        String prefix = "Mr./Ms.";
        if ("Female".equalsIgnoreCase(gender)) prefix = "Ms.";
        else if ("Male".equalsIgnoreCase(gender)) prefix = "Mr.";
        return prefix + " " + lastName;
    }

    public String getDisplayLabel() {
        return getProfessionalTitle() + " • " + (subjectSpecialization != null ? subjectSpecialization : "General");
    }

    public boolean isAvailable() {
        return "Active".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "id=" + id +
                ", name='" + getFullName() + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", subject='" + subjectSpecialization + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}