package org.simpleframework.database;

import org.simpleframework.annotations.NoRepository;

import java.util.List;

@NoRepository
public interface ListPagingAndSortingRepository<T, ID> extends PagingAndSortingRepository<T, ID> {

    Page<T> findAll(Pageable pageable);

    List<T> findAllSortedBy(String field, boolean ascending);

    List<T> findAllPagedAndSorted(int offset, int limit, String sortBy, boolean ascending);
}
