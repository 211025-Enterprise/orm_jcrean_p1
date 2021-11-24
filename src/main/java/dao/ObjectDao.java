package dao;
import annotations.NotNull;
import annotations.Unique;
import services.ClassInspector;
import util.DataConnection;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



/**
 * Returns a generic DAO for performing CRUD operations on any object following the provided specifications
 */
public class ObjectDao {



    /**
     * Creates a new table in the database
     * @param clazz The class for which a table will be created
     * @throws SQLException Invalid SQL exception, to be caught in web api
     */
    public void createTable(Class<?> clazz) throws SQLException {

        //for forming sql statements of potentially varying lengths
        StringBuilder sqlString = new StringBuilder();

        //gets our connection from property file
        Connection connection = DataConnection.getInstance();

        //comma management - prevents violation of sql syntax
        String prefix = "";

        sqlString.append("create table if not exists \"");
        sqlString.append(clazz.getSimpleName() + "\"(");

        //appends the primary key to our sql string, must be present
        Field primaryKey = ClassInspector.getPrimaryKey(clazz);
        sqlString.append("\n" + primaryKey.getName() + " serial primary key, \n");

        //gets a list of all columns excluding the primary key, appends them to our sql string - separates by constraint
        List<Field> fieldList = ClassInspector.getColumns(clazz);
        List<Field> uniqueList = new ArrayList<>();
        List<Field> notNullList = new ArrayList<>();
        List<Field> bothList = new ArrayList<>();

        for(Field checkField:fieldList){
            if((checkField.isAnnotationPresent(Unique.class))&&(checkField.isAnnotationPresent(NotNull.class))){
                bothList.add(checkField);
            }else if((checkField.isAnnotationPresent(Unique.class))&&(!checkField.isAnnotationPresent(NotNull.class))){
                uniqueList.add(checkField);
            }else if((checkField.isAnnotationPresent(NotNull.class))&&(!checkField.isAnnotationPresent(Unique.class))){
                notNullList.add(checkField);
            }
        }

        for(Field field:fieldList){
            sqlString.append(prefix);
            prefix = ", \n";
            sqlString.append(field.getName() + " ");
            Class value = field.getType();

            //type mapping from Java to postgresql
            if(value.equals(String.class)){
                sqlString.append("text");
            }else if(value.equals(int.class)){
                sqlString.append("integer");
            }else if(value.equals(double.class)){
                sqlString.append("double precision");
            }else if(value.equals(float.class)){
                sqlString.append("real");
            }else if(value.equals(boolean.class)){
                sqlString.append("boolean");
            }else if(value.equals(short.class)){
                sqlString.append("smallint");
            }else if(value.equals(long.class)){
                sqlString.append("bigint");
            }else if(value.equals(char.class)){
                sqlString.append("char");
            }

            if(bothList.contains(field)){
                sqlString.append(" unique not null");
            }else if(uniqueList.contains(field)){
                sqlString.append(" unique");
            }else if(notNullList.contains(field)){
                sqlString.append(" not null");
            }

        }

        //finishes up the statement, prepares it, executes it
        sqlString.append(");");
        String sql = sqlString.toString();
        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.executeUpdate();

    }



