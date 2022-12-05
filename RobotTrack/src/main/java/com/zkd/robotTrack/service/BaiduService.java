package com.zkd.robotTrack.service;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import com.zkd.robotTrack.config.BaiduConfig;
import com.zkd.robotTrack.pojo.Route;
import com.zkd.robotTrack.utils.UserThreadLocal;
import com.zkd.robotTrack.vo.RunParamVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BaiduService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        //驼峰转化的参数
        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    @Autowired
    private BaiduConfig baiduConfig;
    @Autowired
    private BaiduApiService baiduApiService;

    //创建鹰眼轨迹服务实体
    public boolean createEntity(String routeId) {
        String url = this.baiduConfig.getUrl() + "/entity/add";
        return this.baiduApiService.execute(url, Method.POST,
                this.createParam(routeId),response->{
            if (response.isOk()){
                String body = response.body();
                JSONObject jsonObject = JSONUtil.parseObj(body);
                return jsonObject.getInt("status") == 0;//成功
            }
            return false;
                });

    }

    //创建实体参数
    private Map<String,Object> createParam(String routeId){
        return MapUtil.builder("entity_name",this.createEntityName(routeId)).build();
    }

    //创建实体名称
    private Object createEntityName(String routeId){
        return "route_" + routeId + "_" + UserThreadLocal.get();
    }

    //删除百度鹰眼服务中的实体
    public boolean deleteEntity(String routeId) {
        String url = this.baiduConfig.getUrl() + "/entity/delete";
        return this.baiduApiService.execute(url, Method.POST,
                this.createParam(routeId),response->{
                    if (response.isOk()){
                        String body = response.body();
                        JSONObject jsonObject = JSONUtil.parseObj(body);
                        return jsonObject.getInt("status") == 0;//成功
                    }
                    return false;
                });

    }

    //给实体添加轨迹点
    public Boolean uploadLocation(String routeId, RunParamVo runParamVo) {
        String url = this.baiduConfig.getUrl() + "/track/addpoint";

        Map<String, Object> paramMap = MapUtil.builder(new HashMap<String, Object>())
                .put("entity_name", this.createEntityName(routeId))
                .put("latitude", runParamVo.getLatitude())
                .put("longitude", runParamVo.getLongitude())
                .put("loc_time", System.currentTimeMillis() / 1000)
                .put("coord_type_input", "gcj02") //gcj02
                .put("speed", runParamVo.getSpeed()).build();


        return this.baiduApiService.execute(url, Method.POST,
                paramMap,response->{
                    if (response.isOk()){
                        String body = response.body();
                        JSONObject jsonObject = JSONUtil.parseObj(body);
                        return jsonObject.getInt("status") == 0;//成功
                    }
                    return false;
                });

    }

    /**
     * 查询实体数据
     * @param routeId   路线Id
     * @param startTime 开始时间，单位，秒
     * @param endTime   结束时间
     */
    public Route queryEntity(String routeId, Long startTime, Long endTime) {
        String url = this.baiduConfig.getUrl() + "/track/gettrack";

        Map<String, Object> paramMap = MapUtil.builder(new HashMap<String, Object>())
                .put("entity_name", this.createEntityName(routeId))
                .put("start_time", startTime) //开始时间
                .put("end_time", endTime) //结束时间
                .put("is_processed", 1) //是否返回纠偏后轨迹
                .put("coord_type_output", "gcj02") //返回的坐标类型
                .build();
        return this.baiduApiService.execute(url,Method.GET,paramMap,response->{
            if (response.isOk()){
                String body = response.body();
                try {
                    //反序列化后返回
                    return MAPPER.readValue(body, Route.class);
                } catch (Exception e) {
                    log.error("查询鹰眼服务出错，查询到的数据为：{}",body,e);
                }
            }
            return null;
        });
    }
}
