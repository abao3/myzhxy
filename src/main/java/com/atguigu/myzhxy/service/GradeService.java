package com.atguigu.myzhxy.service;

import com.atguigu.myzhxy.pojo.Grade;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface GradeService extends IService<Grade> {

    IPage<Grade> getGradeByOpr(Page<Grade> page, String gradName);

    List<Grade> getGrades();
}
