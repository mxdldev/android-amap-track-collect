package com.geduo.datacollect.collect;

import com.geduo.datacollect.bean.Trip;

import java.util.List;

/**
 * Description: <LocalTripManage><br>
 * Author: gxl<br>
 * Date: 2018/12/6<br>
 * Version: V1.0.0<br>
 * Update: <br>
 */
public class LocalTripManage {
  private List<Trip> trips;// 本地多个对应的Trip对象,初始化时读区目录建立。;
  // 返回一个Trip，并添加至trips属性

  public Trip createTrip() {
    return new Trip();
  };

  // 上传对象至服务器。
  public void upload() {

  }

  // 删除,判断当前状态进行本地。
  public void remove(Trip trip) {

  }

  // 根据tripID从云端下载至本地。
  public void download(String TripID) {

  }
}
