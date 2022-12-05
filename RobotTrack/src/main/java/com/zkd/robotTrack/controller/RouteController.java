package com.zkd.robotTrack.controller;

import cn.hutool.core.map.MapUtil;

import com.zkd.robotTrack.pojo.Route;
import com.zkd.robotTrack.service.RouteService;
import com.zkd.robotTrack.utils.DistanceUtils;
import com.zkd.robotTrack.vo.ErrorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("route")
public class RouteController {

    @Autowired
    private RouteService routeService;

    //创建机器人运行路线
    @PostMapping
    public Object createRoute(){
        String routeId = routeService.createRoute();
        if (null!=routeId){
            return MapUtil.builder().put("routeId",routeId).build();
        }
        return ErrorResult.builder()
                .errCode("500")
                .errMessage("创建路线失败，请重试！").build();
    }

    //删除路线
    @DeleteMapping("{routeId}")
    public Object deleteRoute(@PathVariable("routeId") String routeId){
        Boolean result = this.routeService.deleteRoute(routeId);
        if (result){
            return null;
        }
        return ErrorResult.builder().errCode("500")
                .errMessage("删除路线失败，请重试！").build();
    }


    /**
     * 更新路线
     * @param routeId   路线Id
     * @param title 路线标题
     * @return
     */
    @PutMapping
    public Object updateRoute(@RequestParam("routeId") String routeId,@RequestParam("title") String title){
        return this.routeService.updateRoute(routeId,title);
    }

    /**
     * 根据路线id查询路线数据
     * @param routeId   路线id
     * @param longitude 当前用户的经度，用于计算当前用户于该路线的距离
     * @param latitude  当前用户的纬度，用于计算当前用户于该路线的距离
     * @return
     */
    @GetMapping("{routeId}")
    public Object queryRoute(@RequestParam("routeId") String routeId,
                             @RequestParam(value = "longitude") Double longitude,
                             @RequestParam(value = "latitude") Double latitude){
        Route route = this.routeService.queryRouteById(routeId);
        if (null!=route){
            //计算当前机器人于该路线的距离
            double distance = DistanceUtils.getDistance(longitude, latitude,
                    route.getLocation().getX(),
                    route.getLocation().getY());
            route.setRouteDistance(distance);
            return route;
        }
        return ErrorResult.builder().errCode("404")
                .errMessage("路线不存在！").build();
    }


    /**
     * 查询附近的路线
     * @param longitude 当前机器人用户所在位置的经度
     * @param latitude 当前机器人用户所在位置的纬度
     * @param distance  查询的距离，单位：km，默认10km
     * @return
     */
    @GetMapping("near")
    public Object queryNearRoute(@RequestParam("longitude")Double longitude,
                                 @RequestParam("latitude") Double latitude,
                                 @RequestParam(value = "distance",defaultValue = "10") Double distance){
        return this.routeService.queryNearRoute(longitude,latitude,distance);
    }

    /**
     * 沿路线开始运动
     * @param routeId   目标路线id
     * @return  新创建的路线id
     */
    @PostMapping("{routeId}")
    public Object runFromRoute(@PathVariable("routeId")String routeId){
        String myRouteId = this.routeService.runFromRoute(routeId);
        if (null!=myRouteId){
            return MapUtil.builder("routeId",myRouteId).build();
        }

        return ErrorResult.builder().errCode("500")
                .errMessage("沿路线开始运动失败！").build();
    }

    /**
     * 历史路线
     *
     * @param robotId
     * @return
     */
    @GetMapping("history")
    public Object queryHistoryRoute(@RequestParam(value = "robotId", required = false) Long robotId,
                                    @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                    @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return this.routeService.queryHistoryRoute(robotId, pageNum, pageSize);
    }

    /**
     * 历史路线(按照日期显示)
     *
     * @return
     */
    @GetMapping("history/date")
    public Object queryHistoryRouteByDate(@RequestParam(value = "robotId", required = false) Long robotId,
                                          @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return this.routeService.queryHistoryRouteByDate(robotId, pageNum, pageSize);
    }
}
