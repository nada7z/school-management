package schoolmanagement.smproject.parents.repository;

import schoolmanagement.smproject.parents.entity.Parent;
import java.util.List;
import java.util.Optional;

public interface IParentRepository {
    Parent save(Parent parent);
    Optional<Parent> findById(int id);
    Optional<Parent> findByEmail(String email);
    Optional<Parent> findByPhone(String phone);
    List<Parent> findAll();
    List<Parent> findByLastName(String lastName);
    Parent update(Parent parent);
    boolean deleteById(int id);
}