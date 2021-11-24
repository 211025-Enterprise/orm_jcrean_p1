package services;

import annotations.Column;
import annotations.PrimaryKey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ClassInspector {

    public static List<Field> getColumns(Class<?> clazz){

        List<Field> filteredField = new ArrayList<>();
        //gets the declared fields of a class, now we need to filter by annotation
        Field[] fields = clazz.getDeclaredFields();

        //returns a filtered list of fields that are marked as columns
        filteredField = Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(Column.class))
                .collect(Collectors.toList());

        //return our filtered list
        return filteredField;
    }

    public static String getTable(Class<?> clazz){

        return clazz.getSimpleName();
    }


    public static Field getPrimaryKey(Class<?> clazz){

        Field primaryKey = null;

        //finds the primary key for the table
        Field[] fields = clazz.getDeclaredFields();
        for(Field field:fields){
            if(field.isAnnotationPresent(PrimaryKey.class)){
                primaryKey = field;
            }
        }

        return primaryKey;
    }


    public static List<Method> getGetters(Class<?> clazz){

        List<Method> methodList = new ArrayList<>();

        //get our method array
        Method[] methods = clazz.getDeclaredMethods();

        //filter to getters
        methodList = Arrays.stream(methods)
                .filter(method -> method.getName().contains("get"))
                .collect(Collectors.toList());

        return methodList;
    }


    public static List<Method> getSetters(Class<?> clazz){

        List<Method> methodList = null;
        Method[] methods = clazz.getDeclaredMethods();

        methodList = Arrays.stream(methods)
                .filter(method -> method.getName().contains("set"))
                .collect(Collectors.toList());

        return methodList;
    }


    public static Method returnKeyMethod(Class<?> clazz){

        Method pKeyMethod = null;
        Method[] methods = clazz.getDeclaredMethods();
        for(Method method:methods){
            if(method.getName().toLowerCase(Locale.ROOT).contains("pkeyg")){
                pKeyMethod = method;
            }
        }

        return pKeyMethod;
    }


    public static Method returnKeySetter(Class<?> clazz){

        Method pKeySetter = null;
        Method[] methods = clazz.getDeclaredMethods();

        for(Method method:methods){
            if(method.getName().toLowerCase(Locale.ROOT).contains("pkeys")){
                pKeySetter = method;
            }
        }

        return pKeySetter;
    }
}
