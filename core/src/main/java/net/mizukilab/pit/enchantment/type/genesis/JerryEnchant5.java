package net.mizukilab.pit.enchantment.type.genesis;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.events.genesis.GenesisTeam;
import net.mizukilab.pit.parm.listener.ITickTask;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.music.NBSDecoder;
import net.mizukilab.pit.util.music.PositionSongPlayer;
import net.mizukilab.pit.util.music.Song;
import io.irina.backports.utils.SWMRHashTable;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import spg.lgdev.handler.MovementHandler;
import spg.lgdev.iSpigot;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ArmorOnly
public class JerryEnchant5 extends AbstractEnchantment implements ITickTask, MovementHandler {

    private final Map<UUID, PositionSongPlayer> playerMap = new SWMRHashTable<>();
    private final Song demon;
    private final Song angle;

    @SneakyThrows
    public JerryEnchant5() {
        this.demon = NBSDecoder.parse(ThePit.getInstance().getClass().getClassLoader().getResourceAsStream("FloweringNight.nbs"));
        this.angle = NBSDecoder.parse(ThePit.getInstance().getClass().getClassLoader().getResourceAsStream("U.N.OWEN.nbs"));
        new BukkitRunnable() {
            @Override
            public void run() {
                Set<Map.Entry<UUID, PositionSongPlayer>> entries = new HashSet<>(playerMap.entrySet());
                for (Map.Entry<UUID, PositionSongPlayer> entry : entries) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player == null || !player.isOnline()) {
                        PositionSongPlayer remove = playerMap.remove(entry.getKey());
                        remove.setPlaying(false);
                        continue;
                    }
                    if (player.getInventory().getLeggings() == null || getItemEnchantLevel(player.getInventory().getLeggings()) == -1) {
                        PositionSongPlayer remove = playerMap.remove(entry.getKey());
                        remove.setPlaying(false);
                    }
                }
            }
        }.runTaskTimerAsynchronously(ThePit.getInstance(), 20, 20);

        try {
            iSpigot.INSTANCE.addMovementHandler(this);
        } catch (NoClassDefFoundError ignore) {

        }
    }

    @Override
    public String getEnchantName() {
        return "DJ #5";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 1;
    }

    @Override
    public String getNbtName() {
        return "jerry_genesis_s5";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.GENESIS;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7此附魔只能通过阵营活动 &b光&c暗&f派系 &7获得."
                + "/s&7向周围的玩家播放音乐."
                + "/s&7天使阵营播放音乐: &bU.N.オーエンは彼女なのか？"
                + "/s&7恶魔阵营播放音乐: &cフラワリングナイト";
    }

    @Override
    public void handle(int enchantLevel, Player target) {
        PositionSongPlayer songPlayer = playerMap.get(target.getUniqueId());
        if (songPlayer == null) {
            Song song;
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(target.getUniqueId());
            if (profile.getGenesisData().getTeam() == GenesisTeam.DEMON) {
                song = demon;
            } else if (profile.getGenesisData().getTeam() == GenesisTeam.ANGEL) {
                song = angle;
            } else {
                return;
            }
            PositionSongPlayer player = new PositionSongPlayer(song);
            player.setTargetLocation(target.getLocation());
            player.setAutoDestroy(false);
            player.setLoop(true);
            player.setPlaying(true);
            player.setVolume((byte) 0.5);

            this.playerMap.put(target.getUniqueId(), player);
        } else {
            target.getWorld().playEffect(target.getLocation().clone().add(0, 3, 0), Effect.NOTE, 1);
        }
    }

    @Override
    public int loopTick(int enchantLevel) {
        return 10;
    }

    @Override
    public void handleUpdateLocation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {
        PositionSongPlayer songPlayer = this.playerMap.get(player.getUniqueId());
        if (songPlayer != null) {
            songPlayer.setTargetLocation(player.getPlayer().getLocation());
        }
    }

    @Override
    public void handleUpdateRotation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {

    }
}
