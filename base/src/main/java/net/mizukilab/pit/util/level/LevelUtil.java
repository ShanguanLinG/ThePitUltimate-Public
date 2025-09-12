package net.mizukilab.pit.util.level;

import cn.charlotte.pit.ThePit;
import net.mizukilab.pit.menu.prestige.button.PrestigeStatusButton;
import net.mizukilab.pit.util.chat.RomanUtil;

/**
 * @Author: Misoryan
 * @Date: 2020/12/29 22:30
 */

public class LevelUtil {

    public static void dropCache() {
        plevelMapping = null;
    }

    public static void main(String[] args) {
        System.out.println(getLevelExpRequired(35, 120));
    }

    /**
     * @param prestige 精通
     * @return 精通的标识颜色
     */

    public static String getPrestigeColor(int prestige) {
        if (prestige >= 1 && prestige <= 4) {
            return "&9";
        } else if (prestige >= 5 && prestige <= 9) {
            return "&e";
        } else if (prestige >= 10 && prestige <= 14) {
            return "&6";
        } else if (prestige >= 15 && prestige <= 19) {
            return "&c";
        } else if (prestige >= 20 && prestige <= 24) {
            return "&5";
        } else if (prestige >= 25 && prestige <= 29) {
            return "&d";
        } else if (prestige >= 30 && prestige <= 34) {
            return "&f";
        } else if (prestige >= 35 && prestige <= 39) {
            return "&b";
        } else if (prestige >= 40 && prestige <= 44) {
            return "&0";
        } else if (prestige >= 45 && prestige <= 47) {
            return "&0";
        } else if (prestige >= 48 && prestige <= 50) {
            return "&4";
        } else if (prestige >= 51 && prestige <= 65) {
            return "&1";
        } else if (prestige >= 66 && prestige <= 70) {
            return "&3";
        } else if (prestige >= 71 && prestige <= 85) {
            return "&2";
        } else if (prestige >= 86 && prestige <= 90) {
            return "&2";
        } else if (prestige >= 91 && prestige <= 120) {
            return "&8&l";
        } else if (prestige >= 121 && prestige <= 240) {
            return "&b&l";
        } else if (prestige >= 241 && prestige <= 300) {
            return "&4&l";
        } else if (prestige >= 301 && prestige <= 400) {
            return "&f&l";
        } else if (prestige >= 401 && prestige <= 512) {
            return "&c&l";
        } else if (prestige >= 513 && prestige <= 700) {
            return "&e&l";
        } else if (prestige >= 701 && prestige <= 800) {
            return "&2&l";
        } else if (prestige >= 801 && prestige <= 999) {
            return "&3&l";
        } else if (prestige >= 1000) {
            return "&3&l";
        }


        return "&7";
    }

    /**
     * @param level 等级
     * @return 等级的标识颜色
     */
    public static String getLevelColor(int level) {
        if (level >= 10) {
            return switch ((level - level % 10) / 10) {
                case 1 -> "&9";
                case 2 -> "&3";
                case 3 -> "&2";
                case 4 -> "&a";
                case 5 -> "&e";
                case 6 -> "&6&l";
                case 7 -> "&c&l";
                case 8 -> "&4&l";
                case 9 -> "&5&l";
                case 10 -> "&d&l";
                case 11 -> "&f&l";
                default ->
                    //>=120
                        "&b&l";
            };
        }
        return "&7";
    }

    /**
     * For max performance
     */
    static double[] plevelMapping = null;
    static boolean booted = false;
    /**
     * @param prestige 玩家当前精通等级
     * @param level    玩家当前等级
     * @return 某一等级所需经验值(level)
     */
    public static long fromCache = 0;
    public static long fromRaw = 0;
    static boolean booting = false;

    public static double getLevelExpRequired(int prestige, int level) {
        if (plevelMapping == null && !booting) {
            bootCache();
        } else if (booted && plevelMapping != null) {
            if (plevelMapping.length > prestige) {
                try {
                    return plevelMapping[prestige * ThePit.getInstance().getGlobalConfig().maxLevel + level];
                } catch (Exception e) {
                    return Double.MAX_VALUE - 1000.0D;
                }
            }
        }
        double boost = 1.1;
        if (level >= 10) {
            double v = getaDouble(prestige, level, boost);
            if (v <= 0) {
                return Double.MAX_VALUE;
            }
            return v;
        } else {
            // 0~9
            return Math.round(Math.pow(boost, prestige) * 15);
        }
    }

