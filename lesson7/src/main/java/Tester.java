import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class Tester {


    public static void start(String className) throws Exception {
        var clazz = Class.forName(className);
        start(clazz);
    }

    public static void start(Class clazz) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        checkClassAnnotations(clazz);
        Method method = findMethods(clazz, BeforeSuite.class).get(0);
        var clazzObj = clazz.newInstance();
        method.invoke(clazzObj);
        var methods = findMethods(clazz, Tezt.class);
        for (var i = 1; i <= 10; i++) {
            for (var m : methods) {
                if (m.getAnnotation(Tezt.class).priority() == i) {
                    System.out.println("***Tezt method priority is equals " + i);
                    m.invoke(clazzObj);
                }
            }
        }
        method = findMethods(clazz, AfterSuite.class).get(0);
        method.invoke(clazzObj);
    }


    private static ArrayList<Method> findMethods(Class clazz, Class<? extends Annotation> annotation) {
        var out = new ArrayList<Method>();
        for (var method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                out.add(method);
            }
        }
        return out;
    }

    private static void checkClassAnnotations(Class clazz) {
        var methods = findMethods(clazz, BeforeSuite.class);
        if (methods.size() > 1) throw new RuntimeException("More than 1 of \"BeforeSuit\" annotations");
        methods = findMethods(clazz, AfterSuite.class);
        if (methods.size() > 1) throw new RuntimeException("More than 1 of \"AfterSuit\" annotations");
    }

    public static void isValuesEquals(Object expected, Object value) {
        var result = expected.equals(value);
        if (result) {
            System.out.println(true);
        } else {
            System.out.printf("result - %b. Expected - %s, current - %s%n", false, expected.toString(), value.toString());
        }
    }
}
