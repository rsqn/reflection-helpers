package tech.rsqn.reflectionhelpers;

public class TypeUtil {
    public static boolean isSimpleType(Class<?> type) {

        if (Boolean.TYPE.equals(type) || Boolean.class.isAssignableFrom(type)) {
            return true;
        }
        if (Character.TYPE.equals(type) || Character.class.isAssignableFrom(type)) {
            return true;
        }
        if (Byte.TYPE.equals(type) || Byte.class.isAssignableFrom(type)) {
            return true;
        }
        if (Short.TYPE.equals(type) || Short.class.isAssignableFrom(type)) {
            return true;
        }
        if (String.class.isAssignableFrom(type)) {
            return true;
        }
        if (Integer.TYPE.equals(type) || Integer.class.isAssignableFrom(type)) {
            return true;
        }

        if (Long.TYPE.equals(type) || Long.class.isAssignableFrom(type)) {
            return true;
        }

        if (Float.TYPE.equals(type) || Float.class.isAssignableFrom(type)) {
            return true;
        }

        if (Double.TYPE.equals(type) || Double.class.isAssignableFrom(type)) {
            return true;
        }

        return false;
    }

    public static Object getDefaultPrimitive(Class<?> type) {
        if (Boolean.TYPE.equals(type)) return false;
        if (Character.TYPE.equals(type)) return new Character((char)0x00);
        if (Byte.TYPE.equals(type)) return new Byte((byte)0x00);
        if (Integer.TYPE.equals(type)) return 0;
        if (Long.TYPE.equals(type)) return new Long(0L);
        if (Float.TYPE.equals(type)) return new Float(0f);
        if (Double.TYPE.equals(type)) return new Double(0d);
        if (Void.TYPE.equals(type)) return null;
        if (Short.TYPE.equals(type))  return new Short("0");
        return null;
    }


}

