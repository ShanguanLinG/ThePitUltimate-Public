package cn.charlotte.pit.events;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/9 13:11
 */
public abstract class AbstractEvent {

    @Setter @Getter
    private boolean isActive = false;

    public abstract String getEventInternalName();

    public abstract String getEventName();

    public abstract int requireOnline();

    public abstract void onActive();

    public abstract void onInactive();

}
