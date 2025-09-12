package net.mizukilab.pit.data.operator;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.operator.IOperator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import lombok.ToString;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
@Skip
@ToString
public class PackedOperator implements IOperator {
    Throwable throwable;
    ExecutionPolicy policy;
    ThePit pit;
    long lastHeartBeat = 0;

    Player lastBoundPlayer = null;
    @NotNull
    PlayerProfile profile = PlayerProfile.NONE_PROFILE;

    public PackedOperator(ThePit inst) {
        this.policy = ExecutionPolicy.EXECUTION_POLICY_DEFAULT;
        this.pit = inst;

    }

    public PlayerProfile profile() {
        return profile;
    }

    public boolean isLoaded() {
        return profile != PlayerProfile.NONE_PROFILE && profile.isLoaded();
    }

    public void ifLoaded(Runnable runnable) {
        if (isLoaded()) {
            runnable.run();
        }
    }

    public void heartBeat() {
        Player player = Bukkit.getPlayer(profile.getPlayerUuid());
        if (player != null && player.isOnline()) {
            this.quitFlag = false;
            this.fireExit = false;
        }
        this.lastHeartBeat = System.currentTimeMillis();
    }

    public void loadAs(UUID uuid, String name) {
        lastBoundPlayer = Bukkit.getPlayer(uuid);
        synchronized (operations) {
            operations.add(() -> {
                loadAs0(uuid, name);
            });
        }
        this.heartBeat();
    }

    public void loadAs(PlayerProfile profile) {

        if (profile == PlayerProfile.NONE_PROFILE || profile == null) {
            return;
        }
        lastBoundPlayer = Bukkit.getPlayer(profile.getPlayerUuid());
        this.profile = profile;
        this.heartBeat();
    }

    public void loadAs0(UUID uuid, String name) {
        if (profile != PlayerProfile.NONE_PROFILE) {
            return;
        }
        PlayerProfile rawProfile = PlayerProfile.loadPlayerProfileByUuid(uuid);
        if (rawProfile == null) {
            rawProfile = new PlayerProfile(uuid, name);
        } else {
            PlayerProfile.loadMail(rawProfile, uuid);
        }
        PlayerProfile.bootstrapProfile(rawProfile);
        profile = rawProfile;
    }

    final ObjectArrayList<Runnable> operations = new ObjectArrayList<>(); //safer
    Set<Runnable> pendingExecuting = new CopyOnWriteArraySet<>(); //正常情况下就一个

    public void fail(Throwable throwable) {
        pending(i -> {
            throw new RuntimeException(throwable);
        });
    }

    public boolean hasAnyOperation() {
        synchronized (operations) {
            return !operations.isEmpty() && !this.pendingExecuting.isEmpty();
        }
    }

    boolean fireExit = false;
    boolean quitFlag = false;

    //为了防止掉数据做的妥协
    public synchronized boolean save(boolean fireExit, boolean quitFlag) {
        if (System.currentTimeMillis() - lastHeartBeat > 1000) {
            this.fireExit = fireExit;
            if (this.fireExit) {
                if (!quitFlag) {
                    pending(prof -> {
                        prof.disallowUnsafe();
                        prof.save(null);
                        prof.allow();
                    });
                }
            }
            return true;
        }
        if (quitFlag && !this.quitFlag) {
            pending(prof -> {
                PlayerProfile playerProfile = prof.disallowUnsafe();
                PlayerProfile save = playerProfile.save(null);
                pending(unk -> {
                    save.allow();
                });
            });
            this.quitFlag = true;
        }
        return true;
    }

    public void pendingIfLoaded(Consumer<PlayerProfile> profile) {
        if (isLoaded()) {
            pending(profile);
        }
    }

    public void pending(Consumer<PlayerProfile> profile) {
        offerOperation(() -> {
            profile.accept(PackedOperator.this.profile);
        });
    }

    public Promise promise(Consumer<PlayerProfile> profile) {
        Promise promise = new Promise();
        offerOperation(() -> {
            profile.accept(PackedOperator.this.profile);
            promise.ret();
        });
        return promise;
    }

    public void offerOperation(Runnable runnable) {
        synchronized (operations) {
            operations.add(runnable);
        }
    }

    public void tick() {
        updateEntity();
        drainTask();
    }

    public void drainTask() {
        Runnable currentOperation = gainTask();
        if (currentOperation == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(pit, currentOperation);
    }

    public void drainTasksOnCurrentThread() {
        while (true) {
            Runnable runnable = gainTask();
            if (runnable == null) {
                break;
            }
            runnable.run();
        }
    }

    private void updateEntity() {
        if (lastBoundPlayer != null) {
            if (isLoaded()) {
                Player player = Bukkit.getPlayer(this.profile.getPlayerUuid());
                if (player != null && player.isOnline()) {
                    this.lastBoundPlayer = player;
                }
            }
        }
    }

    @Nullable
    private Runnable gainTask() {
        Runnable currentOperation;
        synchronized (operations) {
            if (operations.isEmpty()) {
                return null;
            }

            if (!pendingExecuting.isEmpty()) {
                return null;
            }

            Runnable operation = EMPTY_RUNNABLE;
            for (Runnable next : operations) {
                operation = next;
                break;
            }

            pendingExecuting.add(operation);
            final Runnable operationFinaled = operation;

            currentOperation = () -> {
                try {
                    operationFinaled.run();
                    operations.remove(operationFinaled);
                    pendingExecuting.remove(operationFinaled);
                    policy.success(PackedOperator.this);
                } catch (Exception e) {
                    throwable = e;
                    policy.fail(PackedOperator.this, e);
                }
            };
        }
        return currentOperation;
    }

    private static final Runnable EMPTY_RUNNABLE = () -> {
    };

    public UUID getUniqueId() {
        return this.profile.getPlayerUuid();
    }

    public void wipe(PlayerProfile wProfile) {
        pending(i -> {
            Bukkit.getScheduler().runTask(pit, () -> {
                Bukkit.getPlayer(i.getPlayerUuid()).kickPlayer("working");
            });
            this.profile = wProfile;
        });
    }

    @SneakyThrows
    public void waitForLoad() {
        while (!this.isLoaded()) {
            Thread.onSpinWait();
        }
    }

    public Promise pendingUntilLoadedPromise(Consumer<PlayerProfile> profileConsumer) {
        Promise promise = new Promise();
        pendingUntilLoaded(prof -> {
            profileConsumer.accept(prof);
            promise.ret();
        });
        return promise;
    }

    public void pendingUntilLoaded(Consumer<PlayerProfile> profileConsumer) {
        if (isLoaded()) {
            this.pending(profileConsumer);
            return;
        }
        new BukkitRunnable() {
            public void run() {
                if (isLoaded()) {
                    this.cancel();
                    pendingIfLoaded(profileConsumer);
                }
            }
        }.runTaskTimerAsynchronously(pit, 0, 5);
    }
}
