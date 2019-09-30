package gliby.minecraft.gman;

import com.google.common.base.Predicate;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtility {

    public <T> List<T> getSpecificFields(Object object, Predicate filter, T typeOfField)
            throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = ArrayUtils.addAll(object.getClass().getFields(), object.getClass().getDeclaredFields());
        List<T> list = new ArrayList<T>();
        for (Field field : fields) {
            if (filter.apply(field)) {
                field.setAccessible(true);
                T fieldObject = (T) field.get(object);
                list.add(fieldObject);
            }
        }
        return list;
    }

}
