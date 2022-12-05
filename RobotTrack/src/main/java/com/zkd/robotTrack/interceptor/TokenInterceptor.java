package com.zkd.robotTrack.interceptor;

import cn.hutool.core.util.StrUtil;

import com.zkd.robotTrack.service.UserService;
import com.zkd.robotTrack.utils.NoAuthorization;
import com.zkd.robotTrack.utils.UserThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用于统一校验token的有效性，如果token有效就将userId存储到本地线程中，否则响应401
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            // 没有匹配到Controller中的方法
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //判断该方法是否是@NoAuthorization请求
        if (handlerMethod.hasMethodAnnotation(NoAuthorization.class)) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if(StrUtil.isNotEmpty(token)){
            Long robotId = this.userService.checkToken(token);
            if(null != robotId){
                //把id放入本地线程中
                UserThreadLocal.set(robotId);
                return true;
            }
        }

        //给客户端响应401状态码
        response.setStatus(401);

        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //把本地线程中的机器人id删除
        UserThreadLocal.remove();
    }
}
