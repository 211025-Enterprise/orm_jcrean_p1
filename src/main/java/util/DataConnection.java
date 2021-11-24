package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DataConnection {

    private static Properties properties;
    private static final String propertiesPath = "src/main/resources/application.properties";
    private static final String finalPath = "D:/Project1/src/main/resources/application.properties";


    private static void loadProperties(){
        properties = new Properties();

        try{
            //InputStream stream = new FileInputStream(new File(propertiesPath).getAbsolutePath());
            InputStream stream = new FileInputStream(finalPath);
            properties.load(stream);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private static Connection instance;

    private DataConnection(){

    }{}

    public static Connection getInstance(){
        if(properties == null){
            loadProperties();
        }

        if(instance == null){
            try {
                Class.forName("org.postgresql.Driver");
                instance = DriverManager.getConnection(properties.getProperty("url"),
                        properties.getProperty("username"),
                        properties.getProperty("password"));
            }catch(SQLException | ClassNotFoundException e){
                e.printStackTrace();
            }
        }
        return instance;
    }
}
