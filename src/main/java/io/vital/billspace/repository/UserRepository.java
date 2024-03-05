package io.vital.billspace.repository;

import io.vital.billspace.model.User;

import java.util.Collection;

public interface UserRepository<T extends User> {
    /*Basic CRUD Operations*/
    T create(T entity);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T entity);
    Boolean delete(T entity);

    /*Complex SQL queries*/
    Boolean verifyUserByToken(String token);
}
