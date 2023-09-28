package com.atguigu.myzhxy.service.impl;

import ch.qos.logback.core.util.StringCollectionUtil;
import com.atguigu.myzhxy.mapper.GradeMapper;
import com.atguigu.myzhxy.pojo.Grade;
import com.atguigu.myzhxy.service.GradeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.List;

@Service("gradeService")
@Transactional
public class GradeServiceImpl extends ServiceImpl<GradeMapper,Grade> implements GradeService {

    @Override
    public IPage<Grade> getGradeByOpr(Page<Grade> pageParam, String gradeName) {
        QueryWrapper queryWrapper=new QueryWrapper();

        if (!StringUtils.isEmpty(gradeName)) {
            queryWrapper.like("name",gradeName);
        }
        queryWrapper.orderByDesc("id");
        queryWrapper.orderByAsc("name");
        Page page = baseMapper.selectPage(pageParam, queryWrapper);
        return page;
    }

    @Override
    public List<Grade> getGrades() {
        return baseMapper.selectList(null);
    }
}
