# John Crean Custom ORM
### Description:
A lightweight ORM to abstract away the use of SQL statements in Java applications
___
### Instructions:
Use the following annotations for persisting entities and adding constraints:
> @Column - denotes a field that should be treated as a column in the database  
> @Primary Key - denotes the field that should serve as the primary identifier for an entity  
> @Unique - denotes that a column must contain a unique value  
> @NotNull - denotes that a column must contain a non-null value 
___
### Local Install with Maven:
#### Include the following in your pom.xml
>   `<groupId>org.example</groupId>`  
>   `<artifactId>Project1</artifactId>`    
>   `<version>1.0-SNAPSHOT</version>`  
___
### Includes support for:

Table Creation - createSTable(Class<?> clazz)  

Table Deletion - dropSTable(Class<?> clazz)  

Object Persistence - createSObject(Object o)  
 
Object Deletion - deleteSObject(Class<?> clazz, int keyInt)  

Deletion of All Objects - deleteSAll(Class<?> clazz)  

Object Retrieval - readSObject(Class<?> clazz, int keyInt)  

Object Retrieval (All) - readAllSObjects(Class<T> clazz)

Object Updates - updateSObject(Class<?> clazz, int keyInt, T ...params)
___
### Parameter Descriptions:

clazz: The user-defined class corresponding to the entity to be persisted, retrieved, deleted, or updated  

o: An object to be inserted into the database  

keyInt: The integer representing the primary key of an entity in the database  

params: Parameters to be updated in the database  



