package cn.charlotte.pit.data.sub;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Creator Misoryan
 * @Date 2021/6/9 15:26
 */
@Data
public class MedalData {

    //tier medal -> medalName#medalLevel
    //challenged medal have only 1 level , tiered level have more
    private Map<String, MedalStatus> medalStatus;

    public MedalData() {
        this.medalStatus = new Object2ObjectOpenHashMap<>();
    }

    @JsonIgnore
    public MedalStatus getMedalStatus(String medalInternal, int level) {
        return getMedalStatus(medalInternal + "#" + level);
    }

    @JsonIgnore
    public Map<String, MedalStatus> getUnlockedMedals() {
        Map<String, MedalStatus> unlockedMedals = new HashMap<>(medalStatus);
        List<String> removeList = new ObjectArrayList<>();
        for (String medal : unlockedMedals.keySet()) {
            if (!unlockedMedals.get(medal).isUnlocked()) {
                removeList.add(medal);
            }
        }
        removeList.forEach(unlockedMedals::remove);

        return unlockedMedals;
    }


    @JsonIgnore
    public MedalStatus getMedalStatus(String medalInternal) {
        MedalStatus medalStatusRaw;
        if ((medalStatusRaw = medalStatus.get(medalInternal)) == null) {
            return medalStatus.get(medalInternal + "#" + getMedalLevel(medalInternal));
        } else {
            return medalStatusRaw;
        }
    }

    @JsonIgnore
    public int getMedalLevel(String medalInternal) {
        int result = 1;
        for (Map.Entry<String, MedalStatus> stringMedalStatusEntry : medalStatus.entrySet()) {
            String medal = stringMedalStatusEntry.getKey();
            MedalStatus val = stringMedalStatusEntry.getValue();
            if (medal.startsWith(medalInternal + "#") && val.isUnlocked()) {
                int i = Integer.parseInt(medal.replace(medalInternal + "#", ""));
                if (i > result) {
                    result = i;
                }
            }
        }
        return result;
    }


    @Data
    public static class MedalStatus {

        private boolean unlocked;
        private int progress;
        private long finishedTime;
    }

}
