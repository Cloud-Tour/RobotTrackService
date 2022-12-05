package com.zkd.robotTrack.vo;

import lombok.Data;

@Data
public class MyInfoVo {

    private String nickName; //昵称
    private Double totalDistance; //总里程
    private String totalTime; //累计时间
    private Double averageSpeed; //平均速度
    private Integer count; //运行次数(当月)

}
