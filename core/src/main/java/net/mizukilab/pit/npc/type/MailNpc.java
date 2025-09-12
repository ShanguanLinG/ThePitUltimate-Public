package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerMailData;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.mail.Mail;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.mizukilab.pit.menu.mail.MailMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author: EmptyIrony
 * @Date: 2021/3/25 19:03
 */

public class MailNpc extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "mail";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> hologram = new ObjectArrayList<>(3);
        final PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        final PlayerMailData mailData = profile.getMailData();

        int unread = 0;
        for (Mail mail : mailData.getMails()) {
            if (!mail.isClaimed()) {
                unread++;
            }
        }
        hologram.add("&e&l邮件");
        if (unread > 0) {
            hologram.add((System.currentTimeMillis() % 2 == 0 ? "&a" : "&2") + "您有 " + unread + " 封未读邮件");
        }

        return hologram;
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getMailNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin(
                "ewogICJ0aW1lc3RhbXAiIDogMTc0Mjg4MDM4NTYyNSwKICAicHJvZmlsZUlkIiA6ICJmMjc0YzRkNjI1MDQ0ZTQxOGVmYmYwNmM3NWIyMDIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJIeXBpZ3NlbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80YjA1ZDU2MGYwNjNiOWIzZjg4MGNjNzk1MDIwMDVlYzU0YzZkMmZhMDMxM2NkYjM1OTNiMjgzYjJkNTcxNGVhIgogICAgfQogIH0KfQ==",
                "Ydr/7pLwQxd9WnkxcFw8Yhu+Uvgly5CxpDd/t71GtJyOxEsRp4MHXc9d08+dUbJvy1CElsWt6uh7VJcUOCjCdbh1B8eDwz+37vzjN4bmyoTv2+TnIgYgdSURzkKWcZuHIVzxtXDq/+/8qbm9cBNGMbP6m6F34LQ3Nii6GVhFaxn2HQkPMy3ckJVOFJyEM/SYNQqj0LeLJRunKn08gsrBAYa7Xfa55tdC8EBd5Yce/UELU7RxcyD88ohS41+SzxaB1OolLPTPURfRYW3eytd5j6PAl9iyA+vitzeaGNPAg8bZjDSlVvWscOeyIDuhRP+uJ2kt0sChHPp1k9OzO9ZWwcxflP4xXY+3BCI4UnzJBkKuUa+32P22f6wGks7ttUq7n5nEQd4ZItr2ax+H338xk0myQPIVG3+3vKPtit0dZBw57K2JqKP2H4x8sajmSbZoJUKRwJ93PKp7OafM3qcRaM06I96kArA4toWIQjlixmIIBcFFAVGa4ZByGmsLiiyZ+1vY5Xg7oU2+ol27gEVid0B11FXkxFGSPZWNGEXT2UcasjEQ3llrLyrB4qBul+U3OzTwmfkk9QAVqU87emA6T9Yzdq8+OV+9ivGvs8O+QQBHPa9bdQj9OTC7C63EjV4CIGA0tCTD7ASPecQ4UgYLLlKnMxqSH24gY/8dKRnwCW4="
        );
    }

    @Override
    public void handlePlayerInteract(Player player) {
        new MailMenu().openMenu(player);
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return new ItemStack(Material.CHEST);
    }
}
