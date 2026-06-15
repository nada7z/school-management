package schoolmanagement.smproject.attendance.entity;

import java.time.LocalDate;

public class Attendance {
    private int id;
    private String personType; // "STUDENT" or "TEACHER"
    private int personId;
    private String personName;
    private String className;
    private LocalDate date;
    private String status;
    private String remarks;

    public Attendance() {
    }

    public Attendance(int id, String personType, int personId, String personName, String className, LocalDate date,
            String status, String remarks) {
        this.id = id;
        this.personType = personType;
        this.personId = personId;
        this.personName = personName;
        this.className = className;
        this.date = date;
        this.status = status;
        this.remarks = remarks;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPersonType() {
        return personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}