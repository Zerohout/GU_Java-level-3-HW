public class UsefulTools {
    public int stringToInt(String text){
        return Integer.parseInt(text);
    }

    public boolean stringEquals(String first, String second){
        return first.equals(second);
    }

    public String intToString(int number){
        return String.valueOf(number);
    }

    public boolean isNumberEven(int number){
        return number % 2 == 0;
    }
}
