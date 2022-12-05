package com.zkd.robotTrack.controller;


import com.zkd.robotTrack.service.UserService;
import com.zkd.robotTrack.vo.MyInfoVo;
import com.zkd.robotTrack.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class RobotController {

    @Autowired
    private UserService userService;

    /**
     * 查询机器人的信息
     *
     * @param robotId 机器人id
     * @return
     */
    @GetMapping("myinfo")
    public MyInfoVo queryMyInfo(@RequestParam(value = "robotId", required = false) Long robotId) {
        return this.userService.queryMyInfo(robotId);
    }

    /**
     * 查询附近的机器人
     * @param longitude 经度
     * @param latitude  纬度
     * @param distance  查询的距离
     * @param pageNum   页数
     * @param pageSize  页面大小
     * @return
     */
    @GetMapping("/user/near")
    public PageResult queryNearUser(@RequestParam("longitude") Double longitude,
                                    @RequestParam("latitude") Double latitude,
                                    @RequestParam(value = "distance",defaultValue = "10") Double distance,
                                    @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                    @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        return this.userService.queryNearUser(longitude,latitude,distance,pageNum,pageSize);
    }

}