    /**
     * Enters a single object into the database
     * Creates a table if it does not exist
     * @param o The object to be entered into the database
     * @throws SQLException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void createObject(Object o) throws SQLException, InvocationTargetException, IllegalAccessException {

        //create a list of our new type of object
        //probably cast it as an object of its own type later
        Class<?> clazz = o.getClass();

        Connection connection = DataConnection.getInstance();

        createTable(clazz);

        String prefix = "";

        List<Method> methodList = ClassInspector.getGetters(clazz);

        StringBuilder sqlString = new StringBuilder();

        sqlString.append("insert into \"" + clazz.getSimpleName() + "\"(");

        //fields we need to insert into
        List<Field> toInsert = ClassInspector.getColumns(clazz);

        //put fields on sql string
        for(Field field:toInsert){
            sqlString.append(prefix);
            sqlString.append(field.getName());
            prefix = ", ";
        }

        sqlString.append(") values(");

        //invokes all getters on the object in proper order
        for(Field field:toInsert){

            for(Method method:methodList) {

                if (method.getName().toLowerCase(Locale.ROOT).contains(field.getName().toLowerCase(Locale.ROOT))) {

                    if(field.getType().equals(String.class) || field.getType().equals(char.class)){
                        sqlString.append("'");
                        sqlString.append(method.invoke(o));
                        sqlString.append("'");
                        sqlString.append(", ");
                    }else{
                        sqlString.append(method.invoke(o));
                        sqlString.append(", ");
                    }

                }
            }
        }

        sqlString.setLength(sqlString.length()-2);

        sqlString.append(");");

        String sql = sqlString.toString();
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.executeUpdate();
    }



    /**
     * Deletes a single record from the database based on primary key
     * @param clazz The class to which the object to delete belongs
     * @param keyInt The primary key of the object to delete
     * @param <T> Generic for dynamic handling of object types
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws InstantiationException
     */
    public <T> void deleteObject(Class<?> clazz, int keyInt) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException {

        Connection connection = DataConnection.getInstance();

        Method toInvoke = ClassInspector.returnKeyMethod(clazz);

        //delete the object based on primary key
        Field pKey = ClassInspector.getPrimaryKey(clazz);

        StringBuilder sqlString = new StringBuilder();

        sqlString.append("delete from \"" + clazz.getSimpleName() + "\" where " + pKey.getName() + " = ");

        sqlString.append(keyInt);

        sqlString.append(";");

        String sql = sqlString.toString();

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.executeUpdate();
    }



    /**
     * Deletes all records from a table in the database
     * @param clazz The table to truncate in the database
     * @throws SQLException
     */
    public void deleteAll(Class<?> clazz) throws SQLException {

        Connection connection = DataConnection.getInstance();

        String sql = "truncate \"" + clazz.getSimpleName() + "\";";
        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.executeUpdate();
    }



    /**
     * Returns an object from the database using the primary key of said object
     * @param clazz The class of the object to return
     * @param keyInt The primary key of the object
     * @param <T> Generic
     * @return A new object of type clazz
     * @throws SQLException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public <T> Object readObject(Class<?> clazz, T keyInt) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException {

        T o = (T) clazz.newInstance();
        Connection connection = DataConnection.getInstance();

        //a list of fields and methods we need later
        List<Method> invokeList = ClassInspector.getSetters(clazz);
        List<Field> toRetrieve = ClassInspector.getColumns(clazz);
        Method keySet = ClassInspector.returnKeySetter(clazz);

        StringBuilder sqlString = new StringBuilder();
        sqlString.append("select ");
        String prefix = "";

        Field pKey = ClassInspector.getPrimaryKey(clazz);

        keySet.invoke(o,keyInt);

        for(Field field:toRetrieve){
            sqlString.append(prefix);
            sqlString.append(field.getName());
            prefix = ", ";
        }
        sqlString.append(" from \"")
                .append(clazz.getSimpleName())
                .append("\"")
                .append(" where ")
                .append(pKey.getName())
                .append(" = ")
                .append(keyInt)
                .append(";");


        String sql = sqlString.toString();
        PreparedStatement stmt = connection.prepareStatement(sql);

        ResultSet rs = stmt.executeQuery();

        int i = 1;

        while(rs.next()){
            for(Field field:toRetrieve){
                for(Method method:invokeList){
                    if(method.getName().toLowerCase(Locale.ROOT).contains(field.getName().toLowerCase(Locale.ROOT))){
                        method.invoke(o,rs.getObject(i));
                        i++;
                    }

                }
            }
        }
        return o;
    }



    /**
     * Drops a table from the database
     * @param clazz The class corresponding to the table to be dropped
     * @throws SQLException
     */
    public void dropTable(Class<?> clazz) throws SQLException {

        Connection connection = DataConnection.getInstance();
        StringBuilder sqlString = new StringBuilder();

        sqlString.append("drop table \"").append(clazz.getSimpleName()).append("\";");

        String sql = sqlString.toString();
        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.executeUpdate();
    }



