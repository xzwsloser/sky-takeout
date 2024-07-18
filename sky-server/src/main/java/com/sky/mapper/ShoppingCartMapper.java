package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import com.sky.service.ShoppingCartService;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/18 15:27
 */
@Mapper
public interface ShoppingCartMapper {
    /**
     * 查询购物车
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据 id 修改商品数量
     * @param cart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void update(ShoppingCart cart);

    @Insert("insert into shopping_cart (id,name,image,user_id,dish_id,setmeal_id,dish_flavor,number,amount,create_time)" +
            "values (#{id},#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{createTime})")

    void insert(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where user_id = #{userId} ")
    void delete(ShoppingCart shoppingCart);


    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);
}
