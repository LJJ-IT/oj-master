package com.oj.ide.complier.service;

import com.oj.ide.complier.vo.ResultResponse;

/**
 * C++ 编译器 service 接口
 */
public interface CppComplieService {
    /**
     * 编译 C++ 代码并执行
     * 
     * @param cppSource C++ 代码
     * @param timeLimit 时间限制
     * @param args 运行参数数组
     * @return 执行结果
     */
    ResultResponse compileAndExecuteCpp(String cppSource, Long timeLimit, String[] args) throws Exception;
}