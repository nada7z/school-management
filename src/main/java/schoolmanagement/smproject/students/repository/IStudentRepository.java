package schoolmanagement.smproject.students.repository;

import schoolmanagement.smproject.students.entity.Student;
import java.util.List;
import java.util.Optional;

public interface IStudentRepository {
    Student save(Student student);
    Optional<Student> findById(int id);
    Optional<Student> findByEmail(String email);
    List<Student> findAll();
    List<Student> findByGradeLevel(String gradeLevel);
    List<Student> findByStatus(String status);
    List<Student> findByClassroomId(int classroomId);
    Student update(Student student);
    boolean deleteById(int id);
    
    // Dashboard Statistics
    long countByStatus(String status);
    long countAll();
}