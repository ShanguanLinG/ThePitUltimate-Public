package net.mizukilab.pit.data.operator;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.operator.IOperator;
import cn.charlotte.pit.data.operator.IProfilerOperator;
import com.google.common.annotations.Beta;
import io.irina.backports.utils.SWMRHashTable;
import net.mizukilab.pit.util.Utils;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
@Skip
public class ProfileOperator implements IProfilerOperator {

    ThePit instance;
    SWMRHashTable<UUID, PackedOperator> operators = new SWMRHashTable<>();

    public ProfileOperator(ThePit thePit) {
        this.instance = thePit;
    }

    public PackedOperator loadProfile(Player player) {
        UUID uniqueId = player.getUniqueId();
        PackedOperator packedOperator = operators.get(uniqueId);
        if (packedOperator == null) {
            packedOperator = new PackedOperator(instance);
            operators.put(uniqueId, packedOperator); //put first
            packedOperator.loadAs(uniqueId, player.getName());
        }
        return packedOperator;
    }

    public PackedOperator loadProfile(UUID uniqueId, String name) {
        PackedOperator packedOperator = operators.get(uniqueId);
        if (packedOperator == null) {
            packedOperator = new PackedOperator(instance);
            operators.put(uniqueId, packedOperator); //put first
            packedOperator.loadAs(uniqueId, name);
        }
        return packedOperator;
    }

    @Beta
    public PackedOperator construct(UUID uniqueId, String name) {
        PackedOperator packedOperator = new PackedOperator(instance);
        packedOperator.loadAs0(uniqueId, name);
        return packedOperator;
    }

    public PackedOperator getOperator(UUID uuid) {
        return operators.get(uuid);
    }

    public void dump() {
        System.out.println(withString());
    }

    public String withString() {
        return operators.toString();
    }

    public PackedOperator getOperator(Player player) {
        return getOperator(player.getUniqueId());
    }

    public PackedOperator getOperator(String uuid) {
        return getOperator(UUID.fromString(uuid));
    }

    public PackedOperator getOrLoadOperator(Player player) {
        UUID uuid = player.getUniqueId();
        PackedOperator packedOperator = operators.get(uuid);
        if (packedOperator != null) {
            return packedOperator;
        } else {
            return loadProfile(player);
        }
    }

    public PackedOperator namedOperator(String name) {
        PackedOperator operator = lookupForName(name);
        if (operator != null) {
            return operator;
        }
        PackedOperator packedOperator = new PackedOperator(instance);
        packedOperator.loadAs(PlayerProfile.loadPlayerProfileByName(name));
        return packedOperator;
    }

    public PackedOperator lookupForName(String name) {
        for (PackedOperator value : operators.values()) {
            if (value.isLoaded()) {
                String playerName = value.profile.getPlayerName();
                if (playerName.equals(name)) {
                    return value;
                }
            }//lookups
        }
        return null;
    }

    public PackedOperator lookupStrict(String name) {
        PackedOperator packedOperator = lookupForName(name);
        if (packedOperator == null) {
            return null;
        }
        if (packedOperator.isLoaded()) {
            return packedOperator;
        }
        return null;
    }

    public PackedOperator lookupOnline(String name) {
        PackedOperator packedOperator = lookupStrict(name);
        if (packedOperator == null) {
            packedOperator = Utils.constructUnsafeOperator(name);
            return packedOperator;
        }
        return packedOperator;
    }

    //少用可以吗;w;
    @Beta
    public PackedOperator getOrConstructOperator(Player player) {
        PackedOperator operator = getOperator(player);
        if (operator == null || !operator.isLoaded()) {
            operator = construct(player.getUniqueId(), player.getName());
            //operators.put(player.getUniqueId(),operator);
        }
        return operator;
    }

    public PackedOperator getOrLoadOperatorOffline(UUID uuid, String name) {
        PackedOperator packedOperator = operators.get(uuid);
        if (packedOperator != null) {
            return packedOperator;
        } else {
            return loadProfile(uuid, name);
        }
    }

    public Optional<PackedOperator> operator(UUID uuid) {
        return Optional.ofNullable(getOperator(uuid));
    }

