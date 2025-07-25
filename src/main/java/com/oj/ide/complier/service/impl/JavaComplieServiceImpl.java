package com.oj.ide.complier.service.impl;


import com.oj.ide.complier.config.Constans;
import com.oj.ide.complier.enums.ResultTypeEnum;
import com.oj.ide.complier.exception.ComplieException;
import com.oj.ide.complier.service.JavaComplieService;
import com.oj.ide.complier.util.ClassClassLoader;
import com.oj.ide.complier.vo.ResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Service
public class JavaComplieServiceImpl implements JavaComplieService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Class complie(String javaSource, String className) throws Exception {

        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager standardFileManager = javaCompiler.getStandardFileManager(null, null, null);
        StringObject so = new StringObject(className, javaSource);
        JavaFileObject file = so;
        Iterable options = Arrays.asList("-d", Constans.classPath);
        Iterable<? extends JavaFileObject> files = Arrays.asList(file);

        JavaCompiler.CompilationTask task = javaCompiler.getTask(null, standardFileManager, null, options, null, files);
        Boolean result = task.call();
        if (result) {
            logger.info("complie java source success!");
        } else {
            logger.info("complie java scoure fail!");
            throw new ComplieException("请检查语法");
        }
        return loadClass(className);
    }

    /**
     * 加载CLASS
     *
     * @param clasName 类名
     * @return class文件
     */
    public Class loadClass(String clasName) throws Exception {
//        Class<?> clazz = Class.forName("Main");
        //用自定义classLoader加载这个class
        ClassClassLoader classClassLoader = new ClassClassLoader(getClass().getClassLoader());
        Class<?> clazz = classClassLoader.loadClass(clasName);
        return clazz;
    }


    @Override
    public ResultResponse excuteMainMethod(Class clazz) throws Exception {
        return excuteMainMethodWithClass(clazz, new String[]{});
    }

    @Override
    public ResultResponse excuteMainMethod(Class clazz, String[] args) throws Exception {
        return excuteMainMethodWithClass(clazz, args);
    }

    @Override
    public ResultResponse excuteMainMethod(Class clazz, Long timeLimit) throws Exception {
        return excuteMainMethod(clazz, timeLimit, new String[]{});
    }

    @Override
    public ResultResponse excuteMainMethod(Class clazz, Long timeLimit, String[] args) throws Exception {
        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<FutureTask<ResultResponse>> futureTaskList = new ArrayList<>();
        Callable<ResultResponse> mainMethodExcuteCallable = new Callable<ResultResponse>() {
            @Override
            public ResultResponse call() throws Exception {
                return excuteMainMethodWithClass(clazz, args);
            }
        };
        FutureTask<ResultResponse> futureTask = new FutureTask<ResultResponse>(mainMethodExcuteCallable);
        futureTaskList.add(futureTask);
        executorService.submit(futureTask);//提交到线程池中去执行
        //只里仅仅为了测试，这样写,把多线程当没有线程来用，意思一下
        ResultResponse resultResponse = null;
        FutureTask<ResultResponse> taskItem = futureTaskList.get(0);
//        for (FutureTask<ResultResponse> taskItem : futureTaskList) {
        try {
            resultResponse = taskItem.get(timeLimit, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            taskItem.cancel(true);//超时，就取消
            e.printStackTrace();
            throw new RuntimeException("运行超时了！限定时间为:" + timeLimit + "毫秒");
        }
//        }
        return resultResponse;
    }

    private ResultResponse excuteMainMethodWithClass(Class clazz, String[] args) throws Exception {
        setInputArgs(args);
        ByteArrayOutputStream baoStream = new ByteArrayOutputStream(1024);
        PrintStream cacheStream = new PrintStream(baoStream);
        PrintStream oldStream = System.out;
        System.setOut(cacheStream);//将输出结果保持到baoStream中，以便后面用
        //执行Main方法
        Method method = clazz.getMethod(Constans.excuteMainMethodName, String[].class);

        Long startTime = System.currentTimeMillis();//开始时间

        method.invoke(null, (Object) new String[]{});

        Long endTime = System.currentTimeMillis();//结束时间

        System.setOut(oldStream);//将输出打印到控制台
        String reusltInfo = baoStream.toString();
        ResultResponse resultResponse = new ResultResponse();
        resultResponse.setExcuteResult(reusltInfo);
        resultResponse.setExcuteDurationTime(endTime - startTime);
        resultResponse.setResultTypeEnum(ResultTypeEnum.ok);
        resultResponse.setMessage("ok");
        return resultResponse;
    }

    /**
     * 将args参数设为程序运行时的参数
     *
     * @param args 参数数组
     */
    private void setInputArgs(String[] args) {
        StringBuffer argSb = new StringBuffer();
        for (String argItem : args) {
            argSb.append(argItem);
            argSb.append(" ");
        }
        BufferedInputStream argInputStrem = new BufferedInputStream(new ByteArrayInputStream(argSb.toString().getBytes()));
        System.setIn(argInputStrem);
    }


    private class StringObject extends SimpleJavaFileObject {
        private String contents = null;

        public StringObject(String clasName, String contents) throws Exception {
            super(URI.create("String:///" + clasName + Kind.SOURCE.extension), Kind.SOURCE);
            this.contents = contents;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return contents;
        }
    }


    public static void main(String[] args) {
        while (true) {
            String code = "public class Main {\n" +
                    "\tpublic static void main(String[] args){\n" +
                    "\t\tSystem.out.println(\"hello\t\t\t\t\tworld\");\n" +
                    "\t}\n" +
                    "}";
            System.out.println(code);
            JavaComplieServiceImpl javaComplieService = new JavaComplieServiceImpl();
            try {
                Class clazz = javaComplieService.complie(code,"1231");
                ResultResponse resultResponse = javaComplieService.excuteMainMethod(clazz);
                System.out.println("--------->" + resultResponse.getExcuteResult());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
