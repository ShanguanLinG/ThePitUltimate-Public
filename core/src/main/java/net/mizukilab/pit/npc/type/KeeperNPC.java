package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.mizukilab.pit.menu.hub.HubMenu;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author: Misoryan
 * @Created_In: 2021/2/6 16:28
 */

public class KeeperNPC extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "keeper";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> lines = new ObjectArrayList<>(3);
        lines.add("&6&l看门狗");
        lines.add("&7玩累了？点击返回大厅");
        return lines;
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getKeeperNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin(
                "eyJ0aW1lc3RhbXAiOjE1ODExNjA0NjQyMDIsInByb2ZpbGVJZCI6ImQ2MGYzNDczNmExMjQ3YTI5YjgyY2M3MTViMDA0OGRiIiwicHJvZmlsZU5hbWUiOiJCSl9EYW5pZWwiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzU1NmE2MTUzMDhiYzljM2U1YThiMGVhYzY4MjQyNjY4MzJjMjRlMDFiN2Y0NTUyNjc2OTViYmJkNTg2MGRmZCJ9fX0=",
                "j7unPa7ruQrwSJRf1SZXUq0W6js6+isqWG19zlEBS3QQ62M5GUMribRLfQ3F9CpNe0jOrsCGqepepdrnJLEZg8InudyHxdIp8L3+owFy+OK37v/E3iiwitTnwMHEMSVKFj3Db2OKsebShUXT2oUBz5SaDtK/ofxV7AKp4gJKWeDCfpnnlZg827mSH6OK3Wz6hFTTo9eUANHFCoZvGJyEnuK5Bp4CsBOmwE7npwUYONtTsWODGltYKmIC8B9CynqBzE3OiLGGRNzJW63zXACA7CBPglNAMo70xfNBXIUvYRb2vE4gXGVyDTy0IE1nRoWMWTIocAJFuZ8kxoIiCfJmvAv89Ei05MvaDp0Fhqc4aPm8QcBILagMiUI7RaFiWDcDvAiCy7sTxDGXAl8qfAWW+ID3qE/coc6K2wWhRBdsTbCY2YRPwxAZBKgDvc+0OSihAjiAHF7jamEZ5g/meUv2ih1haeXZL57MD5ES7N2yiWHjx/9s0GabHBkrap8vaJpNXu4SnIerrl2e2ClVKE/6iFY6B5hvvcFu+bYGoPpHd1AYc//brbUZ7hE4LPw7TuS17+RUknWb51uVaeAKcqVtnhqdN6iuM9J1x/7XQSrIoH+iVfQGA2IRtWA2uWOPxivTAljUJC0w04oK6qHw+16W3xv+OGC6iM4m3MxP/1RTlNQ="
        );
    }

    @Override
    public void handlePlayerInteract(Player player) {
        new HubMenu().openMenu(player);
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return null;
    }
}
