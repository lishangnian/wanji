package com.enjoy.wanji.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Http请求操作方法
 */

public class HttpRequestUtil {

    private PrintWriter out = null;
    private BufferedReader in = null;

    private static String tag = "httpUtil";

    /**
     * post方法
     *
     * @param url
     * @param param
     * @return
     */

    public JSONObject post(String url, String param) {
        return httpRequest(url, param, "POST");
    }

    /**
     * put 方法
     *
     * @param url
     * @param param
     * @return
     */
    public JSONObject put(String url, String param) {
        return httpRequest(url, param, "PUT");
    }

    /**
     * @param url
     * @param param
     * @return
     */
    private JSONObject httpRequest(String url, String param, String method) {
        JSONObject resultMsg = null;
        try {
            URL realUrl;
            realUrl = new URL(url);
            Log.i(tag, "url:" + url + ",param:" + param);
            // 打开和URL之间的连接
            URLConnection url_conn = realUrl.openConnection();
            HttpURLConnection conn = (HttpURLConnection) url_conn;
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // 设置通用的请求属性
            conn.setRequestMethod(method);
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 设置连接超时
            conn.setConnectTimeout(30000);
            // 连接地址
            conn.connect();
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            if (param != null) {
                out.write(param);
            }
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "UTF-8"));
            String line;
            StringBuffer jsonResult = new StringBuffer();
            while ((line = in.readLine()) != null) {
                jsonResult.append(line);
            }
            //接收返回数据
            Log.i(tag, "返回数据:" + jsonResult);
            resultMsg = new JSONObject(jsonResult.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException ie) {
            Log.e(tag, "打开连接失败" + ie.getMessage() + ie);
        } catch (JSONException je) {
            Log.e(tag, "jsonObject对象异常" + je.getMessage() + je);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                Log.e(tag, e.getMessage());
            }
        }
        return resultMsg;
    }
}
