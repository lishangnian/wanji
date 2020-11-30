package com.enjoy.sweeper.entity;

/**
 * Created by Administrator on 2016/10/20.
 */

public class Stoa {


    /**
     * gps : 116.34,40.12
     * speed : 30.32
     * heading : 156.33345
     */

    private StoaBean stoa;

    public StoaBean getStoa() {
        return stoa;
    }

    public void setStoa(StoaBean stoa) {
        this.stoa = stoa;
    }

    public static class StoaBean {
        private String gps;
        private String speed;
        private String heading;

        public String getGps() {
            return gps;
        }

        public void setGps(String gps) {
            this.gps = gps;
        }

        public String getSpeed() {
            return speed;
        }

        public void setSpeed(String speed) {
            this.speed = speed;
        }

        public String getHeading() {
            return heading;
        }

        public void setHeading(String heading) {
            this.heading = heading;
        }
    }
}
