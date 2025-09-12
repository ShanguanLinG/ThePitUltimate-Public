package cn.charlotte.pit.buff;

import cn.charlotte.pit.ThePit;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.SneakyThrows;
import net.mizukilab.pit.util.ClassUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.List;

/**
 * @Creator Misoryan
 * @Date 2021/5/10 18:21
 */
//practicing and copying
@Getter
public class BuffFactory {

    private final List<AbstractPitBuff> buffs;

    public BuffFactory() {
        this.buffs = new ObjectArrayList<>();
    }

    public AbstractPitBuff getBuffByInternalName(String internalName) {
        for (AbstractPitBuff buff : buffs) {
            if (buff.getInternalBuffName().equalsIgnoreCase(internalName)) {
                return buff;
            }
        }
        return null;
    }

    @SneakyThrows
    public void init() {
        Collection<Class<?>> classes = ClassUtil.getClassesInPackage(ThePit.getInstance(), "cn.charlotte.pit.buff.impl");
        for (Class<?> clazz : classes) {
            if (AbstractPitBuff.class.isAssignableFrom(clazz)) {
                Object instance = clazz.newInstance();
                if (Listener.class.isAssignableFrom(clazz)) {
                    Bukkit.getPluginManager().registerEvents((Listener) instance, ThePit.getInstance());
                }
                buffs.add((AbstractPitBuff) instance);

            }
        }
    }
}
