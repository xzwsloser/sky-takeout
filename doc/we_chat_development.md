# HttpClient
- HttpClient:
  - 可以用于提供高效的,最新的,功能丰富的支持 HTTP协议的客户端编程工具包,并且支持HTTP协最新的版本和建议
- 核心:
  - HttpClient
  - HttpClients(产生HttpClient)
  - CloseableHttpClient
  - HttpGet
  - HttpPost
- 使用步骤:
  - 创建 HttpClient 对象
  - 创建 Http请求对象
  - 调用 HttpClient的execute方法发送请求
- 利用 HttpClient发送 Get请求如下:
```java
/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/17 10:48
 */
@SpringBootTest(classes = SkyApplication.class)  // 注意指定启动了的字节码文件
public class HttpClientTest {

    /**
     *  通过 HttpClient 发送 Get方式的请求
     */
    @Test
    public void testGet() throws IOException {
        // 创建 HttpClient 对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 创建请求对象
        HttpGet httpGet = new HttpGet("http://localhost:8080/user/shop/status");
        // 发送请求
        CloseableHttpResponse execute = httpClient.execute(httpGet);
        // 获取响应状态码
        int statusCode = execute.getStatusLine().getStatusCode();
        System.out.println("响应状态码为: " + statusCode);
        // 获取对象
        HttpEntity entity = execute.getEntity();
        String body = EntityUtils.toString(entity);
        System.out.println("返回的数据为: " + body);
        // 关闭资源
        execute.close();
        httpClient.close();
    }
}
```
- 发送 POST 请求的方式如下:
```java
    @Test
    public void testPost() throws IOException, JSONException {
        // 测试登录方法
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 创建请求方式
        HttpPost httpPost = new HttpPost("http://localhost:8080/admin/employee/login");
        // 创建请求体    fast-json
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username","admin");
        jsonObject.put("password","123456");
        StringEntity stringEntity = new StringEntity(jsonObject.toString());
        stringEntity.setContentEncoding("utf-8");  // 指定请求编码方式
        stringEntity.setContentType("application/json");  // 数据格式
        // 指定请求的编码方式
        httpPost.setEntity(stringEntity);
        // 发送请求
        CloseableHttpResponse response = httpClient.execute(httpPost);
        // 响应码
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("响应码：" + statusCode);
        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity);
        System.out.println("请求体: " + body);

    }
```
- 一般对于这些底层的工具类进行封装,封装在工具类中: [HttpClientUtil.java](..%2Fsky-common%2Fsrc%2Fmain%2Fjava%2Fcom%2Fsky%2Futils%2FHttpClientUtil.java)
# 微信小程序开发
- 介绍:
  - 可以以个人的身份或者团体的身份进行注册
- 接入流程:
  - 注册(注册小程序)
  - 小程序信息完善
  - 开发小程序
  - 提交审核和发布
## 开发流程详解
1. 注册小程序: https://mp.weixin.qq.com/wxopen/waregister?action=step1
## 微信小程序开发
- 小程序的目录结构如下:
![img.png](img%2Fimg.png)
- app.js  小程序逻辑
- app.json 小程序公共配置
- app.wxss 小程序公共样式表

- 一个小程序的页面由以下部分组成:
  - js 页面逻辑
  - wxml 页面结构
  - json 页面配置文件
  - wxss 页面样式表
- 微信登录主要是前端的工作,这里不是重点
### 微信登录流程
- 微信登录流程分析:
- 有一点想利用 Redis模拟 Session的过程,但是不同点就在于不是访问呢数据库而是发送请求给接口服务,获取 session_key + openId 等信息
- 前端获取 token之后就可以把token存入到 localStorage 中,之后就可以利用 localStorage中的token向开发者服务器发送请求(这里就需要 HttpClient)
![img_1.png](img%2Fimg_1.png)
- 后端开发教程可以查看文档: https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
## 微信登录实现
### 需求分析
- 基于微信登录实现小程序的登录功能,如果时新用户,就需要自动注册
- 接口: /user/user/login
- 方式: POST 
- 数据库使用user表
- 注意一切在java程序中需要使用的配置都需要在配置文件中进行配置,同时配置相关的配置类
- 请求中发送的数据封装在 DTO 中,同时返回的数据封装在 VO对象中
- 微信代码实现方式如下:
```java
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
       String openid = getOpenId(userLoginDTO.getCode());  // 获取得到的状态码
        // 判断微信用户是否是新的用户
        if(openid == null){
            throw  new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        // 如果是新的用户就需要注册新的用户
        User user = userMapper.getByOpenId(openid);
        // 返回用户对象
        if(user == null){
            // 开始构建
            user  = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);  // 注意此时还需要获取主键
        }
        return user;
    }
    
    private String getOpenId(String code){
        // 调用微信接口服务获取 OpenId 
        Map<String,String> map = new HashMap<>();
        // 注意通过配置属性类读取数据
        map.put("appid", weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN,map);
        // 判断 OpenId 是否为空
        // 开始解析 json 
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
```
### 商品浏览
- 接口设计:
  - 查询分类
  - 根据分类id查询商品
  - 根据分类id查询套餐
  - 根据套餐id查询包含的菜品
- 实现方式就是可以利用 Get请求但是参数不同,返回的就是一个数组对象
- 如果传递的参数比较少可以使用url参数传递数据,或者根据路径参数也可以
- 设计接口时需要考虑的问题:
  - 后端查询数据库时需要什么信息,那么前端就需要传递什么信息过来，这些数据通常可以封装成 XxxDTO 类
  - 前端展示页面需要什么信息,那么后端就需要返回相应的信息到前端满足页面的显示需要,一般返回的数据如果和数据库表中的数据不符合那么就可以
  自己封装一个 VO 对象，把需要返回的数据封装到一个 VO 对象中返回给前端