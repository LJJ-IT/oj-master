package com.oj.ide.complier.service;

import com.oj.ide.complier.vo.ResultResponse;

/**
 * Python 编译器 service 接口
 */
public interface PythonComplieService {
    /**
     * 编译 Python 代码并执行
     * 
     * @param pythonSource Python 代码
     * @param timeLimit 时间限制
     * @param args 运行参数数组
     * @return 执行结果
     */
    ResultResponse compileAndExecutePython(String pythonSource, Long timeLimit, String[] args) throws Exception;
}