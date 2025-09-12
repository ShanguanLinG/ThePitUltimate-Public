package net.mizukilab.pit.data.operator;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.operator.IOperator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public interface ExecutionPolicy {
    ExecutionPolicy EXECUTION_POLICY_DEFAULT = new DefaultPolicy();
    void success(IOperator operator);

    void fail(IOperator operator,Throwable throwable);
    public class DefaultPolicy implements ExecutionPolicy {

        @Override
        public void success(IOperator operator) {
            //NO-OP
        }

        @Override
        public void fail(IOperator operator,Throwable throwable) {
            throwable.printStackTrace();
            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                        Player lastBoundPlayer = ((PackedOperator) operator).lastBoundPlayer;
                        if (lastBoundPlayer == null) {
                            lastBoundPlayer = Bukkit.getPlayer(((PackedOperator) operator).profile.getPlayerUuid());
                        }
                        lastBoundPlayer.kickPlayer("An error was occurred on your profile, please contact the administrator to get details. ");
                    }
            );
        }
    }
}
