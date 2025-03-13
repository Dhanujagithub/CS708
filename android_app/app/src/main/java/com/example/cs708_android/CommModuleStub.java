package com.example.cs708_android;

public class CommModuleStub {
    private final static String SERVER_IP ="http://192.168.10.126:5000/" ;/* "http://10.119.133.118:5000/";"http://192.168.0.140:5000/"*/

    public static void callTargetDetectionApi(String command){
     //   sourceActivity.selectTargetScreenArea("{'remote',[0,0,100,100]}");
    }

    public static void callGestureDetectionApi(int segment_code) {
        if(segment_code == 1) {
           // sourceActivity.respondToGesture("Nodding");
        } else {
          //  sourceActivity.respondToGesture("Shaking");
        }
    }
}
