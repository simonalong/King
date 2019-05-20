//package com.simon.king.core.http;
//
//import com.simon.neo.NeoMap;
//import org.springframework.stereotype.Service;
//
///**
// * http的客户端目前，支持http1.1中的如下的一些方法
// * @author zhouzhenyong
// * @since 2019/5/20 下午3:38
// */
//@Service
//public interface HttpModel {
//
//    String url(String url);
//
//    HttpModel headers(NeoMap headMap);
//    HttpModel body(NeoMap bodyMap);
//
//    String get();
//    NeoMap head();
//    String post();
//
//    String put();
//    String patch();
//    String delete();
//
////    String connect();
////    String options();
////    String trace();
//}