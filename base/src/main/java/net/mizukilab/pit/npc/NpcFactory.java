package net.mizukilab.pit.npc;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.event.PitProfileLoadedEvent;
import lombok.Getter;
import lombok.SneakyThrows;
import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.api.events.NPCInteractEvent;
import net.jitse.npclib.api.state.NPCAnimation;
import net.jitse.npclib.api.state.NPCSlot;
import net.jitse.npclib.nms.v1_8_R3.NPC_v1_8_R3;
import net.mizukilab.pit.npc.runnable.NpcRunnable;
import net.mizukilab.pit.parm.AutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Author: EmptyIrony
 * @Date: 2020/12/30 22:35
 */
@AutoRegister
public class NpcFactory implements Listener {
    NPCLib npcLib = new NPCLib(ThePit.getInstance());
    @Getter
    private final List<AbstractPitNPC> pitNpc = new ArrayList<>();
    public boolean hasNPC(Entity e) {
        for (AbstractPitNPC abstractPitNPC : pitNpc) {
            NPC_v1_8_R3 npc = (NPC_v1_8_R3) abstractPitNPC.getNpc();
            if (npc.getEntityId() ==e.getEntityId()) {
                return true;
            }
        }
        return false;

    }
    public void reload(){
        pitNpc.forEach(i -> {
            initNpc(i.getNpc(),i);
            ((NPC_v1_8_R3)i.getNpc()).createPackets();
        });
        Bukkit.getOnlinePlayers().forEach(this::show);
    }
    public void show(Player player){
        pitNpc.forEach(i -> {
            i.getNpc().create();
            if(!i.getNpc().isShown(player)) {
                i.getNpc().show(player);
            } else {
                i.getNpc().hide(player);
                i.getNpc().show(player);
            }
            i.getNpc()
                    .setText(player, i.getNpcTextLine(player));
        });
    }
    @SneakyThrows
    public void init(Collection<Class<? extends AbstractPitNPC>> classes) {;
        for (Class<?> clazz : classes) {
            if (!ThePit.isDEBUG_SERVER()) {
                if (clazz.getName().contains("InfinityItemNPC")) {
                    continue;
                }

            }
            if (AbstractPitNPC.class.isAssignableFrom(clazz)) {
                AbstractPitNPC abstractPitNPC = (AbstractPitNPC) clazz.getConstructor().newInstance();

                NPC npc = npcLib.createNPC();
                initNpc(npc, abstractPitNPC);

                pitNpc.add(abstractPitNPC);
            }
        }
        new NpcRunnable().runTaskTimerAsynchronously(ThePit.getInstance(), 20, 20);
    }

    private static void initNpc(NPC npc, AbstractPitNPC abstractPitNPC) {
        npc.setLocation(abstractPitNPC.getNpcSpawnLocation());

        if (abstractPitNPC.getNpcHeldItem() != null) {
            npc.setItem(NPCSlot.MAINHAND, abstractPitNPC.getNpcHeldItem());
        }
        if (abstractPitNPC.getAnimation() != null){
            npc.playAnimation(abstractPitNPC.getAnimation());
        }
        abstractPitNPC.setNpc(npc);
        abstractPitNPC.initSkin(npc);
    }

    @EventHandler
    @SneakyThrows
    public void onPlayerJoin(PitProfileLoadedEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayerProfile().getPlayerUuid());
        if (player == null || !player.isOnline()) {
            return;
        }
        show(player);

    }

    @EventHandler
    public void onInteract(NPCInteractEvent event) {
        for (AbstractPitNPC abstractPitNPC : pitNpc) {
            if (abstractPitNPC.getNpc().getUniqueId().equals(event.getNPC().getUniqueId())) {
                abstractPitNPC.handlePlayerInteract(event.getWhoClicked());
            }
        }
    }

}
