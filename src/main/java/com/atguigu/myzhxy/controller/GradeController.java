package com.atguigu.myzhxy.controller;


import com.atguigu.myzhxy.pojo.Grade;
import com.atguigu.myzhxy.service.GradeService;
import com.atguigu.myzhxy.util.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

@Api(tags = "年纪控制器")
@RestController
@RequestMapping("/sms/gradeController")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @ApiOperation("获取全部年级")
    @GetMapping("/getGrades")
    public Result getGrades(){

        List<Grade> grades = gradeService.getGrades();
        return Result.ok(grades);
    }


    @ApiOperation("删除Grade信息")
    @DeleteMapping("/deletGrade")
    public Result deleteGrade(
            @ApiParam("所有要删除的GradeID，JOSN集合") @RequestBody List<Integer> ids
    ){
        gradeService.removeByIds(ids);
        return Result.ok();
    }

    @ApiOperation("新增或者修改Grade,有ID就修改，无ID就新增")
    @PostMapping("/saveOrUpdateGrade")
    public Result saveOrUpdateGrade(
            @ApiParam("JSON的Grade对象")@RequestBody Grade grade
    ){
        //接受参数

        //调用服务层方法
        gradeService.saveOrUpdate(grade);

        return Result.ok();
    }

    @ApiOperation("根据年纪名称模糊查询，带分页")
    @GetMapping("/getGrades/{pageNo}/{pageSize}")
    public Result getGradeByOpr(
           @ApiParam("分页查询页码数")@PathVariable(value = "pageNo")Integer pageNo,
           @ApiParam("分页查询页大小")@PathVariable(value = "pageSize")Integer pageSize,
            @ApiParam("分页查询模糊匹配名称") String gradeName
    ){
        //分页 待条件查询
        Page<Grade> page = new Page<>(pageNo,pageSize);
        //通过服务层
        IPage<Grade> pageRs = gradeService.getGradeByOpr(page,gradeName);

        //封装Result对象并返回
        return Result.ok(pageRs);
    }

}
