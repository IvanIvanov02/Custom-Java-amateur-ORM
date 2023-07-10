package Orm;

import Entities.Column;
import Entities.ID;
import Orm.annotations.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static Orm.SupportedSQLTypes.findSQLType;


public class EntityManager<E> implements DBContext<E> {
    private  Connection connection;

    public EntityManager(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean persists(E entity) throws SQLException, IllegalAccessException {
        String tableName = this.getTableName((Class<E>) entity.getClass());
        String fieldList = this.getFieldList(entity);
        String valueList = this.getInsertValues(entity);
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",tableName,fieldList,valueList);
      return this.connection.prepareStatement(sql).execute();
    }

    @Override
    public Iterable<E> find(Class<E> entity) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return find(entity,"");
    }

    @Override
    public Iterable<E> find(Class<E> entity, String where) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String tableName = this.getTableName(entity);
        String query = String.format("SELECT * FROM %s %s ",tableName,
                where.equals("") ? "" : "WHERE " + where );

        List<E> entities = new ArrayList<>();
        ResultSet resultSet = this.connection.prepareStatement(query).executeQuery();
        E e = this.fillEntity(entity, resultSet);
        while (e != null) {
            entities.add(e);
            e = this.fillEntity(entity, resultSet);
        }
        return entities;
    }


    @Override
    public E findFirst(Class<E> entity) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return fillEntity(entity,null);
    }

    @Override
    public E findFirst(Class<E> entity, String where) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String tableName = this.getTableName(entity);
        String query = String.format("SELECT * FROM %s %s LIMIT 1",tableName,
        where.equals("") ? "" : "WHERE " + where );

        ResultSet resultSet = this.connection.prepareStatement(query).executeQuery();

        return this.fillEntity(entity,resultSet);
    }

    @Override
    public void doCreate(Class<E> entity) throws SQLException {
        String tableName = getTableName(entity);
        List<keyValuePair> fieldsAndDataTypes = getAllFieldsAndDataTypes(entity);
        String collectedFields = fieldsAndDataTypes.stream().map(keyValuePair -> String.format("%s %s", keyValuePair.key, keyValuePair.value))
        .collect(Collectors.joining(", "));
        PreparedStatement statement = connection.prepareStatement(String.format("CREATE TABLE %s ( id INT PRIMARY KEY AUTO_INCREMENT, %s)"
        ,tableName, collectedFields));
        statement.execute();
    }


    private List<keyValuePair> getAllFieldsAndDataTypes(Class<E> entity) {

        return getAllFieldsWithoutId(entity).stream()
        .map(field -> new keyValuePair(getSQLColumnName(field), getSQLType(field))).toList();
    }

    private String getSQLType(Field field) {
        return findSQLType(field);
    }

    private String getSQLColumnName(Field field) {
        return field.getAnnotationsByType(Column.class)[0].name();
    }

    private List<Field> getAllFieldsWithoutId(Class<E> entity) {
      return  Arrays.stream(entity.getDeclaredFields()).filter(f -> !f.isAnnotationPresent(ID.class)
        && f.isAnnotationPresent(Column.class)).toList();
    }


    private E fillEntity(Class<E> table, ResultSet resultSet) throws NoSuchMethodException, InvocationTargetException,
    InstantiationException, IllegalAccessException, SQLException {

        if (!resultSet.next()) { return null; }

       E entity = table.getDeclaredConstructor().newInstance();
       Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
           if (!field.isAnnotationPresent(Column.class) && !field.isAnnotationPresent(ID.class)) { continue; }
           Column annotation = field.getAnnotation(Column.class);
           String fieldName = annotation == null ? field.getName() : annotation.name();
           String value = resultSet.getString(fieldName);;
           this.fillData(entity,field,value);
        }
        return entity;
    }

    private void fillData(E entity, Field field, String value) throws IllegalAccessException {
        field.setAccessible(true);
        if (field.getType() == long.class || field.getType() == Long.class) {
            field.setLong(entity,Long.parseLong(value));
        } else if (field.getType() == LocalDate.class) {
            field.set(entity,LocalDate.parse(value));
        } else if (field.getType() == String.class) {
            field.set(entity,value);
        } else if (field.getType() == int.class) {
            field.setInt(entity,Integer.parseInt(value));
        } else { throw new UnsupportedOperationException("Unsupported type " + field.getType()); }

    }

    private String getTableName(Class<E> entity) {
        Entity annotation = entity.getAnnotation(Entity.class);
        if (annotation == null) {  throw new IllegalArgumentException("Provided class is not entity !"); }
        return annotation.name();
    }
    private String getFieldList(E entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields()).filter(f -> f.getAnnotation(Column.class) != null)
         .map(f -> f.getAnnotation(Column.class).name()).collect(Collectors.joining(","));
    }
    private String getInsertValues(E entity) throws IllegalAccessException {
        List<String> entities = new ArrayList<>();
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Column.class) == null) {
                continue;
            }
            field.setAccessible(true);
            entities.add( "\"" + (field.get(entity).toString() + "\""));
        }
        return String.join(",",entities);
    }

    private record keyValuePair(String key,String value) { }

}
