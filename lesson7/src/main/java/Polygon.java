import java.lang.reflect.InvocationTargetException;

public class Polygon {
    public static void main(String[] args) {
         var polygon = new Polygon();
        try {
            Tester.start(polygon.getClass());
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    private static UsefulTools usefulTools;

    @BeforeSuite
    public void init() {
        System.out.println("***init");
        usefulTools = new UsefulTools();
    }


    @Tezt(priority = 3)
    public void stringToIntPositiveTest() {
        System.out.println("stringToInt Test");
        Tester.isValuesEquals(8, usefulTools.stringToInt("8"));
        Tester.isValuesEquals(32, usefulTools.stringToInt("32"));
    }

    @Tezt(priority = 1)
    public void stringEqualsNegativeTest() {
        System.out.println("stringEquals Test");
        Tester.isValuesEquals(false, usefulTools.stringEquals("string1", "string2"));
    }

    @Tezt
    public void intToStringPositiveTest() {
        System.out.println("intToString Test");
        Tester.isValuesEquals("3", usefulTools.intToString(3));
    }

    @Tezt
    public void isNumberEvenPositiveTest() {
        System.out.println("isNumberEven Test");
        Tester.isValuesEquals(true, usefulTools.isNumberEven(24));
        Tester.isValuesEquals(true, usefulTools.isNumberEven(36));
        Tester.isValuesEquals(false, usefulTools.isNumberEven(98));
    }

    @AfterSuite
    public void finalization(){
        System.out.println("***finalization");
        usefulTools = null;
    }
}