    public Optional<PackedOperator> operatorStrict(Player player) {
        PackedOperator operator = getOperator(player);
        if (operator == null || !operator.isLoaded()) {
            return Optional.empty();
        } else {
            return Optional.of(operator);
        }
    }

    public Optional<PackedOperator> operator(Player player) {
        return operator(player.getUniqueId());
    }

    public void ifPresentAndLoaded(Player player, Consumer<PackedOperator> cooperator) {
        PackedOperator operator = getOperator(player);
        if (operator != null) {
            if (operator.isLoaded()) {
                cooperator.accept(operator);
            }
        }
    }

    public void tick() {
        operators.removeIf((uuid, operator) -> {
            if (operator == null) {
                return false;
            }
            UUID uniqueId = operator.getUniqueId();
            Player player = Bukkit.getPlayer(uniqueId);
            operator.tick();
            if (player == null || !player.isOnline()) {
                if(operator.throwable != null){
                    return true;
                }
                if (operator.profile.code == -2) {
                    return false;
                }
                boolean b = !operator.hasAnyOperation();
                boolean lastFireExit = operator.fireExit && b; // use lastFireExit and should wait all operation to remove

                if (!operator.fireExit) {
                    if (b) {
                        if (operator.isLoaded()) {
                            operator.save(true, true);
                        } else {
                            operator.fireExit = true;
                        }
                    } else {
                        operator.heartBeat();
                    }
                }

                return lastFireExit;
            } else {
                if (operator.fireExit) {
                    operator.fireExit = false;
                } else {
                    operator.heartBeat();
                }
            }
            return false;
        });
    }

    @Override
    public IOperator getIOperator(UUID uuid) {
        return getOperator(uuid);
    }

    @Override
    public IOperator getIOperator(String uuid) {
        return getOperator(uuid);
    }

    @Override
    public IOperator getIOperator(Player player) {
        return getOperator(player);
    }

    public void doSaveProfiles() {
        operators.forEachValue(operator -> {
            Player lastBoundPlayer = operator.lastBoundPlayer;
            operator.pendingUntilLoadedPromise(i -> {
                if (lastBoundPlayer != null && lastBoundPlayer.isOnline() && !(operator.quitFlag || operator.fireExit)) {
                    PlayerProfile playerProfile = i.disallowUnsafe();
                    playerProfile.save(lastBoundPlayer);
                }
            }).promise(() -> {
                operator.profile().allow();
            });

        });
        randomGC();

    }

    @Override
    public IOperator getOrConstructIOperator(Player target) {
        return getOrConstructOperator(target);
    }

    @NotNull
    @Override
    public IOperator namedIOperator(@NotNull String target) {
        return namedOperator(target);
    }

    @NotNull
    @Override
    public IOperator lookupIOnline(@NotNull String name) {
        return null;
    }

    @NotNull
    public void ifPresentAndILoaded(@NotNull Player target, @NotNull Consumer<IOperator> function) {
        ifPresentAndLoaded(target, function::accept);
    }

    @Override
    public void forEach(Consumer<IOperator> function) {
        this.operators.values().forEach(function);
    }

    public void randomGC() {
        if (Bukkit.getOnlinePlayers().size() <= 2) {
            return;
        }
        Collection<PackedOperator> values = operators.values();
        if (values.size() < 2) {
            return;
        }
        int randomNum = ThreadLocalRandom.current().nextInt(values.size() - 2) + 1;

        Iterator<PackedOperator> iterator = values.iterator();
        PackedOperator operator = null;
        for (int i = 0; i < randomNum; i++) {
            operator = iterator.next();
        }
        if (operator != null) {
            Player lastBoundPlayer = operator.lastBoundPlayer;
            PackedOperator finalOperator = operator;
            operator.pendingUntilLoaded(i -> {
                if (lastBoundPlayer != null && lastBoundPlayer.isOnline() && !(finalOperator.quitFlag || finalOperator.fireExit)) {
                    PlayerProfile.gcBackups(i.gcBackupIterators(), i, false);
                }
            });
        }
    }

    public void wipe() {
        operators.clear();
    }

    @Override
    public void close() throws IOException {
        operators.removeIf((a,p) -> {
            p.drainTasksOnCurrentThread();
            return true;
        });
    }
}
