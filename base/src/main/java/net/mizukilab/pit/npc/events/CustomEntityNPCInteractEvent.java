package net.mizukilab.pit.npc.events;

import net.mizukilab.pit.npc.AbstractCustomEntityNPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 自定义生物NPC交互事件
 * 当玩家与自定义生物NPC交互时触发
 * @Author: AI Assistant
 * @Date: 2024/12/19
 */
public class CustomEntityNPCInteractEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    
    private boolean cancelled = false;
    
    private final Player player;
    private final AbstractCustomEntityNPC npc;
    private final Entity entity;

    public CustomEntityNPCInteractEvent(Player player, AbstractCustomEntityNPC npc, Entity entity) {
        this.player = player;
        this.npc = npc;
        this.entity = entity;
    }

    /**
     * 获取与NPC交互的玩家
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 获取被交互的自定义NPC
     */
    public AbstractCustomEntityNPC getNPC() {
        return npc;
    }

    /**
     * 获取NPC对应的实体
     */
    public Entity getEntity() {
        return entity;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
} 