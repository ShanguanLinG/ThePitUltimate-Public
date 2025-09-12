package net.mizukilab.pit.impl;

import cn.charlotte.pit.api.PointsAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NullPlayerPointsAPIImpl implements PointsAPI {
    @Override
    public boolean hasPoints(@NotNull Player player, int points) {
        return false;
    }

    @Override
    public int getPoints(@NotNull Player player) {
        return 0;
    }

    @Override
    public void costPoints(@NotNull Player player, int points) {

    }
}
