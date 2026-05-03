package schoolmanagement.smproject.students.service;

import schoolmanagement.smproject.students.entity.Student;
import schoolmanagement.smproject.parents.entity.Parent;
import schoolmanagement.smproject.students.repository.StudentRepository;
import schoolmanagement.smproject.parents.repository.ParentRepository;

public class StudentService {

    private final StudentRepository studentRepo;
    private final ParentRepository parentRepo;

    public StudentService() {
        this.studentRepo = new StudentRepository();
        this.parentRepo = new ParentRepository();
    }

    public void saveStudentWithParents(Student student) {

        Parent primary = student.getPrimaryParent();
        if (primary != null) {
            Parent savedPrimary = findOrCreateParent(primary);
            student.setPrimaryParent(savedPrimary);
        }

        Parent secondary = student.getSecondaryParent();
        if (secondary != null && secondary.getFirstName() != null && !secondary.getFirstName().isEmpty()) {
            Parent savedSecondary = findOrCreateParent(secondary);
            student.setSecondaryParent(savedSecondary);
        }

        studentRepo.save(student);
    }

    private Parent findOrCreateParent(Parent parentData) {

        Parent existing = parentRepo.findByEmail(parentData.getEmail())
                .orElse(null);

        if (existing == null) {
            existing = parentRepo.findByPhone(parentData.getPhone())
                    .orElse(null);
        }

        if (existing != null) {
            existing.setPhone(parentData.getPhone());
            existing.setAddress(parentData.getAddress());
            existing.setOccupation(parentData.getOccupation());
            existing.setRelationship(parentData.getRelationship());
            parentRepo.update(existing);
            return existing;
        }

        return parentRepo.save(parentData);
    }
}