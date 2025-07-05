package com.oj.ide.complier.service.impl;

import com.oj.ide.complier.enums.ResultTypeEnum;
import com.oj.ide.complier.service.CppComplieService;
import com.oj.ide.complier.vo.ResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * C++ 编译器 service 实现类
 */
@Service
public class CppComplieServiceImpl implements CppComplieService {
    private static final Logger logger = LoggerFactory.getLogger(CppComplieServiceImpl.class);
    
    @Override
    public ResultResponse compileAndExecuteCpp(String cppSource, Long timeLimit, String[] args) {
        try {
        long startTime = System.currentTimeMillis();
        File cppFile = createTempFile(cppSource);
        String exePath = compileCppFile(cppFile);
        String result = executeCppFile(exePath, args);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        ResultResponse response = new ResultResponse();
        response.setExcuteResult(result);
        response.setExcuteDurationTime(duration);
        response.setResultTypeEnum(ResultTypeEnum.ok);
        response.setMessage("ok");
        return response;
        } catch (SecurityException e) {
            logger.error("安全异常，执行外部命令被拒绝: ", e);
            ResultResponse response = new ResultResponse();
            response.setResultTypeEnum(ResultTypeEnum.error);
            response.setMessage("安全异常，执行外部命令被拒绝: " + e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("执行 C++ 代码时发生异常: ", e);
            ResultResponse response = new ResultResponse();
            response.setResultTypeEnum(ResultTypeEnum.error);
            response.setMessage("执行 C++ 代码时发生异常: " + e.getMessage());
            return response;
        }
    }

    private File createTempFile(String cppSource) throws IOException {
        File file = File.createTempFile("temp", ".cpp");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(cppSource);
        }
        return file;
    }

    private String compileCppFile(File cppFile) throws IOException, InterruptedException {
        String exePath = cppFile.getAbsolutePath().replace(".cpp", ".exe");
        ProcessBuilder compileBuilder = new ProcessBuilder("g++", cppFile.getAbsolutePath(), "-o", exePath);
        Process compileProcess = compileBuilder.start();
        compileProcess.waitFor();

        // 检查编译是否成功，并记录错误信息
        if (compileProcess.exitValue() != 0) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()))) {
                StringBuilder errorMsg = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorMsg.append(line).append("\n");
                }
                logger.error("C++ 代码编译失败，错误信息: {}", errorMsg);
                throw new IOException("C++ 代码编译失败: " + errorMsg);
            }
        }
        return exePath;
    }

    private String executeCppFile(String exePath, String[] args) throws IOException, InterruptedException {
        // Windows 系统下确保可执行文件有 .exe 后缀
        if (!exePath.endsWith(".exe")) {
            exePath += ".exe";
        }
        ProcessBuilder executeBuilder = new ProcessBuilder(exePath);
        if (args != null) {
            for (String arg : args) {
                executeBuilder.command().add(arg);
            }
        }
        File executableFile = new File(executeBuilder.directory(), executeBuilder.command().get(0));
        if (!executableFile.exists()) {
            throw new IOException("Executable file not found: " + executableFile.getAbsolutePath());
        }

        Process executeProcess = executeBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(executeProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } finally {
            executeProcess.waitFor(); // 等待进程结束
        }

        return output.toString();
    }
}