package org.simpleframework.database;

public class Pageable {
    private int pageNumber;
    private int pageSize;
    private String sortBy;
    private boolean ascending;

    public Pageable(int pageNumber, int pageSize, String sortBy, boolean ascending) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.ascending = ascending;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public boolean isAscending() {
        return ascending;
    }

    public int getOffset() {
        return pageNumber * pageSize;
    }
}
