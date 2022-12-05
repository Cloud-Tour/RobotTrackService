package com.zkd.robotTrack.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.mongodb.client.result.UpdateResult;

import com.zkd.robotTrack.config.RSAConfig;
import com.zkd.robotTrack.pojo.Robot;
import com.zkd.robotTrack.pojo.RobotLocation;
import com.zkd.robotTrack.pojo.Route;

import com.zkd.robotTrack.utils.JwtUtils;
import com.zkd.robotTrack.utils.TimeUtils;
import com.zkd.robotTrack.utils.UserThreadLocal;
import com.zkd.robotTrack.vo.MyInfoVo;

import com.zkd.robotTrack.vo.NearRobotVo;
import com.zkd.robotTrack.vo.PageResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RSAConfig rsaConfig;

    @Autowired
    private RouteService routeService;


    /**
     * 通过公钥校验token，校验成功后返回机器人id
     *
     * @param token
     * @return
     */
    public Long checkToken(String token) {
        Map<String, Object> map = JwtUtils.checkToken(token, this.rsaConfig.getPublishStr());
        if (CollUtil.isNotEmpty(map)) {
            return Convert.toLong(map.get("robotId"));
        }
        return null;
    }

    /**
     * 查询本机器人的信息
     *
     * @param robotId
     * @return
     */
    public MyInfoVo queryMyInfo(Long robotId) {
        if (robotId == null) {
            robotId = UserThreadLocal.get();
        }

        Query query = Query.query(Criteria.where("userId").is(robotId));
        Robot robot = this.mongoTemplate.findOne(query, Robot.class);
        if (null == robot) {
            return null;
        }
        //拷贝基础信息
        MyInfoVo myInfoVo = BeanUtil.toBeanIgnoreError(robot, MyInfoVo.class);

        Date today = new Date();
        Long minDate = DateUtil.beginOfMonth(today).getTime();
        Long maxDate = DateUtil.endOfMonth(today).getTime();

        List<Route> routeList = this.routeService.queryRouteListByDate(robotId, minDate, maxDate);

        if (CollUtil.isEmpty(routeList)) {
            //返回默认值
            myInfoVo.setCount(0);
            myInfoVo.setTotalDistance(0.00);
            myInfoVo.setAverageSpeed(0.00);
            myInfoVo.setTotalTime("00:00");
            return myInfoVo;
        }

        //运动次数
        myInfoVo.setCount(routeList.size());

        //计算总里程
        try {
            double totalDistance = routeList.stream().mapToDouble(route -> Convert.toDouble(route.getDistance(), 0d)).sum();
            myInfoVo.setTotalDistance(NumberUtil.round(totalDistance, 2).doubleValue());
        } catch (Exception e) {
            myInfoVo.setTotalDistance(0d);
        }

        //计算平均速度
        try {
            OptionalDouble averageSpeed = routeList.stream().mapToDouble(route -> Convert.toDouble(route.getSpeed(), 0d)).average();
            myInfoVo.setAverageSpeed(NumberUtil.round(averageSpeed.getAsDouble(), 2).doubleValue());
        } catch (Exception e) {
            myInfoVo.setAverageSpeed(0d);
        }

        //计算运动总时间
        try {
            long totalTime = routeList.stream()
                    .filter(route -> route.getEndPoint() != null && route.getStartPoint() != null)
                    .mapToLong(route -> route.getEndPoint().getLocTime() - route.getStartPoint().getLocTime())
                    .sum();
            myInfoVo.setTotalTime(TimeUtils.formatTime(totalTime));
        } catch (Exception e) {
            myInfoVo.setTotalTime("0:00");
        }

        return myInfoVo;
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
    public PageResult queryNearUser(Double longitude, Double latitude,
                                    Double distance, Integer pageNum, Integer pageSize) {
        PageResult pageResult = new PageResult();
        pageResult.setPageNum(pageNum);
        pageResult.setPageSize(pageSize);

        //构造分页条件
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);

        //构造查询对象
        NearQuery nearQuery = NearQuery.near(longitude, latitude, Metrics.KILOMETERS)
                .with(pageRequest)
                .maxDistance(distance);

        GeoResults<RobotLocation> geoResults = this.mongoTemplate.geoNear(nearQuery, RobotLocation.class);
        if (CollUtil.isEmpty(geoResults.getContent())){
            return pageResult;
        }

        Long robotId = UserThreadLocal.get();

        //本月时间范围
        Date today = new Date();
        Long minTime = DateUtil.beginOfMonth(today).getTime();
        Long maxTime = DateUtil.endOfMonth(today).getTime();

        List<NearRobotVo> nearRobotVoList = geoResults.getContent().stream()
                //排除自己
                .filter(result -> !ObjectUtil.equal(result.getContent().getRobotId(), robotId))
                .map(result -> {
                    RobotLocation userLocation = result.getContent();
                    NearRobotVo nearUserVo = new NearRobotVo();
                    nearUserVo.setRobotId(userLocation.getRobotId());
                    nearUserVo.setLongitude(userLocation.getLocation().getX());
                    nearUserVo.setLatitude(userLocation.getLocation().getY());
                    //距离
                    nearUserVo.setDistance(NumberUtil.round(result.getDistance().getValue(), 2).doubleValue()*1000);
                    //查询该用户本月的运动次数
                    Integer runCount = this.routeService.queryRouteCountByDate(nearUserVo.getRobotId(),minTime,maxTime);
                    nearUserVo.setRunCount(runCount);
                    return nearUserVo;
                }).collect(Collectors.toList());

        //查询用户信息，回填到nearUserVo对象中
        Map<Long, Robot> userMap = this.queryUserMap(CollUtil.getFieldValues(nearRobotVoList, "robotId"));
        nearRobotVoList.forEach(nearUserVo -> {
            Robot robot = userMap.get(nearUserVo.getRobotId());
            nearUserVo.setNickName(robot.getNickName());
        });

        pageResult.setRecords(nearRobotVoList);
        return pageResult;

    }

    /**
     * 根据用户id的列表查询用户列表
     * @param userIdList    用户id列表
     * @return
     */
    public Map<Long,Robot> queryUserMap(List<Object> userIdList){
        Query query = Query.query(Criteria.where("robotId").in(userIdList));
        return this.mongoTemplate.find(query,Robot.class)
                .stream().collect(Collectors.toMap(Robot::getRobotId,robot->robot));
    }
}
