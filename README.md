# DependencyInjectionFrameword
手写一个简易版的DI框架，有助于小伙伴们深刻理解SpringFramework的工作原理和核心架构

完整版源码：


**（2-4）Dependency Injection框架：**

  + 工厂模式和 DI 容器有何区别？

    一个工厂类只负责某个类对象或者某一组相关类对象（继承自同一抽象类或者接口的子类）的创建，

    而 DI 容器负责的是整个应用中所有类对象的创建。

  + DI容器核心功能：

    1. 配置解析
    2. 对象创建
    3. 生命周期管理

  + 实现：

    ```xml
    <!-- （1）配置文件beans.xml -->
    
    <beans>
       <bean id="rateLimiter" class="com.xzg.RateLimiter">
          <constructor-arg ref="redisCounter"/>
       </bean>
     
       <bean id="redisCounter" class="com.xzg.redisCounter" scope="singleton" lazy-init="true">
         <constructor-arg type="String" value="127.0.0.1">
         <constructor-arg type="int" value=1234>
       </bean>
    </beans>
    ```

    ```java
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
    ```

    ```java
    //（3）配置文件解析：通过对不同格式的配置文件进行解析，得到BeanDefinitionList对象列表
    
    //（3.0）BeanConfigParser对象配置转换器接口
    public interface BeanConfigParser {
        List<BeanDefinition> parse(InputStream inputStream);
        List<BeanDefinition> parse(String configContent);
    }
    
    //（3.1）支持XML文件转换的BeanConfigParser
    public class XmlBeanConfigParser implements BeanConfigParser {
        @Override
        public List<BeanDefinition> parse(InputStream inputStream) {
            String content = null;
            // TO DO....把配置文件的字节输入流对象转换成字符串对象
            return parse(content);
        }
        @Override
        public List<BeanDefinition> parse(String configContent) {
            List<BeanDefinition> beanDefinitions = new ArrayList<>();
            // TO DO....将配置文件的字符串内容转换为beanDefinition
            return beanDefinitions;
        }
    }
    
    //（3.2）支持properties文件转换的BeanConfigParser
    
    //（3.3）其他格式的配置文件转换器
    ```

    ```java
    //（4）根据反射实现核心工厂类
    public class BeansFactory {
        private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
        private ConcurrentHashMap<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();
        
        public void addBeanDefinitions(List<BeanDefinition> beanDefinitionList) {
            for (BeanDefinition beanDefinition : beanDefinitionList) {
                this.beanDefinitions.putIfAbsent(beanDefinition.getId(), beanDefinition);
            }
            for (BeanDefinition beanDefinition : beanDefinitionList) {
                if (beanDefinition.isLazyInit() == false  &&  beanDefinition.isSingleton()) {
                    createBean(beanDefinition);
                }
            }
        }
        
        public Object getBean(String beanId) {
            BeanDefinition beanDefinition = beanDefinitions.get(beanId);
            if (beanDefinition == null) {
                throw new NoSuchBeanDefinitionException("Bean is not defined: " + beanId);
            }
            return createBean(beanDefinition);
        }
        
        @VIsibleForTesting
        protected Object createBean(BeanDefinition beanDefinition) {
            if (beanDefinition.isSingleton()  &&  singletonObjects.contains(beanDefinition.getId())) {
                return singletonObjects.get(beanDefinition.getId());
            }
            Object bean = null;
            try {
                Class beanClass = Class.forName(beanDefinition.getClassName());
                List<BeanDefinition.ConstructorArg> args = beanDefinition.getConstructorArgs();
                if (args.isEmpty()) {
                    bean = beanClass.newInstance();
                } else {
                    Class[] argClasses = new Class[args.size()];
                    Object[] argObjects = new Object[args.size()];
                    for (int i = 0; i < args.size(); i++) {
                        BeanDefinition.ConstructorArg arg = args.get(i);
                        if (!arg.getIsRef()) {
                            argClasses[i] = arg.getType();
                            argObjects[i] = arg.getArg();
                        } else {
                            BeanDefinition refBeanDefinition = beanDefinitions.get(arg.getArg());
                            if (refBeanDefinition == null) {
                                throw new NoSuchBeanDefinitionException("Bean is not defined: " + arg.getArg());
                            }
                            argClasses[i] = Class.forName(refBeanDefinition.getClassName());
                            argObjects[i] = createBean(refBeanDefinition);
                        }
                    }
                    bean = beanClass.getConstructor(argClasses).newInstance(argObjects);
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                throw new  BeanCreationFailureException("", e);
            }
            if (bean != null  &&  beanDefinition.isSingleton()) {
                singletonObjects.putIfAbsent(beanDefinition.getId(), bean);
            }
            return bean;
        }
    }
    ```

    ```java
    //（5）提供执行入口
    
    //（5.0）工厂总接口（工厂的工厂的接口）
    public interface ApplicationContext {
        Object getBean(String beanId);
        // 还有其他的API，先省略了。。。。
    }
    
    //（5.1）非web环境（main、junit）
    public class ClassPathXmlApplicationContext implements ApplicationContext {
        private BeansFactory beansFactory;
        private BeanConfigParser beanConfigParser;
        
        public ClassPathXmlApplicationContext(String configLocation) {
            this.beansFactory = new BeansFactory();
            this.beanConfigParser = new XmlBeanConfigParser();			// 此处注入XmlBeanConfigParser
            loadBeanDefinitions(configLocation);
        }
        
        private void loadBeanDefinitions(String configLocation) {
            InputStream in = null;
            try {
                in = this.getClass().getResourceAsStream("/" + configLocation);
                if (in == null) {
                    throw new RuntimeException("Can not find config file: " + configLocation)
                }
                List<BeanDefinition> beanDefinitions = beanConfigParser.parse(in);
                beansFactory.addBeanDefinitions(beanDefinitions);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // TODO: log error
                    }
                }
            }
        }
        
        @Override
        public Object getBean(String beanId) {
            return beansFactory.getBean(beanId);
        }
    }
    
    //（5.2）web环境：XmlWebApplicationContext
    ```

    ```java
    //（6）Spring应用：
    public class Demo {
        public static void main(String[] args) {
            ApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");
            RateLimiter rateLimiter = (RateLimiter) ctx.getBean("rateLimiter");
            rateLimiter.test();
            //....
        }
    }    
    ```

