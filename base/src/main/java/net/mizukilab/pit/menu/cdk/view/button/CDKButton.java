package net.mizukilab.pit.menu.cdk.view.button;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.CDKData;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.mail.Mail;
import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import lombok.SneakyThrows;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;

/**
 * @Author: EmptyIrony
 * @Date: 2021/3/27 19:37
 */
public class CDKButton extends Button {

    private static final Gson gson = new Gson();
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    private final CDKData data;

    public CDKButton(CDKData data) {
        this.data = data;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        final long now = System.currentTimeMillis();

        return new ItemBuilder(Material.NAME_TAG)
                .name("&a" + data.getCdk())
                .lore(
                        "&aExpire: " + (data.getExpireTime() < now ? "&c" : "&a") + format.format(data.getExpireTime()),
                        "&aLimitLevel: " + data.getLimitLevel(),
                        "&aLimitPerm: " + data.getLimitPermission(),
                        "&aExp: " + data.getExp(),
                        "&aCoins: " + data.getCoins(),
                        "&aRenown: " + data.getRenown(),
                        "&aItems: " + InventoryUtil.getInventoryFilledSlots(data.getItem().getContents()),
                        "&aLimitClaimed: " + data.getLimitClaimed(),
                        "&aClaimed: " + data.getClaimedPlayers().size(),
                        "",
                        "&eLeft Click to view more details",
                        "&cRight Click to DELETE this cdk",
                        "&fShift + Left Force Click to claim this cdk"
                )
                .build();
    }

    @Override
    @SneakyThrows
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        if (clickType == ClickType.RIGHT) {
            ThePit.getInstance()
                    .getMongoDB()
                    .getCdkCollection()
                    .deleteOne(Filters.eq("cdk", data.getCdk()));
            player.closeInventory();
            CDKData.getCachedCDK().remove(data.getCdk());
            player.sendMessage(CC.translate("&cIts done"));
            return;
        }
        if (clickType == ClickType.LEFT) {
            final StringBuilder builder = new StringBuilder("Expire: " + format.format(data.getExpireTime()) + "\n" +
                    "LimitLevel: " + data.getLimitLevel() + "\n" +
                    "LimitPerm: " + data.getLimitPermission() + "\n" +
                    "Exp: " + data.getExp() + "\n" +
                    "Coins: " + data.getCoins() + "\n" +
                    "Renown: " + data.getRenown() + "\n" +
                    "Items: " + InventoryUtil.getInventoryFilledSlots(data.getItem().getContents()) + "\n" +
                    "LimitClaimed: " + data.getLimitClaimed() + "\n" +
                    "Claimed: " + "\n");

            for (String claimedPlayer : data.getClaimedPlayers()) {
                builder.append(claimedPlayer)
                        .append("\n");
            }
            player.sendMessage(builder.toString());
//            player.sendMessage(CC.translate("&aPost information to paste server,it will take a seconds,please wait..."));

//            CloseableHttpClient client = HttpClientBuilder.create().build();
//
//            HttpPost postRequest = new HttpPost("http://chatlog.staff.mc.netease.domcer.com:7777/documents");
//            StringEntity userEntity = new StringEntity(builder.toString(), HTTP.UTF_8);
//            postRequest.setEntity(userEntity);
//
//            HttpResponse response = client.execute(postRequest);
//            if (response.getStatusLine().getStatusCode() == 200) {
//                JsonObject responseObject = gson.fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
//                player.sendMessage(CC.translate("&a完成: &e" + "http://chatlog.staff.mc.netease.domcer.com:7777/" + responseObject.get("key").getAsString()));
//            } else {
//                player.sendMessage(CC.translate("&cERROR"));
//            }
            return;
        }

        if (clickType == ClickType.SHIFT_LEFT) {
            final Mail mail = new Mail();
            mail.setExpireTime(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L);
            mail.setCoins(data.getCoins());
            mail.setExp(data.getExp());
            mail.setRenown(data.getRenown());
            mail.setItem(data.getItem());
            mail.setSendTime(System.currentTimeMillis());
            mail.setTitle("&e【奖励】兑换码兑换奖励");
            mail.setContent("&f亲爱的玩家: 请查收通过兑换码获得的奖励");

            final PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
            profile.getMailData().sendMail(mail);
            return;
        }
    }
}
