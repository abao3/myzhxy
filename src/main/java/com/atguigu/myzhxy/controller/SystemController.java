package com.atguigu.myzhxy.controller;


import com.atguigu.myzhxy.pojo.Admin;
import com.atguigu.myzhxy.pojo.LoginForm;
import com.atguigu.myzhxy.pojo.Student;
import com.atguigu.myzhxy.pojo.Teacher;
import com.atguigu.myzhxy.service.AdminService;
import com.atguigu.myzhxy.service.StudentService;
import com.atguigu.myzhxy.service.TeacherService;
import com.atguigu.myzhxy.util.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jdk.nashorn.internal.ir.RuntimeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.management.BufferPoolMXBean;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Api(tags = "系统控制器")
@RestController
@RequestMapping("/sms/system")
public class SystemController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private TeacherService teacherService;


    @ApiOperation("修改密码")
    @PostMapping("/updatePwd/{oldPwd}/{newPwd}")
    public Result updatePwd(@RequestHeader("token") String token,
                            @PathVariable("oldPwd") String oldPwd,
                            @PathVariable("newPwd") String newPwd){
        boolean yOn = JwtHelper.isExpiration(token);
        if(yOn){
            //token过期
            return Result.fail().message("token失效!");
        }
        //通过token获取当前登录的用户id
        Long userId = JwtHelper.getUserId(token);
        //通过token获取当前登录的用户类型
        Integer userType = JwtHelper.getUserType(token);
        // 将明文密码转换为暗文
        oldPwd= MD5.encrypt(oldPwd);
        newPwd= MD5.encrypt(newPwd);
        if(userType == 1){
            QueryWrapper<Admin> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("id",userId.intValue()).eq("password",oldPwd);
            Admin admin = adminService.getOne(queryWrapper);
            if (null!=admin) {
                admin.setPassword(newPwd);
                adminService.saveOrUpdate(admin);
            }else{
                return Result.fail().message("原密码输入有误！");
            }
        }else if(userType == 2){
            QueryWrapper<Student> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("id",userId.intValue()).eq("password",oldPwd);
            Student student = studentService.getOne(queryWrapper);
            if (null!=student) {
                student.setPassword(newPwd);
                studentService.saveOrUpdate(student);
            }else{
                return Result.fail().message("原密码输入有误！");
            }
        }
        else if(userType == 3){
            QueryWrapper<Teacher> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("id",userId.intValue()).eq("password",oldPwd);
            Teacher teacher = teacherService.getOne(queryWrapper);
            if (null!=teacher) {
                teacher.setPassword(newPwd);
                teacherService.saveOrUpdate(teacher);
            }else{
                return Result.fail().message("原密码输入有误！");
            }
        }
        return Result.ok();
    }





    @ApiOperation("头像上传统一入口")
    @PostMapping("/headerImgUpload")
    public Result headerImgUpload(
            @ApiParam("文件二进制数据") @RequestPart("multipartFile") MultipartFile multipartFile,
            HttpServletRequest request
    ){

        //使用UUID随机生成文件名
        String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        //生成新的文件名字
        String originalFilename = uuid.concat(multipartFile.getOriginalFilename());
        int i = originalFilename.lastIndexOf(".");
        String newFileName = uuid.concat(originalFilename.substring(i));
        //保存文件,将文件发送至第三方服务器
        //生成文件的保存路径(实际生产环境这里会使用真正的文件存储服务器)
        String portraitPath= "D:/code/myzhxy/target/classes/public/upload".concat(newFileName);
        try {
            multipartFile.transferTo(new File(portraitPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String path ="upload/".concat(newFileName);
        return Result.ok(path);
    }



    @GetMapping("/getInfo")
    public Result getInfoByToken(@RequestHeader("token") String token) {
        boolean expiration = JwtHelper.isExpiration(token);
        if (expiration) {
            return Result.build(null, ResultCodeEnum.TOKEN_ERROR);
        }
        //从token中解析出 用户id 和用户类型
        Long userId = JwtHelper.getUserId(token);
        Integer userType = JwtHelper.getUserType(token);

        Map<String,Object> map = new LinkedHashMap<>();
        switch (userType){
            case 1:
                Admin admin = adminService.getAdminById(userId);
                map.put("userType",1);
                map.put("user",admin);
                break;
            case 2:
                Student student = studentService.getStudentById(userId);
                map.put("userType",2);
                map.put("user",student);
                break;
            case 3:
                Teacher teacher = teacherService.getTeacherById(userId);
                map.put("userType",3);
                map.put("user",teacher);
                break;
        }

        return Result.ok(map);
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginForm loginForm, HttpServletRequest request){
        //验证码校验
       HttpSession session = request.getSession();
      String sessionVerifiCode = (String)session.getAttribute("verifiCode");
      String loginVerifiCode = loginForm.getVerifiCode();
      if("".equals(sessionVerifiCode)){
          return Result.fail().message("验证码失效，请刷新后重试");
      }
      if(!sessionVerifiCode.equalsIgnoreCase(loginVerifiCode)) {
           return Result.fail().message("验证码有误，请刷新后重试");
       }
      //从session域中移除
        session.removeAttribute("verifiCode");
        //分用户类型进行校验
        //准备一个map用户存放响应的数据
        Map<String,Object> map = new LinkedHashMap<>();
        switch (loginForm.getUserType()){
          case 1:
              try {
                  Admin admin = adminService.login(loginForm);
                  if (null != admin){
                      //用户的类型和用户的id转换成密文，以token的名称向客户端反馈
                      map.put("token",JwtHelper.createToken(admin.getId().longValue(),1));
                  }else {
                      throw new RuntimeException("用户名或密码有误");
                  }
                  return Result.ok(map);
              } catch (RuntimeException e) {
                  e.printStackTrace();
                  return Result.fail().message(e.getMessage());
              }

          case 2:
              try {
                   Student student = studentService.login(loginForm);
                  if (null != student){
                      //用户的类型和用户的id转换成密文，以token的名称向客户端反馈
                      map.put("token",  JwtHelper.createToken(student.getId().longValue(),2));
                  }else {
                      throw new RuntimeException("用户名或密码有误");
                  }
                  return Result.ok(map);
              } catch (RuntimeException e) {
                  e.printStackTrace();
                  return Result.fail().message(e.getMessage());
              }
          case 3:
              try {
                  Teacher teacher = teacherService.login(loginForm);
                  if (null != teacher){
                      //用户的类型和用户的id转换成密文，以token的名称向客户端反馈
                      map.put("token",  JwtHelper.createToken(teacher.getId().longValue(),3));
                  }else {
                      throw new RuntimeException("用户名或密码有误");
                  }
                  return Result.ok(map);
              } catch (RuntimeException e) {
                  e.printStackTrace();
                  return Result.fail().message(e.getMessage());
              }
      }
        return Result.fail().message("查无此用户");

    }


    @GetMapping ("/getVerifiCodeImage")
    public void getVerifiCodeImage(HttpServletRequest request, HttpServletResponse response){
        //获取图片
        BufferedImage verifiCodeImage = CreateVerifiCodeImage.getVerifiCodeImage();
        //获取图片上的验证码
        String verifiCode =new String(CreateVerifiCodeImage.getVerifiCode());
        HttpSession session =request.getSession();
        /*将验证码放入当前请求域*/
        session.setAttribute("verifiCode",verifiCode);
        //将验证码图片响应给浏览器
        try {
            ImageIO.write(verifiCodeImage,"JPEG",response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
