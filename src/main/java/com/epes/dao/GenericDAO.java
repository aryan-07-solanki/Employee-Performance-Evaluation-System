package com.epes.dao;

import com.epes.exception.DatabaseException;
import com.epes.exception.RecordNotFoundException;

import java.util.List;

/**
 * Common database operations. Every DAO class (UserDAO, GoalDAO, ...)
 * implements this interface. T is the model class, e.g. User (Generics).
 *
 * @param <T> the type of object stored in the table
 */
public interface GenericDAO<T> {

    void add(T item) throws DatabaseException;

    T getById(int id) throws DatabaseException, RecordNotFoundException;

    List<T> getAll() throws DatabaseException;

    void update(T item) throws DatabaseException;

    void delete(int id) throws DatabaseException;
}
