package com.oj.ide.complier.controller;

import com.oj.ide.complier.enums.ResultTypeEnum;
import com.oj.ide.complier.service.CppComplieService;
import com.oj.ide.complier.vo.ResultResponse;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/** * author: ljj
 * date:  2025/6/20
 * C++ 编译器 controller
 */
@CrossOrigin
@Controller
public class CppComplierController {

    @Resource
    private CppComplieService cppComplieService;

    /**
     * 执行 C++ 代码编译和执行
     * 
     * @param cppSource C++ 代码
     * @param excuteTimeLimit 执行时间限制
     * @param excuteArgs 执行参数
     * @param request HTTP 请求
     * @return 执行结果
     */
    @ResponseBody
    @RequestMapping(value = "cppComplie")
    public ResultResponse cppComplie(@RequestParam("cppSource") String cppSource,
                                     @RequestParam(value = "excuteTimeLimit", required = false) Long excuteTimeLimit,
                                     @RequestParam(value = "excuteArgs", required = false) String excuteArgs,
                                     HttpServletRequest request) {
        try {
            if (StringUtils.isEmpty(cppSource)) {
                return ResultResponse.Build(ResultTypeEnum.error, "C++ 代码不能为空！");
            }
            String[] args = getInputArgs(excuteArgs);
            if (null == excuteTimeLimit && null == args) {
                return cppComplieService.compileAndExecuteCpp(cppSource, null, null);
            } else if (null == excuteTimeLimit) {
                return cppComplieService.compileAndExecuteCpp(cppSource, null, args);
            } else if (null == args) {
                if (excuteTimeLimit <= 0) {
                    return ResultResponse.Build(ResultTypeEnum.error, "限制时间不能小于1毫秒！");
                }
                return cppComplieService.compileAndExecuteCpp(cppSource, excuteTimeLimit, null);
            } else {
                if (excuteTimeLimit <= 0) {
                    return ResultResponse.Build(ResultTypeEnum.error, "限制时间不能小于1毫秒！");
                }
                return cppComplieService.compileAndExecuteCpp(cppSource, excuteTimeLimit, args);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultResponse.Build(ResultTypeEnum.error, "C++ 代码执行出错！ 错误信息: " + e.getMessage());
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
}