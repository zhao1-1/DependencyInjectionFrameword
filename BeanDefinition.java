//（2）定义BeanDefinition类
public class BeanDefinition {
    private String id;
    private String className;
    private List<ConstructorArg> constructorArgs = new ArrayList<>();		// 构造函数的参数列表
    private Scope scope = Scope.SINGLETON;									// 默认是单例模式
    private boolean lazyInit = false;										// 默认是不进行懒加载
    
    // 省略必要的getter、setter、Constructors
    
    public boolean isSingleton() {
        return scope.equals(Scope.SINGLETON);
    }
    
    public static enum Scope {
        SINGLETON,
        PROTOTYPE
    }
    
    public static class ConstructorArg {
        private boolean isRef;
        private Class type;
        private Object arg;
        // 省略get、set方法和构造方法
    }
}
