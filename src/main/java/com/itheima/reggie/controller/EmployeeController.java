package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    // @RequestBody json格式 HttpServletRequest 存储id到session
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1。将页面提交的密码进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        

        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //参数1：表示要查询的目标字段是Employee对象的用户名字段。参数2：返回当前Employee对象的用户名值。它表示要与目标字段进行比较的值。
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        //数据库查询
        Employee emp = employeeService.getOne(queryWrapper);

        //2.查询页面输入的用户名是否存在
        if(emp == null){
            return R.error("用户名不存在");
        }

        //3.判断密码是否正确
        if(!emp.getPassword().equals(password)){
            return  R.error("密码错误");
        }

        //4.判断账号是否禁用
        if(emp.getStatus() == 0){
            R.error("账号已禁用");
        }

        //5.成功登录
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());

        //设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户的id
        Long empId = (Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

}
