package com.zkd.robotTrack.vo;

import lombok.Data;

/**
 * 用于附近的人响应数据结构
 */
@Data
public class NearRobotVo {

    private Long RobotId; //用户id
    private String nickName; //昵称
    private Double latitude; //机器人的纬度
    private Double longitude; //机器人的经度
    private Integer runCount; //本月完成的运行次数
    private Double distance; //离，单位：米

}
