package net.mizukilab.pit.config

import cn.charlotte.pit.ThePit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * @author Araykal
 * @since 2025/5/12
 */
object TabConfiguration {
    var enable = true
    var tick = 2;
    var delay = 0;
    var animation = HashMap<String, List<String>>()
    var head: List<String> = listOf("   {harrys}   ", "   &bYou are playing on {title}   ", "/s")
    var part: List<String> =
        listOf("/s", "&aRanks, Boosters &a& &aMORE! &c&lSTORE.HYPIXEL.NET", "&eBoost: &b%pit_boost%")
    lateinit var config: YamlConfiguration
    private val title = listOf(
        "&eM&eC&e.&eH&eY&eP&eI&eX&eE&eL&e.&eN&eE&eT",
        "&6M&eC&e.&eH&eY&eP&eI&eX&eE&eL&e.&eN&eE&eT",
        "&6M&6C&e.&eH&eY&eP&eI&eX&eE&eL&e.&eN&eE&eT",
        "&6M&6C&6.&eH&eY&eP&eI&eX&eE&eL&e.&eN&eE&eT",
        "&6M&6C&6.&6H&eY&eP&eI&eX&eE&eL&e.&eN&eE&eT",
        "&6M&6C&6.&6H&6Y&eP&eI&eX&eE&eL&e.&eN&eE&eT",
        "&6M&6C&6.&6H&6Y&6P&eI&eX&eE&eL&e.&eN&eE&eT",
        "&6M&6C&6.&6H&6Y&6P&6I&eX&eE&eL&e.&eN&eE&eT",
        "&6M&6C&6.&6H&6Y&6P&6I&6X&eE&eL&e.&eN&eE&eT",
        "&6M&6C&6.&6H&6Y&6P&6I&6X&6E&eL&e.&eN&eE&eT",
        "&6M&6C&6.&6H&6Y&6P&6I&6X&6E&6L&e.&eN&eE&eT",
        "&6M&6C&6.&6H&6Y&6P&6I&6X&6E&6L&6.&eN&eE&eT",
        "&6M&6C&6.&6H&6Y&6P&6I&6X&6E&6L&6.&6N&eE&eT",
        "&6M&6C&6.&6H&6Y&6P&6I&6X&6E&6L&6.&6N&6E&eT",
        "&6M&6C&6.&6H&6Y&6P&6I&6X&6E&6L&6.&6N&6E&6T",
        "&6M&6C&6.&6H&6Y&6P&6I&6X&6E&6L&6.&6N&6E&eT",
        "&6M&6C&6.&6H&6Y&6P&6I&6X&6E&6L&6.&6N&eE&eT",
        "&6M&6C&6.&6H&6Y&6P&6I&6X&6E&6L&e.&eN&eE&eT",
        "&6M&6C&6.&6H&6Y&6P&6I&6X&eE&eL&e.&eN&eE&eT",
        "&6M&6C&6.&6H&6Y&6P&eI&eX&eE&eL&e.&eN&eE&eT",
        "&6M&6C&6.&6H&6Y&eP&eI&eX&eE&eL&e.&eN&eE&eT",
        "&6M&6C&6.&6H&eY&eP&eI&eX&eE&eL&e.&eN&eE&eT",
        "&6M&6C&6.&eH&eY&eP&eI&eX&eE&eL&e.&eN&eE&eT",
        "&6M&6C&e.&eH&eY&eP&eI&eX&eE&eL&e.&eN&eE&eT",
        "&6M&eC&e.&eH&eY&eP&eI&eX&eE&eL&e.&eN&eE&eT",
        "&eM&eC&e.&eH&eY&eP&eI&eX&eE&eL&e.&eN&eE&eT"
    )

