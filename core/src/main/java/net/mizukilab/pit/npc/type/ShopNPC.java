package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.mizukilab.pit.menu.shop.ShopMenu;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.level.LevelUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/1 11:16
 */

public class ShopNPC extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "shop";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> lines = new ObjectArrayList<>(3);
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        lines.add("&b&l商店");
        if (profile.getLevel() >= 10) {
            lines.add("&e&l右键查看");
        } else {
            lines.add("&c在 " + LevelUtil.getLevelTag(profile.getPrestige(), 10) + " &c时解锁");
        }
        return lines;
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getShopNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin(
                "ewogICJ0aW1lc3RhbXAiIDogMTcwMTcxNzQzOTk1NSwKICAicHJvZmlsZUlkIiA6ICI2ZjhlYWI1MTVmNTc0MmRhOWYxZDYzMzY1ODAxMDU4YyIsCiAgInByb2ZpbGVOYW1lIiA6ICJDaW5kZXJGb3hfMjAwNiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yNWI5NTZiNzMwMzU3ODcwZGVmOGJjZWI4ZWMxY2JhYzA3ZTM1YjYwZGIyMDE0YzdmZGU4ZTM3ODM0NzllMTUiCiAgICB9CiAgfQp9",
                "Vg+BT3pvZOSQD0aegUo1Fw/HVKydOfmWDvH6apUMw4dsq9OTpWhXeEui2dy5xBZfzJWkm8MSBXaddIY6n6zGsoKtKae3Yw5+VWaCM+hVjZ8Dw1FiwBcMXiTeSVjuIsPD7EAIUbGX5qDTxB27KUS2ElE1sZS/VxeQKYNh0J5EpM6BCHTO7DXZ7nOOLE+eocyTkgwDixCSr17m+L0jn9kcBxHof1BAMC6RjF6AAZ+uo4SUudDlMdtIpHXhq9esvHnSEKJn+ViWIz9hTEgfiH9h5NtrzrgmVFIFDv617IVfVhtMkiLiQbWHA6JZK6OvQ6UoOAHxsHpSZ6X8dJE964T/kJ0/B5fQi8xpwGGgWR1tVBo6mx6ieBxy7LpYkwdww8JadereBmSR6mndBp5wjWBKHi1NEuJSoX0OfNQOf5dL8S5vYQLrWV8+kWRRFI8F9pOyEUsKlhcIdNNRLcYgS+RH6bu2mv8IbfpjkLWpvWKXDxb1/RkND8cNkVIgiFW2wO2+eGqX4EaapGV9vIoBKq/WvXg3QueZ6PJFFeX4lcHAidggyEvnUmr4IDanRXTs7GXAGezOP1bNKmAaeqoHqw9k+MHeHNh2MBzswHcCAXnLfraTHsqvoI1VB1CK4/vDHzB/M4+NUTcFmFk8cQBNDtgw0zKiWeSCFmBch9DiAgMZM6M=");
    }

    @Override
    public void handlePlayerInteract(Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile.getLevel() < 10) {
            player.sendMessage(CC.translate("&c&l等级不足! &7商店在 " + LevelUtil.getLevelTag(profile.getPrestige(), 10) + " &7时解锁."));
        } else {
            new ShopMenu().openMenu(player);
        }
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return null;
    }
}
