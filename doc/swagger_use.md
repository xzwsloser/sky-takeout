# 完善登录功能
- 真实项目中密码的存储需要加密处理
- 这里使用 MD5 加密处理算法进行处理,MD5加密不可以反向把密文转换为明文,所以要想要比较就需要把用户输入的密码从明文转换为暗纹并且和数据库中的数据进行比较
# 导入接口文档
## 前后端分离开发流程
![Screenshot_20240714_161623_tv.danmaku.bilibilihd.jpg](img%2FScreenshot_20240714_161623_tv.danmaku.bilibilihd.jpg)
## 导入接口
- 这里使用 YApi 接口管理平台: 用于管理接口,测试功能的一个平台: https://yapi.pro/login
- 还是使用 ApiFox 吧,YApi的服务器停了 
## 使用 Swagger 
### Swagger的介绍
- Knife4j是为Java MVC 框架集成Swagger生成 Api文档的增强解决方式
- 使用方式:
  - 导入依赖
  - 在配置类中加入knife4j2中进行相关的配置
  - 设置静态资源映射,否则就可以文档无法访问
- knife4j2的相关配置
```java
    @Bean
    public Docket docket() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller"))  // 指定扫描的包
                .paths(PathSelectors.any())
                .build();
        return docket;
    }
```
- 配置静态资源(就是在 MVC 配置类中配置相关信息)
```java
  protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
```
### Swagger中的常用注解
注解|说明
---|---
@Api|用在类上,例如 Controller,表示对于类的说明
@ApiModel|用在类上,例如 entity,DTO,VO
@ApiModelProperty|用在属性上,描述属性信息
@ApiOperation|用在方法上,例如 controller 的方法,说明方法的用途和作用

- 使用方式如下:
```java
@Data
@ApiModel(description = "员工登录时传递的数据模型")
public class EmployeeLoginDTO implements Serializable {

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

}
```