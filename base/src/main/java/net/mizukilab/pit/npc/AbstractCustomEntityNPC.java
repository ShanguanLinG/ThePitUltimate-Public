package net.mizukilab.pit.npc;

import net.mizukilab.pit.util.chat.CC;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Araykal
 * @Date: 2025/6/21
 */
public abstract class AbstractCustomEntityNPC {

    private Entity entity;

    //NPC 内部名称
    public abstract String getNpcInternalName();

    //NPC 显示名称 可以自行加空格
    public abstract List<String> getNpcDisplayName(Player player);

    public List<String> getNpcTextLine(Player player) {
        List<String> displayName = this.getNpcDisplayName(player);
        List<String> text = new ArrayList<>();
        for (String s : displayName) {
            text.add(CC.translate(s));
        }
        return text;
    }

    //NPC 生成位置
    public abstract Location getNpcSpawnLocation();

    //NPC 实体类型
    public abstract EntityType getEntityType();

    //玩家交互处理
    public abstract void handlePlayerInteract(Player player);

    //npc 手持物品
    public ItemStack getNpcHeldItem() {
        return null;
    }

    //自定义实体属性设置
    public abstract void customizeEntity(Entity entity);
    

    //是否允许实体受到伤害
    public boolean canTakeDamage() {
        return false;
    }

    //是否允许实体移动
    public boolean canMove() {
        return false;
    }

    //是否显示名称标签
    public  boolean showNameTag(){
        return true;
    };

    //实体最大生命值
    public double getMaxHealth() {
        return 10.0;
    }

    ;

    //实体是否为成年体 (仅对支持年龄的实体有效)
    public boolean isAdult(){
        return true;
    };

    public Entity getEntity() {
        return this.entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }


    public void initializeEntity(Entity entity) {

        entity.setCustomName("");
        entity.setCustomNameVisible(false);

        if (entity instanceof org.bukkit.entity.LivingEntity) {
            org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) entity;
            livingEntity.setMaxHealth(getMaxHealth());
            livingEntity.setHealth(getMaxHealth());
        }

        customizeEntity(entity);
    }
} 