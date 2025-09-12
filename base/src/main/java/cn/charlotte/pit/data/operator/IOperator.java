package cn.charlotte.pit.data.operator;

import cn.charlotte.pit.data.PlayerProfile;

public interface IOperator {

    PlayerProfile profile();

    boolean isLoaded();

    void wipe(PlayerProfile profile);
}
