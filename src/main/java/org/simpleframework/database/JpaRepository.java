package org.simpleframework.database;

import org.simpleframework.annotations.NoRepository;
import org.simpleframework.annotations.Repository;

import java.util.List;
import java.util.Optional;

public interface JpaRepository<T, ID> extends ListCrudRepository<T, ID>, ListPagingAndSortingRepository<T, ID> {

    List<T> findBy(String field, Object value);
    List<T> finByLike(String field, String pattern);

    Optional<T> findById(ID id);

    List<T> findAll();

    T save(T entity);

    void deleteById(ID id);

    boolean existsById(ID id);

    long count();
}
