package com.sky.service.impl;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 后期需要进行md5加密，然后再进行比对
        // 首先加密密码
        String passStr = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!passStr.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public Result saveEmployee(EmployeeDTO employeeDTO) {
        // 转换为实体类
        Employee employee = new Employee();
        // 进行属性转换
        // 使用对象属性的拷贝
        BeanUtils.copyProperties(employeeDTO,employee);  // Spring提供的工具,用于对象属性的拷贝
        // 设置状态的状态
        employee.setStatus(StatusConstant.ENABLE);  // 设置状态,定义为常量,方式硬编码

        // 设置密码,使用默认密码,还是注意通过常量的方式进行应用
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        // 设置当前时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        // 设置修改人
        // 这里需要完善代码,可以使用 ThreadLocal 或者 JWT
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        // 这里需要处理用户名已经存在不可以添加用户的问题
        employeeMapper.save(employee);
        return Result.success();
    }

}
