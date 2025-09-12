package net.mizukilab.pit.config;

import lombok.Getter;
import lombok.Setter;
import net.mizukilab.pit.util.configuration.Configuration;
import net.mizukilab.pit.util.configuration.annotations.ConfigData;
import net.mizukilab.pit.util.configuration.annotations.ConfigSerializer;
import net.mizukilab.pit.util.configuration.serializer.LocationSerializer;
import net.mizukilab.pit.util.configuration.serializer.LocationsSerializer;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: KleeLoveLife
 * @Date: 2025/5/3 16:39
 */
public class PitWorldConfig extends Configuration {
    @Setter
    @Getter
    @ConfigData(
            path = "worldName"
    )
    private String worldName;
    @Setter
    @Getter
    @ConfigData(
            path = "id"
    )
    private int id;
    @Setter
    @Getter
    @ConfigData(
            path = "arenaHighestY"
    )
    private int arenaHighestY;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.spawn"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private List<Location> spawnLocations = new ArrayList<>();
    @Setter
    @Getter
    @ConfigData(
            path = "loc.npc.shop"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location shopNpcLocation;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.npc.quest"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location questNpcLocation;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.npc.perk"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location perkNpcLocation;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.npc.prestige"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location prestigeNpcLocation;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.npc.status"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location statusNpcLocation;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.npc.keeper"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location keeperNpcLocation;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.npc.mail"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location mailNpcLocation;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.npc.warehouse"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location warehouseNpcLocation;

    @Setter
    @Getter
    @ConfigData(
            path = "loc.npc.genesis_demon"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location genesisDemonNpcLocation;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.npc.genesis_angel"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location genesisAngelNpcLocation;

    @Setter
    @Getter
    @ConfigData(
            path = "loc.npc.sewers_fish"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location sewersFishNpcLocation;

    @Setter
    @Getter
    @ConfigData(
            path = "loc.hologram.spawn"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location hologramLocation;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.hologram.leaderBoard"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location leaderBoardHologram;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.hologram.helper"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location helperHologramLocation;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.region.pitA"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location pitLocA;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.region.pitB"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location pitLocB;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.region.enchant"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location enchantLocation;

    @Setter
    @Getter
    @ConfigData(
            path = "loc.region.sewers"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location sewersLocation;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.hamburger.shop"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location hamburgerShopLoc;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.hamburger.villager.a"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private List<Location> hamburgerNpcLocA = new ArrayList<>();
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.spire.spireLoc"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location spireLoc;
    //每层塔地面中心坐标 (1~9)
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.spire.spireFloorLocations"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private List<Location> spireFloorLoc = new ArrayList<>();
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.spire.spireFloorY"
    )
    private List<Integer> floorY = new ArrayList<>();
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.hamburger.villager.a-offer" //the villager who offer the ham
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location hamburgerOfferNpcLocA;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.rage.middle" //the middle point of rage pit
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location ragePitMiddle;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.rage.radius" //the radius of rage pit
    )
    private int ragePitRadius;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.rage.height" //the height of rage pit
    )
    private int ragePitHeight;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.portal.posA" //Middle portal posA
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location portalPosA;

    @Setter
    @Getter
    @ConfigData(
            path = "loc.portal.posB" //Middle portal posA
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location portalPosB;
    @Setter
    @Getter
    @ConfigData(path = "loc.events.dragon-egg.loc")
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location dragonEggLoc;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.cake.a.posA" //cake posA
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location cakeZoneAPosA;


    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.cake.a.posB" //cake posB
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location cakeZoneAPosB;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.cake.b.posA" //cake posA
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location cakeZoneBPosA;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.cake.b.posB" //cake posB
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location cakeZoneBPosB;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.cake.c.posA" //cake posA
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location cakeZoneCPosA;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.cake.c.posB" //cake posB
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location cakeZoneCPosB;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.cake.d.posA" //cake posA
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location cakeZoneDPosA;
    @Setter
    @Getter
    @ConfigData(
            path = "loc.events.cake.d.posB" //cake posB
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private Location cakeZoneDPosB;

    @Setter
    @Getter
    @ConfigData(
            path = "loc.Genesis.angel"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private List<Location> angelSpawns = new ArrayList<>();

    @Setter
    @Getter
    @ConfigData(
            path = "loc.Genesis.demon"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private List<Location> demonSpawns = new ArrayList<>();

    @Setter
    @Getter
    @ConfigData(
            path = "loc.packages"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private List<Location> packageLocations = new ArrayList<>();
    @Setter
    @Getter
    @ConfigData(
            path = "debug.infinityNpcLoc"
    )
    private Location infinityNpcLocation;
    @Setter
    @Getter
    @ConfigData(
            path = "debug.ienchantNpcLoc"
    )
    private Location enchantNpcLocation;

    @Setter
    @Getter
    @ConfigData(
            path = "loc.Sewers.chests"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private List<Location> sewersChestsLocations = new ArrayList<>();

    @Setter
    @Getter
    @ConfigData(
            path = "loc.squads"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private List<Location> squadsLocations = new ArrayList<>();

    @Setter
    @Getter
    @ConfigData(
            path = "loc.blockHead"
    )
    @ConfigSerializer(serializer = LocationSerializer.class)
    private List<Location> blockHeadLocations = new ArrayList<>();

    PitGlobalConfig global;
    public PitWorldConfig(PitGlobalConfig globalConfig, JavaPlugin plugin, String worldYml,String directory) {
        super(plugin,worldYml,directory,false);
        this.global = globalConfig;
    }

    public boolean isGenesisEnable() {
        try {
            return System.currentTimeMillis() >= getGenesisStartTime() && System.currentTimeMillis() < getGenesisEndTime();
        } catch (Exception ignored) {
            return false;
        }
    }

    @Setter
    @Getter
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日HH时mm分");

    public long getGenesisStartTime() {
        return global.getGenesisStartDate();
    }

    public long getGenesisOriginalEndTime() {
        return getGenesisStartTime() + 16 * 24 * 60 * 60 * 1000;
    }

    public long getGenesisEndTime() {
        long endTime = getGenesisOriginalEndTime();
        while (endTime < System.currentTimeMillis()) {
            endTime += 56 * 24 * 60 * 60 * 1000L;
        }
        return endTime;
    }

    //Season X: From Season X-1 End To Season X End
    public int getGenesisSeason() {
        int season = 1;
        long endTime = getGenesisOriginalEndTime();
        while (endTime < System.currentTimeMillis()) {
            endTime += 56L * 24 * 60 * 60 * 1000;
            season++;
        }
        return season;
    }


}
