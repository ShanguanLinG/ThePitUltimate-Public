package cn.charlotte.pit.data.sub;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.UUID;

/**
 * @Creator Misoryan
 * @Date 2021/5/29 10:15
 */
public class OfferData {

    private String buyer;
    //use InventoryUtil to serialize
    private String itemStack;
    private double price;
    private long endTime;

    public OfferData() {
    }

    public OfferData(String buyer, String itemStack, double price, long endTime) {
        this.buyer = buyer;
        this.itemStack = itemStack;
        this.price = price;
        this.endTime = endTime;
    }

    @JsonIgnore
    public UUID getBuyer() {
        if (buyer == null) return null;
        return UUID.fromString(buyer);
    }

    @JsonIgnore
    public void setBuyer(UUID uuid) {
        buyer = uuid.toString();
    }

    @JsonIgnore
    public ItemStack getItemStack() {
        return InventoryUtil.deserializeItemStack(itemStack);
    }

    @JsonIgnore
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = InventoryUtil.serializeItemStack(itemStack);
    }

    @JsonIgnore
    public boolean hasActiveOffer() {
        return buyer != null && System.currentTimeMillis() < endTime;
    }

    @JsonIgnore
    public boolean hasUnclaimedOffer() {
        return buyer != null && System.currentTimeMillis() >= endTime;
    }

    @JsonIgnore
    public void createOffer(UUID target, ItemStack itemStack, double price, long endTime) {
        if (hasActiveOffer() || hasUnclaimedOffer()) return;
        this.buyer = target.toString();
        setItemStack(itemStack);
        this.price = price;
        this.endTime = endTime;
    }

    @JsonIgnore
    public void createOffer(UUID target, ItemStack itemStack, double price) {
        createOffer(target, itemStack, price, System.currentTimeMillis() + 60 * 1000L);
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public void setItemStack(String itemStack) {
        this.itemStack = itemStack;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OfferData offerData = (OfferData) o;
        return Double.compare(price, offerData.price) == 0 && endTime == offerData.endTime && Objects.equals(buyer, offerData.buyer) && Objects.equals(itemStack, offerData.itemStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buyer, itemStack, price, endTime);
    }

    @Override
    public String toString() {
        return "OfferData{" +
                "buyer='" + buyer + '\'' +
                ", itemStack='" + itemStack + '\'' +
                ", price=" + price +
                ", endTime=" + endTime +
                '}';
    }
}
