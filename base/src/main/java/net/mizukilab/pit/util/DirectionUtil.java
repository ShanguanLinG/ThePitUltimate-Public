package net.mizukilab.pit.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * 2 * @Author: EmptyIrony
 * 3 * @Date: 2020/3/9 23:09
 * 4
 */

public class DirectionUtil {

    public static String getDetailedDirection(Player p1, Location location) {
        Location loc1 = p1.getLocation();

        int x1 = loc1.getBlockX();
        int z1 = loc1.getBlockZ();
        int x2 = location.getBlockX();
        int z2 = location.getBlockZ();

        int dx = x2 - x1;
        int dz = z2 - z1;

        if (dx == 0 && dz == 0) {
            return "X";
        }

        double angleToTarget = Math.atan2(dz, dx) * (180 / Math.PI);
        if (angleToTarget < 0) {
            angleToTarget += 360;
        }

        float yaw = p1.getLocation().getYaw();
        if (yaw < 0) {
            yaw += 360;
        }


        double relativeAngle = (angleToTarget - yaw + 360) % 360;


        if (relativeAngle >= 22.5 && relativeAngle < 67.5) {
            return "⬉";
        } else if (relativeAngle >= 67.5 && relativeAngle < 112.5) {
            return "⬆";
        } else if (relativeAngle >= 112.5 && relativeAngle < 157.5) {
            return "⬈";
        } else if (relativeAngle >= 157.5 && relativeAngle < 202.5) {
            return "➡";
        } else if (relativeAngle >= 202.5 && relativeAngle < 247.5) {
            return "⬊";
        } else if (relativeAngle >= 247.5 && relativeAngle < 292.5) {
            return "⬇";
        } else if (relativeAngle >= 292.5 && relativeAngle < 337.5) {
            return "⬋";
        } else {
            return "⬅";
        }
    }

