package com.zkd.robotTrack.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tb_user")
public class Robot {

    @Id
    private ObjectId id; //主键id
    @Indexed //索引，默认正序
    private Long RobotId; //用户id
    @Indexed
    private String nickName; //昵称
    private Long created; //创建时间
    private Long updated; //更新时间

}
