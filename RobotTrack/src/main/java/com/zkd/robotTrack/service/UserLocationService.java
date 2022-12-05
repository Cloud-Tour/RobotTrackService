package com.zkd.robotTrack.service;


import com.zkd.robotTrack.pojo.RobotLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class UserLocationService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 更新用户地理位置数据
     * @param robotId    用户Id
     * @param longitude 经度
     * @param latitude  纬度
     * @return
     */
    @Async //异步方法
    public CompletableFuture<String> uploadLocation(Long robotId, Double longitude, Double latitude) {

        Query query = Query.query(Criteria.where("robotId").is(robotId));

        //更新的数据
        Update update = Update.update("robotId", robotId)
                .set("location", new GeoJsonPoint(longitude, latitude))
                .set("updated", System.currentTimeMillis());


        //如果数据库中有数据则更新，无则添加
        this.mongoTemplate.upsert(query,update, RobotLocation.class);

        return CompletableFuture.completedFuture("ok");
    }
}
