public class Lesson4 {
    public static void main(String[] args) {
        var dbr = new DBRequestBuilder();
        System.out.println(dbr.insert("login","tester").insert("password","123").insert("nickname","Tester").insert("isOnline",true).buildInsert().build());
        System.out.println(dbr.reset().update("nickname","Moder").update("isOnline",false).where("login","client2").where("password","pass2").build());
        System.out.println(dbr.reset().delete().where("nickname","NonModer").where("id",5).build());
        System.out.println(dbr.reset().select("id","nickname").where("login","Moder").build());
        System.out.println(dbr.reset().select().where("login","Moder").build());
        System.out.println(dbr.reset().delete().build());
        System.out.println(dbr.reset().delete().where("id",3).build());

    }


}
