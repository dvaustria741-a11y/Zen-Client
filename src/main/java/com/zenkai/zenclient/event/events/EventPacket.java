package com.zenkai.zenclient.event.events;

import com.zenkai.zenclient.event.Event;
import net.minecraft.network.Packet;

/** Fired when a packet is sent to or received from the server. Cancellable. */
public class EventPacket extends Event {

    public enum Direction { SEND, RECEIVE }

    private final Direction direction;
    private       Packet<?> packet;

    public EventPacket(Direction direction, Packet<?> packet) {
        this.direction = direction;
        this.packet    = packet;
    }

    @Override
    public boolean isCancellable() { return true; }

    public Direction getDirection() { return direction; }
    public boolean   isSend()       { return direction == Direction.SEND; }
    public boolean   isReceive()    { return direction == Direction.RECEIVE; }

    public Packet<?> getPacket()            { return packet; }
    public void      setPacket(Packet<?> p) { this.packet = p; }
}
