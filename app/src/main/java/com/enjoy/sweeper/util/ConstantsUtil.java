package com.enjoy.sweeper.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ConstantsUtil {

    /*****定义线程池****/
    public static ExecutorService executorService = new ThreadPoolExecutor(24,24,0L,
            TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));


    /**
     * 获取字符串中数字部分
     * @param string
     * @return
     */
    public static String getNumStrFromStr(String string){
        String regEx = "[^0-9]"; //正则表达式，找到数字
        Pattern p = Pattern.compile(regEx);
        return p.matcher(string).replaceAll("").trim();
    }

}