    /**
     * Updates the values of an object in the database
     * @param clazz The class of the object
     * @param pKey The primary key of the object
     * @param params The parameters of the object that will be updated, should not exceed the parameters accepted as defined by the class
     * @param <T> Generic for handling of varied parameter types
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws SQLException
     */
    @SafeVarargs
    public final <T> void updateObject(Class<?> clazz, int pKey, T... params) throws InvocationTargetException, IllegalAccessException, SQLException {

        Connection connection = DataConnection.getInstance();

        StringBuilder sqlString = new StringBuilder();

        Field pKeyField = ClassInspector.getPrimaryKey(clazz);

        List<Field> fieldList = ClassInspector.getColumns(clazz);

        int count = 0;

        sqlString.append("update \"")
                .append(clazz.getSimpleName())
                .append("\" set ");

        String prefix = "";

        for(T param:params){

            sqlString.append(prefix);
            prefix = ",";

            if(param instanceof String){
                sqlString.append(fieldList.get(count).getName());
                sqlString.append(" = '").append(param).append("'");
                count++;
            }if(param instanceof Integer){
                sqlString.append(fieldList.get(count).getName());
                sqlString.append(" = ").append(param);
                count++;
            }if(param instanceof Double){
                sqlString.append(fieldList.get(count).getName());
                sqlString.append(" = ").append(param);
                count++;
            }if(param instanceof Character){
                sqlString.append(fieldList.get(count).getName());
                sqlString.append(" = '").append(param).append("'");
                count++;
            }if(param instanceof Boolean){
                sqlString.append(fieldList.get(count).getName());
                sqlString.append(" = ").append(param);
                count++;
            }if(param instanceof Long){
                sqlString.append(fieldList.get(count).getName());
                sqlString.append(" = ").append(param);
                count++;
            }if(param instanceof Short){
                sqlString.append(fieldList.get(count).getName());
                sqlString.append(" = ").append(param);
                count++;
            }if(param instanceof Float){
                sqlString.append(fieldList.get(count).getName());
                sqlString.append(" = ").append(param);
                count++;
            }

        }

        sqlString.append(" where ")
                .append(pKeyField.getName())
                .append(" = ")
                .append(pKey).append(";");

        String sql = sqlString.toString();

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.executeUpdate();
    }


    /**
     * Returns a list of all objects in a table
     * @param clazz The class corresponding to the table
     * @param <T> Generic for dynamic retrieval
     * @return A list of objects
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public <T> List<T> readAllObjects(Class<T> clazz) throws SQLException, InstantiationException, IllegalAccessException, InvocationTargetException {

        List<T> genericList = new ArrayList<>();
        Connection connection = DataConnection.getInstance();
        List<Method> invokeList = ClassInspector.getSetters(clazz);
        List<Field> toRetrieve = ClassInspector.getColumns(clazz);
        Method pKeySet = ClassInspector.returnKeySetter(clazz);

        StringBuilder sqlString = new StringBuilder();
        sqlString.append("select * from \"").append(clazz.getSimpleName()).append("\";");
        String sql = sqlString.toString();

        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();
        int i = 2;
        int j = 0;


        //add objects to the list to update with table info
        StringBuilder sb = new StringBuilder();
        sb.append("select * from \"").append(clazz.getSimpleName()).append("\";");
        String sql2 = sqlString.toString();
        PreparedStatement stmt2 = connection.prepareStatement(sql2);
        ResultSet rs2 = stmt2.executeQuery();

        while(rs2.next()){
            genericList.add(clazz.newInstance());
        }


        while(rs.next()){
            //for all the fields we need
            for(Field f:toRetrieve){
                //for all the methods we need
                for(Method m:invokeList){
                    //if they match
                    if((m.getName().toLowerCase(Locale.ROOT).contains(f.getName().toLowerCase(Locale.ROOT))) && (i<=cols)){
                        //invoke on the object
                        m.invoke(genericList.get(j),rs.getObject(i));

                        //sets the primary keys
                        pKeySet.invoke(genericList.get(j),rs.getObject(1));

                        //increment i to move to next column
                        i++;
                    }
                }
            }
            //reset i to reset column and increment j to move to next row
            i=2;
            j++;
        }

        //return the object list
        return genericList;
    }
}
