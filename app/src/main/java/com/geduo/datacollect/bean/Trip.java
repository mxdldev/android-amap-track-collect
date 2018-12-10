package com.geduo.datacollect.bean;

/**
 * Description: <Trip><br>
 * Author: gxl<br>
 * Date: 2018/12/6<br>
 * Version: V1.0.0<br>
 * Update: <br>
 */
public class Trip {
  private String tripID;
  private String tracks;
  private String medias;

  void addTrackPoint(LocationInfo locationInfo) {};

  void addMedia(Media media) {};

  void removeMedia(Media media) {};
}
