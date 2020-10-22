//（5）提供执行入口

//（5.0）工厂总接口（工厂的工厂的接口）
public interface ApplicationContext {
    Object getBean(String beanId);
    // 还有其他的API，先省略了。。。。
}
