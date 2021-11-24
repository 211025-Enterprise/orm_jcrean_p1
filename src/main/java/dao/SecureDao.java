package dao;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

/**
 * Exception handling for ObjectDao
 */
public class SecureDao {
    ObjectDao objectDao = new ObjectDao();

    public void createSTable(Class<?> clazz){
        try{
            objectDao.createTable(clazz);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createSObject(Object o){
        try{
            objectDao.createObject(o);
        } catch (SQLException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public <T> void deleteSObject(Class<?> clazz, int keyInt){
        try{
            objectDao.deleteObject(clazz,keyInt);
        } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void deleteSAll(Class<?> clazz){
        try{
            objectDao.deleteAll(clazz);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public <T> Object readSObject(Class<?> clazz, int keyInt){
        try{
            return objectDao.readObject(clazz,keyInt);
        } catch (SQLException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void dropSTable(Class<?> clazz){
        try{
            objectDao.dropTable(clazz);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SafeVarargs
    public final <T> void updateSObject(Class<?> clazz, int keyInt, T ...params){
        try{
            objectDao.updateObject(clazz,keyInt,params);
        } catch (SQLException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public <T> List<T> readAllSObjects(Class<T> clazz){
        try{
            return objectDao.readAllObjects(clazz);
        } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
