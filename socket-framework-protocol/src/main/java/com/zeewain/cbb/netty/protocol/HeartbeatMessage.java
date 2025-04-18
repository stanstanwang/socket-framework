package com.zeewain.cbb.netty.protocol;

/**
 * @author stan
 * @description
 * @date 2022/7/19
 */
public class HeartbeatMessage {

    private boolean ping = true;

    public static final HeartbeatMessage PING = new HeartbeatMessage(true);
    /**
     * The constant PONG.
     */
    public static final HeartbeatMessage PONG = new HeartbeatMessage(false);


    public HeartbeatMessage() {
    }

    private HeartbeatMessage(boolean ping) {
        this.ping = ping;
    }


    @Override
    public String toString() {
        return this.ping ? "services ping" : "services pong";
    }

    public boolean isPing() {
        return ping;
    }

    public void setPing(boolean ping) {
        this.ping = ping;
    }

}