    public static String getTargetDirection(Player p, Location targetLoc) {
        String f = getDirection(p);
        Location l = p.getLocation();
        int x = l.getBlockX();
        int z = l.getBlockZ();
        int x2 = targetLoc.getBlockX();
        int z2 = targetLoc.getBlockZ();
        switch (getDirection(p)) {
            case "N":
                if (z < z2) {
                    if (x < x2) {
                        f = "⬋";
                    } else if (x > x2) {
                        f = "⬈";
                    } else {
                        f = "⬇";
                    }
                } else if (z > z2) {
                    if (x < x2) {
                        f = "⬈";
                    } else if (x > x2) {
                        f = "⬉";
                    } else {
                        f = "⬆";
                    }
                } else if (x < x2) {
                    f = "➡";
                } else if (x > x2) {
                    f = "⬅";
                } else {
                    f = "✘";
                }
                break;
            case "S":
                if (z > z2) {
                    if (x > x2) {
                        f = "⬋";
                    } else if (x < x2) {
                        f = "⬊";
                    } else {
                        f = "⬇";
                    }
                } else if (z < z2) {
                    if (x > x2) {
                        f = "⬈";
                    } else if (x < x2) {
                        f = "⬉";
                    } else {
                        f = "⬆";
                    }
                } else if (x > x2) {
                    f = "➡";
                } else if (x < x2) {
                    f = "⬅";
                } else {
                    f = "✘";
                }
                break;
            case "W":
                if (x < x2) {
                    if (z > z2) {
                        f = "⬋";
                    } else if (z < z2) {
                        f = "⬊";
                    } else {
                        f = "⬇";
                    }
                } else if (x > x2) {
                    if (z > z2) {
                        f = "⬈";
                    } else if (z < z2) {
                        f = "⬉";
                    } else {
                        f = "⬆";
                    }
                } else if (z < z2) {
                    f = "⬅";
                } else if (z > z2) {
                    f = "➡";
                } else {
                    f = "✘";
                }
                break;
            case "E":
                if (x > x2) {
                    if (z < z2) {
                        f = "⬋";
                    } else if (z > z2) {
                        f = "⬊";
                    } else {
                        f = "⬇";
                    }
                } else if (x < x2) {
                    if (z < z2) {
                        f = "⬈";
                    } else if (z > z2) {
                        f = "⬉";
                    } else {
                        f = "⬆";
                    }
                } else if (z > z2) {
                    f = "⬅";
                } else if (z < z2) {
                    f = "➡";
                } else {
                    f = "✘";
                }
                break;
            case "NE":
                if (x == x2) {
                    if (z < z2) {
                        f = "⬋";
                    } else {
                        f = "⬉";
                    }
                } else if (z == z2) {
                    if (x < x2) {
                        f = "⬈";
                    } else {
                        f = "⬊";
                    }
                } else if (x < x2) {
                    if (z < z2) {
                        f = "➡";
                    } else {
                        f = "⬆";
                    }
                } else if (x > x2) {
                    if (z < z2) {
                        f = "⬇";
                    } else {
                        f = "⬅";
                    }
                }
                break;
            case "ES":
                if (x == x2) {
                    if (z < z2) {
                        f = "⬈";//AbajoDer
                    } else {
                        f = "⬊";
                    }
                } else if (z == z2) {
                    if (x < x2) {
                        f = "⬉";
                    } else {
                        f = "⬋";//AbajoIzq
                    }
                } else if (x < x2) {
                    if (z < z2) {
                        f = "⬆";
                    } else {
                        f = "⬅";
                    }
                } else if (x > x2) {
                    if (z < z2) {
                        f = "➡";
                    } else {
                        f = "⬇";
                    }
                }
                break;
            case "SW":
                if (x == x2) {
                    if (z < z2) {
//                    f = "⬈";
                        f = "⬉";
                    } else {
//                    f = "⬊";
                        f = "⬋";
                    }
                } else if (z == z2) {
                    if (x < x2) {
//                    f = "⬋";
                        f = "⬊";
                    } else {
//                    f = "⬉";
                        f = "⬈";
                    }
                } else if (x < x2) {
                    if (z < z2) {
                        f = "⬅";
                    } else {
                        f = "⬇";
                    }
                } else if (x > x2) {
                    if (z < z2) {
                        f = "⬆";
                    } else {
                        f = "➡";
                    }
                }
                break;
            case "WN":
                if (x == x2) {
                    if (z < z2) {
                        f = "⬊z<";//⬊
                    } else {
                        f = "⬈";
                    }
                } else if (z == z2) {
                    if (x < x2) {
                        f = "⬋";
                    } else {
                        f = "⬉";
                    }
                } else if (x < x2) {
                    if (z < z2) {
                        f = "⬇";
                    } else {
                        f = "➡";
                    }
                } else if (x > x2) {
                    if (z < z2) {
                        f = "⬅";
                    } else {
                        f = "⬆";
                    }
                }
                break;
            default:
                f = "x";
                break;
        }
        return f;
    }

    public static String getDirection(Player player) {
        String dir = "";
        float y = player.getLocation().getYaw();
        if (y < 0) {
            y += 360;
        }
        y %= 360;
        int i = (int) ((y + 8) / 22.5);
        if (i == 0) {
            dir = "S";
        } else if (i == 1) {
            dir = "S";
        } else if (i == 2) {
            dir = "SW";
        } else if (i == 3) {
            dir = "W";
        } else if (i == 4) {
            dir = "W";
        }//Estaba N
        else if (i == 5) {
            dir = "W";
        } else if (i == 6) {
            dir = "WN";
        } else if (i == 7) {
            dir = "N";
        } else if (i == 8) {
            dir = "N";
        } else if (i == 9) {
            dir = "N";
        } else if (i == 10) {
            dir = "NE";
        } else if (i == 11) {
            dir = "E";
        } else if (i == 12) {
            dir = "E";
        } else if (i == 13) {
            dir = "E";
        } else if (i == 14) {
            dir = "ES";
        } else if (i == 15) {
            dir = "S";
        } else {
            dir = "S";
        }
        return dir;
    }
}