    private val harrys = listOf(
        "&e&lH&e&lA&e&lR&e&lR&e&lY&e&l'&e&lS&e&l &e&lN&e&lE&e&lT&e&lW&e&lO&e&lR&e&lK",
        "&6&lH&e&lA&e&lR&e&lR&e&lY&e&l'&e&lS&e&l &e&lN&e&lE&e&lT&e&lW&e&lO&e&lR&e&lK",
        "&6&lH&6&lA&e&lR&e&lR&e&lY&e&l'&e&lS&e&l &e&lN&e&lE&e&lT&e&lW&e&lO&e&lR&e&lK",
        "&6&lH&6&lA&6&lR&e&lR&e&lY&e&l'&e&lS&e&l &e&lN&e&lE&e&lT&e&lW&e&lO&e&lR&e&lK",
        "&6&lH&6&lA&6&lR&6&lR&e&lY&e&l'&e&lS&e&l &e&lN&e&lE&e&lT&e&lW&e&lO&e&lR&e&lK",
        "&6&lH&6&lA&6&lR&6&lR&6&lY&e&l'&e&lS&e&l &e&lN&e&lE&e&lT&e&lW&e&lO&e&lR&e&lK",
        "&6&lH&6&lA&6&lR&6&lR&6&lY&6&l'&e&lS&e&l &e&lN&e&lE&e&lT&e&lW&e&lO&e&lR&e&lK",
        "&6&lH&6&lA&6&lR&6&lR&6&lY&6&l'&6&lS&e&l &e&lN&e&lE&e&lT&e&lW&e&lO&e&lR&e&lK",
        "&6&lH&6&lA&6&lR&6&lR&6&lY&6&l'&6&lS&6&l &e&lN&e&lE&e&lT&e&lW&e&lO&e&lR&e&lK",
        "&e&lH&6&lA&6&lR&6&lR&6&lY&6&l'&6&lS&6&l &6&lN&e&lE&e&lT&e&lW&e&lO&e&lR&e&lK",
        "&e&lH&e&lA&6&lR&6&lR&6&lY&6&l'&6&lS&6&l &6&lN&6&lE&e&lT&e&lW&e&lO&e&lR&e&lK",
        "&e&lH&e&lA&e&lR&6&lR&6&lY&6&l'&6&lS&6&l &6&lN&6&lE&6&lT&e&lW&e&lO&e&lR&e&lK",
        "&e&lH&e&lA&e&lR&e&lR&6&lY&6&l'&6&lS&6&l &6&lN&6&lE&6&lT&6&lW&e&lO&e&lR&e&lK",
        "&e&lH&e&lA&e&lR&e&lR&e&lY&6&l'&6&lS&6&l &6&lN&6&lE&6&lT&6&lW&6&lO&e&lR&e&lK",
        "&e&lH&e&lA&e&lR&e&lR&e&lY&e&l'&6&lS&6&l &6&lN&6&lE&6&lT&6&lW&6&lO&6&lR&e&lK",
        "&e&lH&e&lA&e&lR&e&lR&e&lY&e&l'&e&lS&6&l &6&lN&6&lE&6&lT&6&lW&6&lO&6&lR&6&lK",
        "&e&lH&e&lA&e&lR&e&lR&e&lY&e&l'&e&lS&e&l &e&lN&e&lE&e&lT&e&lW&e&lO&e&lR&e&lK"
    )

    fun save() {
        config.save(File(ThePit.getInstance().dataFolder, "tab.yml"))
    }

    fun loadFile() {
        val file = File(ThePit.getInstance().dataFolder, "tab.yml")
        config = YamlConfiguration.loadConfiguration(file)
    }

    fun load() {
        refreshAndSave()
        enable = config.getBoolean("enable")
        tick = config.getInt("tick")
        delay = config.getInt("delay")
        config.getConfigurationSection("animation").getKeys(false).forEach {
            animation[it] = this.config.getStringList("animation.${it}")
        }
        head = this.config.getStringList("head")
        part = this.config.getStringList("part")
    }

    private fun refreshAndSave() {
        this.defaults.entries.forEach {
            if (this.config.get(it.key) == null) {
                this.config.set(it.key, it.value)
            }
        }
        this.config.save(File(ThePit.getInstance().dataFolder, "tab.yml"))
    }

    private val defaults = mapOf(
        "enable" to true,
        "tick" to tick,
        "delay" to delay,
        "animation.title" to title,
        "animation.harrys" to harrys,
        "head" to head,
        "part" to part,
    )

}