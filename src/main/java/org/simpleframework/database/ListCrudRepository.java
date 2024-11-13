package org.simpleframework.database;

import org.simpleframework.annotations.NoRepository;
import java.util.List;

@NoRepository
public interface ListCrudRepository<T, ID> extends CrudRepository<T, ID> {
    List<T> findAll();

//    <S extends T> List<S> saveAll(Iterable<S> entities);
//
//    List<T> findAllById(Iterable<ID> ids);
}
