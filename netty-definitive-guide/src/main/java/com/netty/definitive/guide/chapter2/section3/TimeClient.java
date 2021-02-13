package com.netty.definitive.guide.chapter2.section3;

/**
 * @author dongzonglei
 * @description
 * @date 2019-04-14 21:45
 */
public class TimeClient {

    public static void main(String args[]) throws Exception {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        new Thread(new TimeClientHandle("127.0.0.1", port)).start();
    }
}
