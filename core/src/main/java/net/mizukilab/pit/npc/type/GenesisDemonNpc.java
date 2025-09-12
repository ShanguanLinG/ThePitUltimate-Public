package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author Yurinan
 * @since 2022/3/6 7:45
 */

public class GenesisDemonNpc extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "GenesisDemon";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> lines = new ObjectArrayList<>(3);
        lines.add("&e限时活动: 光暗派系");
        lines.add("&c&l恶魔");
        lines.add("&e&l右键查看");
        return lines;
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getGenesisDemonNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin(
                "ewogICJ0aW1lc3RhbXAiIDogMTc0NTEyOTI2NTM0NCwKICAicHJvZmlsZUlkIiA6ICI2ZjhlYWI1MTVmNTc0MmRhOWYxZDYzMzY1ODAxMDU4YyIsCiAgInByb2ZpbGVOYW1lIiA6ICJDaW5kZXJGb3hfMjAwNiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80ZTc5NzBiOTFmNGY3YjI5MTI0YjU3N2QzNjc3OTljOWJiODJlODlmNmY4OTA0NzAxNjVkOWE1YzZjZGFhMjEwIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                "uSNEaxynpDGyNO2dCNKPpePSuUeNuKeRqod1YQqkdXjZ0h+j8iQ4+D6qRoWvRk7PFDy0knxHAZQv2AvN7uuzpdZ10bX+SOWuY8mWuujBvHPMiTvWwk1ReYR3PpK/ipud8jT4TU2+Rmk7go7sIfHZiFX6LrqhORnLRXkHT8Kx/y9Cp10t7e3FM1jj3E7icPYvpWmaSK77XunUHUfIT4dND5ONnf94BUm7+uZtEjMObQ1bOVuf5Vh/zs0KNrmXAAC3+Cf/AyEQoEhJvr3iGEmpUb6bc07236mPD+hONF1RQTzHsBbPFxBnYtnsu72S6PWR6I/X3zyxaq/aFGG9nIfkI4z9YVJxZrN1fyVgunK1KS2zMJRmFBn9zg/5mPYSZVZV1klyCgxw4qhRt6oFfc3gJT0fAEPVNnzLO6Mj5tT9hPk6ydms1R1XYczftDfEFql0UtNAE13+7nqomf0hdq8AmrMYXf23U7Eu7PGTi6S9cKTs0shZ8EHklN/HcCFTq8M5jEN1sROklz6eaKsPofFziwWkaAeZIHNE9KBdqisJNCMKBMol2k63uqBP2lRrqLmUHO9PvKhATTfV8MbXMweIbCse/ZUxVzwZg02uPWRa+87warfSnzlQh6NtbsEsZcyIbgJ1591Ka6CEvDqyLHhGImIHn29Gm7IBc9xsTEqMzsE="
        );
    }

    @Override
    public void handlePlayerInteract(Player player) {
        ThePit.getApi().openDemonMenu(player);
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return null;
    }

}
