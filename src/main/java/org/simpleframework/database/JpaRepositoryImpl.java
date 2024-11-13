package org.simpleframework.database;

import org.simpleframework.annotations.*;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JpaRepositoryImpl<T, ID> implements JpaRepository<T, ID> {

    private final Class<T> entityType;
    private final String tableName;
    private final Field idField;

    public JpaRepositoryImpl(Class<T> entityType) {
        this.entityType = entityType;
        this.tableName = getTableName();
        this.idField = findIdField();
        this.idField.setAccessible(true);
    }


    public String getTableName() {
        Table table = entityType.getAnnotation(Table.class);

        if (table == null) {
            throw new RuntimeException("Entity class must be annotated with @Table");
        }

        return table.name();
    }

    private Field findIdField() {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No @Id field in " + entityType.getName()));
    }

    @Override
    public List<T> findBy(String field, Object value) {
        return List.of();
    }

    @Override
    public List<T> finByLike(String field, String pattern) {
        return List.of();
    }

    @Override
    public Optional<T> findById(ID id) {
        String sql = String.format("SELECT * FROM %s WHERE id = ?", tableName);
        T entity = JdbcTemplate.queryForObject(sql, this::mapRowToEntity, id);
        return Optional.ofNullable(entity);
    }

    @Override
    public List<T> findAll() {
        String sql = String.format("SELECT * FROM %s", tableName);
        return JdbcTemplate.queryForList(sql, this::mapRowToEntity);
    }

    @Override
    public T save(T entity) {
        if (existsById((ID) getIdValue(entity))) {
            update(entity);
        } else {
            insert(entity);
        }
        return entity;
    }

    @Override
    public void deleteById(ID id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        JdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(ID id) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE id = ?";
        return Boolean.TRUE.equals(JdbcTemplate.queryForObject(sql, rs -> {
            try {
                return rs.getInt(1) > 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, id));
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        Long res = JdbcTemplate.queryForObject(sql, rs -> {
            try {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            } catch (SQLException e) {
                return 0L;
            }

            return 0L;
        });

        return res != null ? res : 0L;
    }

    @Override
    public List<T> findAllSortedBy(String fieldName, boolean ascending) {
        String order = ascending ? "ASC" : "DESC";
        String sql = "SELECT * FROM " + tableName + " ORDER BY " + fieldName + " " + order;
        return JdbcTemplate.queryForList(sql, this::mapRowToEntity);
    }

    @Override
    public List<T> findAllPagedAndSorted(int offset, int limit, String sortBy, boolean ascending) {
        String order = ascending ? "ASC" : "DESC";
        String sql = "SELECT * FROM " + tableName + " ORDER BY " + sortBy + " " + order + " LIMIT ? OFFSET ?";
        return JdbcTemplate.queryForList(sql, this::mapRowToEntity, limit, offset);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        long totalElements = count();
        List<T> content = findAllPagedAndSorted(pageable.getOffset(), pageable.getPageSize(), pageable.getSortBy(), pageable.isAscending());
        return new Page<>(content, totalElements, pageable.getPageNumber(), pageable.getPageSize());
    }

    private void insert(T entity) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName).append(" (");

        StringBuilder placeholders = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();

        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if (field != idField || field.getAnnotation(GeneratedValue.class) == null) {
                sql.append(field.getName()).append(",");
                placeholders.append("?,");
                params.add(getFieldValue(entity, field));
            }
        }

        sql.deleteCharAt(sql.length() - 1).append(") ");
        placeholders.deleteCharAt(placeholders.length() - 1).append(")");
        sql.append(placeholders);

        JdbcTemplate.update(sql.toString(), params.toArray());
    }

    private void update(T entity) {
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(tableName).append(" SET ");
        List<Object> params = new ArrayList<>();

        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field != idField) {
                sql.append(field.getName()).append(" = ?,");
                params.add(getFieldValue(entity, field));
            }
        }

        sql.delete(sql.length() - 2, sql.length());
        sql.append(" WHERE ").append(idField.getName()).append(" = ?");
        params.add(getIdValue(entity));

        JdbcTemplate.update(sql.toString(), params.toArray());
    }

    private Object getIdValue(T entity) {
        try {
            return idField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getFieldValue(T entity, Field field) {
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected T mapRowToEntity(ResultSet rs) {
        try {
            T entity = entityType.getDeclaredConstructor().newInstance();
            for (Field field : entity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                field.set(entity, rs.getObject(field.getName()));
            }

            return entity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
