package net.mizukilab.pit.util;

import net.minecraft.server.v1_8_R3.*;
import nya.Skip;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * 粒子效果工具类
 * 用于神话之井附魔动画
 */
@Skip
public class ParticleUtil {

    /**
     * 发送基础粒子效果
     */
    public static void spawnParticle(Player player, EnumParticle particle, Location location, 
                                   int count, double offsetX, double offsetY, double offsetZ, double speed) {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
            particle, true,
            (float) location.getX(), (float) location.getY(), (float) location.getZ(),
            (float) offsetX, (float) offsetY, (float) offsetZ,
            (float) speed, count
        );
        
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    /**
     * 创建螺旋上升的粒子效果
     */
    public static void createSpiralParticles(Player player, Location center, int colorR, int colorG, int colorB) {
        for (int i = 0; i < 20; i++) {
            double angle = i * 0.3;
            double radius = 1.5 + i * 0.05;
            double height = i * 0.1;
            
            double x = center.getX() + Math.cos(angle) * radius;
            double y = center.getY() + height;
            double z = center.getZ() + Math.sin(angle) * radius;
            
            Location particleLocation = new Location(center.getWorld(), x, y, z);
            
            // 彩色粒子效果
            spawnColoredParticle(player, particleLocation, colorR, colorG, colorB);
        }
    }

    /**
     * 创建温和的魔法粒子效果（替代爆炸）
     */
    public static void createMagicParticles(Player player, Location center, int colorR, int colorG, int colorB) {
        // 温和的魔法光环
        for (int i = 0; i < 360; i += 15) {
            double angle = Math.toRadians(i);
            double radius = 1.0;
            
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            
            Location particleLocation = new Location(center.getWorld(), x, center.getY() + 0.5, z);
            spawnColoredParticle(player, particleLocation, colorR, colorG, colorB);
        }
        
        // 附魔台粒子效果
        spawnParticle(player, EnumParticle.ENCHANTMENT_TABLE, center, 10, 0.5, 0.5, 0.5, 0.1);
    }

    /**
     * 创建最终完成粒子效果（温和版本）
     */
    public static void createFinalBurstParticles(Player player, Location center, int colorR, int colorG, int colorB) {
        // 温和的完成效果
        spawnParticle(player, EnumParticle.FIREWORKS_SPARK, center, 20, 1.0, 1.0, 1.0, 0.2);
        
        // 彩色魔法粒子环
        for (int i = 0; i < 360; i += 10) {
            double angle = Math.toRadians(i);
            double radius = 1.5;
            
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            
            Location ringLocation = new Location(center.getWorld(), x, center.getY() + 1, z);
            spawnColoredParticle(player, ringLocation, colorR, colorG, colorB);
        }
        
        // 上升的魔法光柱
        for (int i = 0; i < 15; i++) {
            Location pillarLocation = center.clone().add(0, i * 0.15, 0);
            spawnParticle(player, EnumParticle.ENCHANTMENT_TABLE, pillarLocation, 3, 0.2, 0.1, 0.2, 0.05);
        }
        
        // 魔法星尘效果
        for (int i = 0; i < 20; i++) {
            Vector direction = new Vector(
                (Math.random() - 0.5) * 1.5,
                Math.random() * 1.5,
                (Math.random() - 0.5) * 1.5
            );
            
            Location stardustLocation = center.clone().add(direction);
            spawnParticle(player, EnumParticle.SPELL_WITCH, stardustLocation, 1, 0.1, 0.1, 0.1, 0.02);
        }
    }

    /**
     * 发送彩色粒子（使用红石粒子模拟）
     */
    private static void spawnColoredParticle(Player player, Location location, int r, int g, int b) {
        // 使用红石粒子来创建彩色效果
        float red = r / 255.0f;
        float green = g / 255.0f;
        float blue = b / 255.0f;
        
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
            EnumParticle.REDSTONE, true,
            (float) location.getX(), (float) location.getY(), (float) location.getZ(),
            red, green, blue,
            1.0f, 0
        );
        
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    /**
     * 创建魔法阵粒子效果
     */
    public static void createMagicCircleParticles(Player player, Location center, double radius, int colorR, int colorG, int colorB) {
        // 外圈魔法阵
        for (int i = 0; i < 360; i += 5) {
            double angle = Math.toRadians(i);
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            
            Location circleLocation = new Location(center.getWorld(), x, center.getY(), z);
            spawnColoredParticle(player, circleLocation, colorR, colorG, colorB);
        }
        
        // 内圈魔法阵
        for (int i = 0; i < 360; i += 10) {
            double angle = Math.toRadians(i);
            double x = center.getX() + Math.cos(angle) * (radius * 0.5);
            double z = center.getZ() + Math.sin(angle) * (radius * 0.5);
            
            Location innerCircleLocation = new Location(center.getWorld(), x, center.getY(), z);
            spawnParticle(player, EnumParticle.ENCHANTMENT_TABLE, innerCircleLocation, 1, 0, 0, 0, 0);
        }
        
        // 魔法符文（星形）
        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(i * 72); // 五角星
            double x = center.getX() + Math.cos(angle) * (radius * 0.8);
            double z = center.getZ() + Math.sin(angle) * (radius * 0.8);
            
            Location runeLocation = new Location(center.getWorld(), x, center.getY() + 0.1, z);
            spawnParticle(player, EnumParticle.SPELL_WITCH, runeLocation, 3, 0.1, 0.1, 0.1, 0.05);
        }
    }

    /**
     * 根据神话颜色获取对应的RGB值
     */
    public static int[] getColorFromMythicColor(String colorName) {
        return switch (colorName.toUpperCase()) {
            case "RED" -> new int[]{255, 85, 85};
            case "ORANGE" -> new int[]{255, 170, 0};
            case "YELLOW" -> new int[]{255, 255, 85};
            case "GREEN" -> new int[]{85, 255, 85};
            case "BLUE" -> new int[]{85, 85, 255};
            case "DARK" -> new int[]{85, 0, 170};
            case "RAGE" -> new int[]{170, 0, 0};
            default -> new int[]{255, 255, 255}; // 白色作为默认
        };
    }
} 