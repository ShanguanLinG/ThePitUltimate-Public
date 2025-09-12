// 已有变量:
// Bukkit
// Pit

var Player = Packages.org.bukkit.entity.Player.class;

function description(enchantLevel) {
    return `&7给你击飞得老高咯, 还带个闪电`
}


function attack(enchantLevel, attacker, target, damage, finalDamage, boostDamage, cancel) {
    if (Player.isInstance(target)) {
        Bukkit.getScheduler().runTaskLater(Pit, () => {
            target.setVelocity(target.getVelocity().setY(2.0))
            target.getLocation().getWorld().strikeLightningEffect(target.getLocation())
        }, 1)
    }
}