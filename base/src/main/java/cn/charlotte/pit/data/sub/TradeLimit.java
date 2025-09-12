package cn.charlotte.pit.data.sub;

import java.util.Objects;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/27 16:56
 */
public class TradeLimit {

    private long lastRefresh;
    private int times;
    private double amount;

    public TradeLimit() {
    }

    public TradeLimit(long lastRefresh, int times, double amount) {
        this.lastRefresh = lastRefresh;
        this.times = times;
        this.amount = amount;
    }

    public long getLastRefresh() {
        return lastRefresh;
    }

    public void setLastRefresh(long lastRefresh) {
        this.lastRefresh = lastRefresh;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeLimit that = (TradeLimit) o;
        return lastRefresh == that.lastRefresh && times == that.times && Double.compare(amount, that.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastRefresh, times, amount);
    }

    @Override
    public String toString() {
        return "TradeLimit{" +
                "lastRefresh=" + lastRefresh +
                ", times=" + times +
                ", amount=" + amount +
                '}';
    }
}
