/**
 * @section REFERENCES
 * 
 * Creating New Class Instances
 * https://docs.oracle.com/javase/tutorial/reflect/member/ctorInstance.html
 * 
 * java.lang.reflect.Contructor
 * https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/lang/reflect/Constructor.java
 * 
 * jdk.internal.reflect.Reflection.GetCallerClass
 * https://github.com/openjdk/jdk/blob/master/test/jdk/jdk/internal/reflect/Reflection/GetCallerClass.java
 * 
 * Arrays.copyOf()
 * https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/Arrays.java
 * :: copyOf(U[], int, Class<? extends T[]>)
 * 
 *  \@IntrinsicCandidate
 *  public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
 *      \@SuppressWarnings("unchecked")
 *      T[] copy = ((Object)newType == (Object)Object[].class)
 *          ? (T[]) new Object[newLength]
 *          : (T[]) Array.newInstance(newType.getComponentType(), newLength);
 *      System.arraycopy(original, 0, copy, 0,
 *                       Math.min(original.length, newLength));
 *      return copy;
 *  }
 */

package com.github.naiithink.app.experimental.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Relational database
 * 
 * Database.table ::= <"TABLE_NAME": String, TABLE_ENTRY: Database$Table>
 * 
 * {
 *     "TABLE_A" -> Database$Table,
 *     "TABLE_B" -> Database$Table,
 *     "TABLE_C" -> Database$Table
 * }
 * 
 * 
 * Database$Table ::= [ E[] ]
 * 
 * [
 *     id
 *     0: [ "IDCd0",    "IDCd1",    "IDCd2",    "IDCd3" ],      // Column descriptors
 *     1: [ "VAL10",    "VAL11",    "VAL12",    "VAL13" ],      // Datum go from here on
 *     2: [ "VAL20",    "VAL21",    "VAL22",    "VAL23" ],
 *     3: [ "VA3L0",    "VAL31",    "VAL32",    "VAL33" ]
 * ]
 */
public class Database {

    private static Database instance;

    private static Logger logger;

    private static Map<String, Table<?>> table;

    private class Table<E> {

        private static Table<?> instance;

        private List<String> columnDescriptor;

        private List<E[]> data;

        private Table(String... columnDescriptor) {
            this.columnDescriptor = List.of(columnDescriptor.clone());
            data = new CopyOnWriteArrayList<>();
        }

        public static Table<?> getTable() {
            if (instance == null) {
                synchronized (Database.Table.class) {
                    if (instance == null) {
                        instance = new Database.Table<>();
                    }
                }
            }

            return instance;
        }

        public void insert(E[] record) {
            this.data.add(record);
        }

        public void delete(int recordID) {
            data.remove(recordID);
        }
    }

    static {
        logger = Logger.getLogger(Database.class.getName());
    }

    protected Database() {}

    public static Database getDatabase() {
        if (instance == null) {
            synchronized (Database.class) {
                if (instance == null) {
                    instance = new Database();
                }
            }
        }

        return instance;
    }

    public Database.Table<?> getTable(String tableName) {
        return table.get(tableName);
    }

    public <T> boolean createTable(String tableName,
                                   Class<? extends T> typeOfElement,
                                   String... columnDescriptor) {

        if (table.containsKey(tableName)) {
            logger.log(Level.SEVERE, "Duplicate tableName, no operations taken");
            return false;
        }

        Database.Table< ? typeOfElement.class ?> newTable = new Database.Table(columnDescriptor);

        table.put(tableName, newTable);

        return true;
    }
}
