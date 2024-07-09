package com.wqa.qiojbackendquestionservice.mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wqa.qiojbackendmodel.model.entity.Question;

/**
* @author wuqian
* @description 针对表【question(题目)】的数据库操作Mapper
* @createDate 2023-11-13 11:25:43
* @Entity com.wqa.qioj.model.entity.Question
*/
public interface QuestionMapper extends BaseMapper<Question> {

    int updateSubmitNumById( @Param("id") Long id);

}




