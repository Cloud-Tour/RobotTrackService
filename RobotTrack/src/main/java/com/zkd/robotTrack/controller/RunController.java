package com.zkd.robotTrack.controller;


import com.zkd.robotTrack.service.BaiduService;
import com.zkd.robotTrack.service.UserLocationService;
import com.zkd.robotTrack.utils.UserThreadLocal;
import com.zkd.robotTrack.vo.ErrorResult;
import com.zkd.robotTrack.vo.RunParamVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("run")
public class RunController {

    @Autowired
    private BaiduService baiduService;

    @Autowired
    private UserLocationService userLocationService;

    /**
     * 上报地理位置
     * @param routeId   路线Id
     * @param runParamVo    请求参数，其中包含了经纬度和速度
     * @return
     */
    @PostMapping("{routeId}")
    public Object run(@PathVariable("routeId")String routeId, RunParamVo runParamVo){
        Boolean result = this.baiduService.uploadLocation(routeId,runParamVo);
        if (result){
            //异步更新自己的位置数据
            Long userId = UserThreadLocal.get();
            Double longitude = runParamVo.getLongitude();
            Double latitude = runParamVo.getLatitude();
            this.userLocationService.uploadLocation(userId,longitude,latitude);
            return null;
        }
        return ErrorResult.builder()
                .errCode("500")
                .errMessage("上报地理位置失败").build();
    }
}
