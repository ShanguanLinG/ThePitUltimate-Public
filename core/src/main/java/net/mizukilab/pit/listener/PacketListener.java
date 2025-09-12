package net.mizukilab.pit.listener;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.event.PotionAddEvent;
import net.minecraft.server.v1_8_R3.*;
import net.mizukilab.pit.events.impl.major.RedVSBlueEvent;
import net.mizukilab.pit.util.item.ItemBuilder;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import lombok.SneakyThrows;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;

/**
 * @Author: EmptyIrony
 * @Date: 2021/2/4 14:56
 */
@Skip
public class PacketListener extends PacketAdapter {
    public PacketListener() {
        super(ThePit.getInstance(), PacketType.Play.Server.ENTITY_EQUIPMENT, PacketType.Play.Server.ENTITY_EFFECT,PacketType.Play.Client.CUSTOM_PAYLOAD);//PacketType.Play.Server.SCOREBOARD_TEAM, PacketType.Play.Server.PLAYER_INFO);
    }

    @Override
    @SneakyThrows
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        final Player player = event.getPlayer();
        if (packet.getType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
            processRvBPackets((Packet<?>) packet.getHandle());
        } else if (packet.getType() == PacketType.Play.Server.ENTITY_EFFECT) {
            processPotionAddEvent(packet, player);
       }
//            processPlayerInfo(player,packet);
//        } else if(packet.getType() == PacketType.Play.Server.SCOREBOARD_TEAM) {
//            processPlayerTeam(player,packet);
//        }
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        if (packet.getType() == PacketType.Play.Client.CUSTOM_PAYLOAD){
            process(event);
        }
    }

    public void process(PacketEvent event){
        Object handle = event.getPacket().getHandle();
    }

    private static void processPotionAddEvent(PacketContainer container, Player player) {
        final Integer entityId = container.getIntegers().read(0);
        if (player.getEntityId() != entityId) return;

        final PotionEffectType potion = PotionEffectType.getById(container.getBytes().read(0));
        final Byte level = container.getBytes().read(1);
        final Integer duration = container.getIntegers().read(1);
        final boolean hide = container.getBytes().read(2) == 1;

        Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> {
            PotionAddEvent potionEvent = new PotionAddEvent(player, new PotionEffect(potion, level, duration, hide));
            potionEvent.callEvent();

            if (potionEvent.isCancelled()) {
                Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> {
                    player.removePotionEffect(potion);
                }, 1L);
            }
        }, 1L);
    }

    private static void processRvBPackets(Packet<?> packet) throws NoSuchFieldException, IllegalAccessException {
        if ("red_vs_blue".equals(ThePit.getInstance().getEventFactory().getActiveEpicEventName())) {
            PacketPlayOutEntityEquipment equipment = (PacketPlayOutEntityEquipment) packet;
            Class<? extends PacketPlayOutEntityEquipment> clazz = equipment.getClass();
            Field b = clazz.getDeclaredField("b");
            b.setAccessible(true);
            Field c = clazz.getDeclaredField("c");
            c.setAccessible(true);
            Field a = clazz.getDeclaredField("a");
            a.setAccessible(true);
            if ((int) (b.get(packet)) == 4) {
                RedVSBlueEvent activeEpicEvent = (RedVSBlueEvent) ThePit.getInstance().getEventFactory().getActiveEpicEvent();
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (target.getEntityId() == (int) a.get(packet)) {
                        if (activeEpicEvent.getRedTeam().contains(target.getUniqueId())) {
                            c.set(packet, CraftItemStack.asNMSCopy(new ItemBuilder(Material.WOOL).durability(14).build()));
                        } else if (activeEpicEvent.getBlueTeam().contains(target.getUniqueId())) {
                            c.set(packet, CraftItemStack.asNMSCopy(new ItemBuilder(Material.WOOL).durability(11).build()));
                        }
                        break;
                    }
                }
            }
        }
    }
}