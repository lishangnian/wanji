package com.enjoy.sweeper.util;

import com.enjoy.sweeper.entity.DataStorage;

import java.util.Map;

public class MyStringUtil {

    public static String subscribeTopicStr(String topicStr) {
        return "{\"op\":\"subscribe\",\"topic\":\"" + topicStr + "\"}";
    }

    public static String sendDataStr(String sendTopicStr, Map<String, Object> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            stringBuilder.append(key);
            Object valueObj = entry.getValue();
            if (valueObj instanceof String) {
                stringBuilder.append("\"").append(valueObj).append("\"");
            } else {
                stringBuilder.append(valueObj);
            }
        }
        String data = stringBuilder.toString();
        return "{\"op\":\"publish\",\"topic\":\"" + sendTopicStr + "\",\"msg\":{" + data + "}}";
    }


}
