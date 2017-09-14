package tech.rsqn.reflectionhelpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


public class ConversionUtil {
    private static Logger log = LoggerFactory.getLogger(ConversionUtil.class);

    public static Object convertForSetter(Method setter, Object o) {
        Class requiredType = null;
        if (o == null) {
            return null;
        }
        if (o instanceof String && ((String) o).length() == 0) {
            return null;
        }

        try {
            requiredType = setter.getParameterTypes()[0];
            if (requiredType.isAssignableFrom(o.getClass())) {
                return o;
            }
            if (requiredType.isPrimitive()) {
                log.debug("Converting to primitive type " + requiredType + " from " + o);
                return convert(requiredType, o);
            }

            log.debug("Attempting to convert " + o + " of type " + o.getClass() + " to " + requiredType);
            Constructor constructor = requiredType.getConstructor(String.class);
            Object converted = constructor.newInstance(o);
            log.debug("Conversion successful " + converted);
            return converted;
        } catch (Exception e) {
            throw new RuntimeException("Exception during conversion from String to " + requiredType, e);
        }
    }

    public static String convertToString(Object o) {
        if (o == null) {
            return null;
        }
        return o.toString();
    }

    public static <T> T convert(Class<?> targetType, Object o) {
        if (o == null) {
            if (targetType.isPrimitive()) {
                return (T) TypeUtil.getDefaultPrimitive(targetType);
            }
            return null;
        }
        try {
            if (targetType.isPrimitive()) {
                targetType = convertType(targetType);
            }
            //todo: fix this (quick cheat to get a build out)
            T ret = null;
            if (TypeUtil.isSimpleType(o.getClass()) && targetType.equals(String.class)) {
                ret = (T) o.toString();
            } else {
                Constructor r = targetType.getConstructor(o.getClass());
                ret = (T) r.newInstance(o);
            }
            return ret;

        } catch (NoSuchMethodError ignore) {
            log.error("NoSuchMethodError " + o + " of class " + o.getClass() + " to " + targetType, ignore);
        } catch (Exception e) {
            log.error("could not convert " + o + " of class " + o.getClass() + " to " + targetType, e);
//            System.err.println("could not convert " + o + " of class " + o.getClass() + " to " + targetType);
            e.printStackTrace();
            throw new RuntimeException("could not convert " + o + " of class " + o.getClass() + " to " + targetType, e);
        }
        return null;
    }

    public static Class<?> convertType(Class<?> type) {
        if (Boolean.TYPE.equals(type)) return Boolean.class;
        if (Character.TYPE.equals(type)) return Character.class;
        if (Byte.TYPE.equals(type)) return Byte.class;
        if (Integer.TYPE.equals(type)) return Integer.class;
        if (Long.TYPE.equals(type)) return Long.class;
        if (Float.TYPE.equals(type)) return Float.class;
        if (Double.TYPE.equals(type)) return Double.class;
//        if (Void.TYPE.equals(type)) return null;
        if (Short.TYPE.equals(type)) return Short.class;
        return null;
    }

}
