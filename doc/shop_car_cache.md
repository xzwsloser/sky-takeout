# 商品信息的缓存
## 缓存菜品
- 具体的缓存方式还是参考黑马点评中的缓存机制:
  - 首先查询 Redis 看Redis中是否存在缓存: 不存在就可以查询数据库,如果存在取出缓存中的数据
  - 不存在查询数据库之后就可以把查询到的数据写入到数据库中
- 注意如何确定缓存粒度: 这里根据分类进行展示,所以这里可以使用每一个分类构建一个缓存就可以了
- 最后注意修改数据库时需要清除缓存
- 类型可以使用 String,Hash,List都可以:
  - String: 直接把列表序列化存入到Redis中
  - Hash: 直接用id作为key,之后的对象作为 field
  - List: 把每一个数据作为一个对象缓存到Redis中
- 具体实现还是注意一点: 使用 StringRedisTemplate 还是 RedisTemplate
- 二者的区别就在于前者主要用于处理string类型类型的数据,存入和取出的数据都需要是String类型(但是取出之后需要自己序列化和反序列化),但是RedisTemplate操作的是任意数据类型
取出之后可以通过强制类型转换转换到需要的数据类型
- 数据的一致性:
  - 数据发生更新时一定需要清除缓存而不是更新缓存,这就是保证缓存一致性的最好的策略,详情查看黑马点评项目
- 注意缓存重建问题: 这里不需要考虑并发情况下的缓存重建问题,统一采用删除 缓存的方式重建缓存,同时注意增删改的操作一般使用的比较少,
所以在增删改操作的时候直接请求所有的缓存就可以了,最佳时间可以查看黑马点评项目中的笔记
## 缓存套餐
### SpringCache
- SpringCache其实时Spring提供的一个缓存框架,可以通过配置相关注解的方式进行缓存
- 底层可以使用不同的实践:
  - EHCache
  - Caffeine
  - Redis等
- SpringCache中的常用注解

注解|说明
---|---
@EnableCaching|开启缓存注解功能,通常加载启动类上
@Cacheable|在方法执行前先查询缓存中是否有数据,如果有数据,就可以直接返回缓存数据,如果没有缓存数据,调用方法并且将方法返回值放在缓存中
@CachePut|将方法的返回值放到缓存中
@CacheEvict|将一条或者多条数据从缓存中删除

- 其实 SpringCache底层还是使用了代理思想,通过代理对象查询数据库并且写入缓存,本质其实还是 aop , 注意利用动态代理扩展属性功能十分重要!!!
- 一般还需要和 Spring EL 结合进行使用,Spring EL(Spirng Expression) 表示 Spring中的表达式,可以利用这些表达式指定注入的对象
- 这里使用一个小的案例演示 Spring Cache:
```java
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserMapper userMapper;
                                    // 其实这里还可以写 #p0.id 或者 #a0.id 都可以这就是 Spring表达式的作用
    @PostMapping               // 注意 Spring EL 表达式 ,前面使用 # 就可以获取到动态的id,或者使用 #result.id 也可以
    @CachePut(cacheNames = "userCache",key = "#user.id")  // key 的构成就是 userCache::key,key一般不要固定
    public User save(@RequestBody User user){
        userMapper.insert(user);
        return user;
    }

    @CacheEvict(cacheNames = "userCache",key = "#id")  // 删除缓存 cacheName::key
    @DeleteMapping
    public void deleteById(Long id){
        userMapper.deleteById(id);
    }

    @CacheEvict(cacheNames = "userCache" ,allEntries = true)  // 表示请求所有键值对
	@DeleteMapping("/delAll")
    public void deleteAll(){
        userMapper.deleteAll();
    }

    @Cacheable(cacheNames = "userCache",key = "#id")  // 表示利用 Spring EL,可以查看源码
    @GetMapping
    public User getById(Long id){
        User user = userMapper.getById(id);
        return user;
    }
}
```
# 添加购物车
## 需求分析
- 购物车就是临时存储物品的位置
- 接口分析:
  - 请求方式: POST
  - 请求路径: /user/shoppingCart/add
  - 请求数据： 套餐id,菜品id,口味等信息
  - 返回结果: code,data,msg
- 数据库表包含 user_id,dish_id,flavor等信息
- 注意冗余字段name,image,冗余字段不应该经常变化
- 注意插入数据到数据库的操作逻辑: 首先查询购物车中是否有这一个商品,如果有这一个商品的话只用把数量增加一个
如果没有这一个商品的话就需要构建数据之后插入
- 注意一般的逻辑就是在外面构建数据,之后插入统一插入数据,不要零散的传递单个变量而是统一把对象作为变量传递
```java
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        // 首先判断商品是否存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);  // Spring提供的一个很好的方法
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        // 相同商品就需要份数的叠加
        if(shoppingCarts != null && !shoppingCarts.isEmpty()){
            ShoppingCart cart = shoppingCarts.get(0);
            cart.setNumber(cart.getNumber() + 1);
            // 更新
            shoppingCartMapper.update(cart);
        }
        // 如果不存在就可以使用 insert 操作
        // 首先构造购物车对象
        // 如果提交的是菜品就可以在相应的数据库表中查询
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        // 判断是菜品还是套餐
        if(dishId != null){
            // 本次添加的就是菜品
            Dish dish = dishMapper.getById(dishId);
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        } else {
            // 需要查询套餐表
            Setmeal setmeal = setmealMapper.getById(setmealId);
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        }
        // 统一进行数据的插入
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
    }
```
## 查看购物车
- 查询数据库,把购物车数据放在购物车中就可以了
- 接口设计:
  - path: /user/shoppingCart/list
  - 利用 Get 请求
- 注意Mapper层中的所有方法最好都是用这一个表关联的对象作为参数,这样可以提高代码的可复用性
## 清空购物车
- 接口设计:
  - 路径： /path/shoppingCart/clean
  - 方式: Delete 
## 减少购物车中的商品
- 唯一的细节就是查询得到购物车之后如果商品存在就需要减少商品的数量,否则就需要删除商品
