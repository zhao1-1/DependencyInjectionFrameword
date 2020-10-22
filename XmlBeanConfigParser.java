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
