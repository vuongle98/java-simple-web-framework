package org.simpleframework.database;

import org.simpleframework.annotations.NoRepository;

@NoRepository
public interface PagingAndSortingRepository<T, ID> extends Repository<T, ID> {

    Page<T> findAll(Pageable pageable);

}
