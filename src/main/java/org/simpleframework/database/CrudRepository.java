package org.simpleframework.database;

import org.simpleframework.annotations.NoRepository;

import java.sql.SQLException;
import java.util.Optional;

@NoRepository
public interface CrudRepository<T, ID> extends Repository<T, ID> {
    Optional<T> findById(ID id);
    Iterable<T> findAll();

    T save(T entity);
    void deleteById(ID id);
    long count();
    boolean existsById(ID id);


}