    private static double getaDouble(int prestige, int level, double boost) {
        return switch ((level - level % 10) / 10) {
            case 1 -> Math.round(Math.pow(boost, prestige) * 30);
            case 2 -> Math.round(Math.pow(boost, prestige) * 50);
            case 3 -> Math.round(Math.pow(boost, prestige) * 75);
            case 4 -> Math.round(Math.pow(boost, prestige) * 125);
            case 5 -> Math.round(Math.pow(boost, prestige) * 250);
            case 6 -> Math.round(Math.pow(boost, prestige) * 600);
            case 7 -> Math.round(Math.pow(boost, prestige) * 800);
            case 8 -> Math.round(Math.pow(boost, prestige) * 900);
            case 9 -> Math.round(Math.pow(boost, prestige) * 1000);
            case 10 -> Math.round(Math.pow(boost, prestige) * 1200);
            default ->
                //>=110
                    Math.round(Math.pow(boost, prestige) * 1500);
        };
    }

    public static void bootCache() {
        booting = true;
        booted = false;
        int limit = PrestigeStatusButton.limit;
        double[] plevelMappingRaw = new double[(limit + 40) * (ThePit.getInstance().getGlobalConfig().maxLevel + 1)];
        for (int i = 0; i <= limit; i++) {
            int append = i * ThePit.getInstance().getGlobalConfig().maxLevel;
            for (int ia = 0; ia < ThePit.getInstance().getGlobalConfig().maxLevel; ia++) {
                plevelMappingRaw[append + ia] = getLevelExpRequired(i, ia);
            }
        }
        plevelMapping = plevelMappingRaw;
        booted = true;
        booting = false;
    }

    /**
     * @param prestige 精通等级
     * @param exp      总经验值
     * @return 通过已有经验值推算等级
     */
    public static int getLevelByExp(int prestige, double exp) {
        double experience = exp;
        int level = 0;
        for (int i = 0; i <= ThePit.getInstance().getGlobalConfig().maxLevel; i++) {
            level = i;
            double levelExpRequired = getLevelExpRequired(prestige, i);
            if (experience >= levelExpRequired) {
                experience = experience - levelExpRequired;
            } else {
                return i;
            }
        }
        return level;
    }

    /**
     * @param prestige
     * @param level
     * @return 获取升级到某一级所需经验值(0 - level)
     */
    public static double getLevelTotalExperience(int prestige, int level) {
        double experience = 0;
        for (int i = 0; i < level; i++) {
            experience = experience + getLevelExpRequired(prestige, i);
            if (experience < 0) {
                return Double.MAX_VALUE;
            }
        }
        return experience;
    }

    public static float getLevelProgress(int prestige, double experience) {
        int level = LevelUtil.getLevelByExp(prestige, experience);
        if (level >= ThePit.getInstance().getGlobalConfig().maxLevel) {
            return 1;
        } else {
            double levelExpRequired = getLevelExpRequired(prestige, level);
            return (float) ((levelExpRequired - getLevelTotalExperience(prestige, level + 1) + experience) / levelExpRequired);
        }
    }

    /**
     * @param prestige
     * @param experience
     * @return 通过精通和经验值获得一个展示用等级标志 [120] <- example
     */
    public static String getLevelTag(int prestige, double experience) {
        int level = getLevelByExp(prestige, experience);
        return getLevelTag(prestige, level);
    }


    public static String getLevelTag(int prestige, int level) {
        String prestigeColor = getPrestigeColor(prestige);
        return prestigeColor + "[" + getLevelColor(level) + level + prestigeColor + "]";
    }

    public static String getLevelTagWithOutAnyPS(int level) {
        return getLevelColor(level) + level;
    }

    public static String getLevelTagTabListSpec(int prestige, int level) {
        if (prestige < 91) {
            return getLevelTag(prestige, level);
        } else {
            return getPrestigeColor(prestige) + "P" + getLevelColor(level) + level;
        }
    }

    public static String getLevelTagWithRoman(int prestige, double experience) {
        if (prestige > 0) {
            int level = getLevelByExp(prestige, experience);
            return getPrestigeColor(prestige) + "[&e" + RomanUtil.convert(prestige) + getPrestigeColor(prestige) + "-" + getLevelColor(level) + level + getPrestigeColor(prestige) + "]";
        } else {
            return getLevelTag(prestige, experience);
        }
    }
    public static String getPrestigeTagFully(int prestige) {
        if (prestige > 0) {
            String prestigeColor = getPrestigeColor(prestige);
            return prestigeColor + "[&e" + RomanUtil.convert(prestige) + prestigeColor + "]";
        } else {
            return RomanUtil.convert(prestige);
        }
    }
    public static String getLevelTagWithRoman(int prestige, int level) {
        if (prestige > 0) {
            return getPrestigeColor(prestige) + "[&e" + RomanUtil.convert(prestige) + getPrestigeColor(prestige) + "-" + getLevelColor(level) + level + getPrestigeColor(prestige) + "]";
        } else {
            return getLevelTag(prestige, level);
        }
    }
}
