package mn.usug.dis_news_service.Service;

public class UserContext {

    private static final ThreadLocal<Integer> userId   = new ThreadLocal<>();
    private static final ThreadLocal<String>  username = new ThreadLocal<>();

    public static void set(Integer id, String name) {
        userId.set(id);
        username.set(name);
    }

    public static Integer getUserId()   { return userId.get(); }
    public static String  getUsername() { return username.get(); }

    public static void clear() {
        userId.remove();
        username.remove();
    }
}
