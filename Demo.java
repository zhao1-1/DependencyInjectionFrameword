//（6）Spring应用：
public class Demo {
    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");
        RateLimiter rateLimiter = (RateLimiter) ctx.getBean("rateLimiter");
        rateLimiter.test();
        //....
    }
} 
