package com.oj.ide.complier.service.impl;

import com.oj.ide.complier.enums.ResultTypeEnum;
import com.oj.ide.complier.service.PythonComplieService;
import com.oj.ide.complier.vo.ResultResponse;
import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Python 编译器 service 实现类
 */
@Service
public class PythonComplieServiceImpl implements PythonComplieService {
    @Override
    public ResultResponse compileAndExecutePython(String pythonSource, Long timeLimit, String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        PythonInterpreter interpreter = new PythonInterpreter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        interpreter.setOut(new PrintStream(baos));

        interpreter.exec(pythonSource);

        String result = baos.toString("GBK");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        ResultResponse response = new ResultResponse();
        response.setExcuteResult(result);
        response.setExcuteDurationTime(duration);
        response.setResultTypeEnum(ResultTypeEnum.ok);
        response.setMessage("ok");
        return response;
    }
}