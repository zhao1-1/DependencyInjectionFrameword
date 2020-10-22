//（3）配置文件解析：通过对不同格式的配置文件进行解析，得到BeanDefinitionList对象列表

//（3.0）BeanConfigParser对象配置转换器接口
public interface BeanConfigParser {
    List<BeanDefinition> parse(InputStream inputStream);
    List<BeanDefinition> parse(String configContent);
}
