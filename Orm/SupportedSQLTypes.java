package Orm;

import java.lang.reflect.Field;
import java.time.LocalDate;

public class SupportedSQLTypes {
    public static final String VARCHAR = "VARCHAR(50)";
    public static final String DATE = "DATE";
    public static final String INT = "INT";
    public static final String BOOLEAN = "BOOL";
    public static final String DECIMAL = "DECIMAL(19,4)";

    public static String findSQLType(Field field) {
        Object type = field.getType();
        if (type == Integer.class || type == int.class || type == long.class || type == Long.class) { return INT; }
        if (type == String.class) { return VARCHAR; }
        if (type == LocalDate.class) { return DATE; }
        if (type == Boolean.class || type == boolean.class) { return BOOLEAN; }
        if (type == Double.class || type == double.class) { return DECIMAL; }
        throw new UnsupportedOperationException("Unsupported SQL Type -> " + type);
    }

}
