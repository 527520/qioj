package com.wqa.qioj.judge.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {

    /**
     * 一组输入
     */
    private List<String> inputList;

    /**
     * 代码
     */
    private String code;

    private String language;
}
