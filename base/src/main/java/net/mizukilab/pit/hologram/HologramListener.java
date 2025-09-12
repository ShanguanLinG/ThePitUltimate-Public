package net.mizukilab.pit.hologram;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.event.PitProfileLoadedEvent;
import cn.charlotte.pit.util.hologram.Hologram;
import cn.charlotte.pit.util.hologram.HologramAPI;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mizukilab.pit.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/4 13:09
 */
//@AutoRegister
public class HologramListener implements Listener {

    protected final static Map<UUID, PlayerHologram> hologramMap = new HashMap<>();

    @EventHandler
    public void onLoad(PitProfileLoadedEvent event) {
        Bukkit.getScheduler()
                .runTaskAsynchronously(ThePit.getInstance(), () -> {
                    Player player = Bukkit.getPlayer(event.getPlayerProfile().getPlayerUuid());
                    if (player != null && player.isOnline()) {
                        PlayerHologram playerHologram = new PlayerHologram(new ObjectArrayList<>());
                        hologramMap.put(player.getUniqueId(), playerHologram);

                        for (AbstractHologram hologram : ThePit.getInstance().getHologramFactory().loopHologram) {
                            handleHologramCreate(player, playerHologram, hologram);
                        }

                        for (AbstractHologram hologram : ThePit.getInstance().getHologramFactory().normalHologram) {
                            handleHologramCreate(player, playerHologram, hologram);
                        }
                    }
                });
    }

    private void handleHologramCreate(Player player, PlayerHologram playerHologram, AbstractHologram hologram) {
        List<Hologram> holograms = new ArrayList<>();
        for (int i = 0; i < hologram.getText(player).size(); i++) {
            String text = hologram.getText(player).get(i);
            Hologram holo = HologramAPI.createHologram(hologram.getLocation().clone().add(0, -i * hologram.getHologramHighInterval(), 0), CC.translate(text));
            holo.spawn(Collections.singletonList(player));
            holograms.add(holo);
        }
        playerHologram.hologramData.put(hologram.getInternalName(), new HologramData(holograms, hologram.getInternalName()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler()
                .runTaskAsynchronously(
                        ThePit.getInstance()
                        , () -> {
                            PlayerHologram playerHologram = hologramMap.remove(event.getPlayer().getUniqueId());
                            if (playerHologram == null) {
                                return;
                            }
                            for (HologramData datum : playerHologram.hologramData.values()) {
                                for (Hologram hologram : datum.holograms) {
                                    hologram.deSpawn();
                                }
                            }
                        });
    }

    public static class PlayerHologram {

        private final Map<String, HologramData> hologramData;

        public PlayerHologram(List<HologramData> hologramData) {
            this.hologramData = new HashMap<>();
            for (HologramData data : hologramData) {
                this.hologramData.put(data.internalName, data);
            }
        }

        public Map<String, HologramData> getHologramData() {
            return this.hologramData;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof PlayerHologram)) return false;
            final PlayerHologram other = (PlayerHologram) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$hologramData = this.getHologramData();
            final Object other$hologramData = other.getHologramData();
            if (this$hologramData == null ? other$hologramData != null : !this$hologramData.equals(other$hologramData))
                return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof PlayerHologram;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $hologramData = this.getHologramData();
            result = result * PRIME + ($hologramData == null ? 43 : $hologramData.hashCode());
            return result;
        }

        public String toString() {
            return "HologramListener.PlayerHologram(hologramData=" + this.getHologramData() + ")";
        }
    }

    public static class HologramData {

        private List<Hologram> holograms;
        private String internalName;

        public HologramData(List<Hologram> holograms, String internalName) {
            this.holograms = holograms;
            this.internalName = internalName;
        }

        public List<Hologram> getHolograms() {
            return this.holograms;
        }

        public void setHolograms(List<Hologram> holograms) {
            this.holograms = holograms;
        }

        public String getInternalName() {
            return this.internalName;
        }

        public void setInternalName(String internalName) {
            this.internalName = internalName;
        }

        public String toString() {
            return "HologramListener.HologramData(holograms=" + this.getHolograms() + ", internalName=" + this.getInternalName() + ")";
        }
    }
}
