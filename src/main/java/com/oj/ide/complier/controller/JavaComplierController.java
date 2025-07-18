package com.oj.ide.complier.controller;


import com.oj.ide.complier.enums.ResultTypeEnum;
import com.oj.ide.complier.service.JavaComplieService;
import com.oj.ide.complier.util.RequestUtils;
import com.oj.ide.complier.vo.ResultResponse;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * author: ljj
 * date:  2025/6/20
 * desc: JAVA编译器controller
 */
@CrossOrigin
@Controller
public class JavaComplierController {


    @Resource
    private JavaComplieService javaComplieService;


    /**
     * 执行编译
     *
     * @param javaSource JAVA代码
     * @return 编译结果
     */
    @ResponseBody
    @RequestMapping(value = "complie")
    public ResultResponse complie(String javaSource,
                                  @RequestParam(value = "excuteTimeLimit", required = false) Long excuteTimeLimit,
                                  @RequestParam(value = "excuteArgs", required = false) String excuteArgs,
                                  HttpServletRequest request) {
        try {
            if (StringUtils.isEmpty(javaSource)) {
                return ResultResponse.Build(ResultTypeEnum.error, "代码不能为空！");
            }
            String ip = RequestUtils.getRemortIP(request);
            javaSource = "public class " + ip +"{" + javaSource+"}";
            Class clazz = javaComplieService.complie(javaSource,ip);
            String[] args = getInputArgs(excuteArgs);
            if (null == excuteTimeLimit && null == args) {
                //无参数 无时限
                return javaComplieService.excuteMainMethod(clazz);
            } else if (null == excuteTimeLimit) {
                //有参数 无时限
                return javaComplieService.excuteMainMethod(clazz, args);
            } else if (null == args) {
                //无参数 有时限
                if (excuteTimeLimit <= 0) {
                    return ResultResponse.Build(ResultTypeEnum.error, "限制时间不能小于1毫秒！");
                }
                return javaComplieService.excuteMainMethod(clazz, excuteTimeLimit);
            } else {
                //有参数 有时限
                if (excuteTimeLimit <= 0) {
                    return ResultResponse.Build(ResultTypeEnum.error, "限制时间不能小于1毫秒！");
                }
                return javaComplieService.excuteMainMethod(clazz, excuteTimeLimit, args);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultResponse.Build(ResultTypeEnum.error, "编译出错了！ 错误信息:" + e.getMessage());
        }
    }

    /**
     * 获取运行时程序需要的参数
     *
     * @param excuteArgsStr 参数字符串
     */
    private String[] getInputArgs(String excuteArgsStr) {
        if (StringUtils.isEmpty(excuteArgsStr)) {
            return null;
        } else {
            return excuteArgsStr.split(" ");
        }
    }

    @GetMapping(value = {"", "index"})
    public String index() {

        return "index";
    }


}
