package cn.charlotte.pit.data.operator;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.UUID;
import java.util.function.Consumer;

public interface IProfilerOperator extends Closeable {

    IOperator getIOperator(UUID uuid);

    IOperator getIOperator(String uuid);

    IOperator getIOperator(Player player);

    void doSaveProfiles();

    IOperator getOrConstructIOperator(Player target);

    @NotNull IOperator namedIOperator(@NotNull String target);

    @NotNull IOperator lookupIOnline(@NotNull String name);

    @NotNull void ifPresentAndILoaded(@NotNull Player target, @NotNull Consumer<IOperator> function);
    void forEach(Consumer<IOperator> function);
}
