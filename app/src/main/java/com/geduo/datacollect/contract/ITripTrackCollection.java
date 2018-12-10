package com.geduo.datacollect.contract;

/**
 * Description: <出游数据采集协议><br>
 * Author: gxl<br>
 * Date: 2018/12/6<br>
 * Version: V1.0.0<br>
 * Update: <br>
 */
public interface ITripTrackCollection {
  // 开始收集
  void start();

  // 停止收集
  void stop();

  // 暂停收集
  void pause();

  // 保存当前的状态，和当前活动的Trip对象id至本地文件
  void saveHoldStatus();

  //销毁
  void destory();
}
