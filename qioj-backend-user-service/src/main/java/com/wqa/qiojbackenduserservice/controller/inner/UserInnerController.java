package com.wqa.qiojbackenduserservice.controller.inner;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.wqa.qiojbackendcommon.common.ErrorCode;
import com.wqa.qiojbackendcommon.exception.BusinessException;
import com.wqa.qiojbackendcommon.utils.JwtUtils;
import com.wqa.qiojbackendmodel.model.entity.User;
import com.wqa.qiojbackendserviceclient.service.UserFeignClient;
import com.wqa.qiojbackenduserservice.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static com.wqa.qiojbackendcommon.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 该服务仅内部调用，不是给前端的
 */
@RestController
@RequestMapping("/inner")
public class UserInnerController implements UserFeignClient {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    @GetMapping("/get/id")
    public User getById(@RequestParam("userId") long userId) {
        return userService.getById(userId);
    }

    @Override
    @GetMapping("/get/ids")
    public List<User> listByIds(@RequestParam("idList") Collection<Long> idList) {
        return userService.listByIds(idList);
    }

    @Override
    @GetMapping("/get/loginUser")
    public User getLoginUser(@RequestParam("request") HttpServletRequest request) {
        // 从请求头中取出token
        String token = request.getHeader("token");
        // 检验JWT的合法性
        DecodedJWT decodedJWT = JwtUtils.decoded(token);
        String id = decodedJWT.getClaim("id").asString();
        String storedToken = (String) redisTemplate.opsForValue().get(USER_LOGIN_STATE + id);
        if (!token.equals(storedToken)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        User currentUser = userService.getById(id);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }
}
