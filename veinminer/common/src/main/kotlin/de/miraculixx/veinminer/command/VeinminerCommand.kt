@file:Suppress("unused", "UNCHECKED_CAST", "BooleanLiteralArgument")

package de.miraculixx.veinminer.command

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import de.miraculixx.veinminer.ActiveConfig
import de.miraculixx.veinminer.data.BlockGroup
import de.miraculixx.veinminer.data.VeinminerSettingsOverride
import de.miraculixx.veinminer.utils.cBase
import de.miraculixx.veinminer.utils.cGreen
import de.miraculixx.veinminer.utils.cHighlight
import de.miraculixx.veinminer.utils.cRed
import de.miraculixx.veinminer.utils.cWhite
import de.miraculixx.veinminer.utils.debug
import de.miraculixx.veinminer.utils.permissionBlocks
import de.miraculixx.veinminer.utils.permissionGroups
import de.miraculixx.veinminer.utils.permissionReload
import de.miraculixx.veinminer.utils.permissionSettings
import de.miraculixx.veinminer.utils.permissionToggle
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument
import net.minecraft.commands.arguments.item.ItemPredicateArgument
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import java.net.URI
import kotlin.reflect.typeOf

object VeinminerCommand {

    fun build(ctx: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> =
        veinminerCommand("veinminer") {
            executesAsync {
                val host = ActiveHost.host
                source.sendSystemMessage(
                    header("连锁挖矿") + cmp("\n版本 ") + value(host.versionVeinminer) +
                        cmp(" (") + value(host.platform) + cmp(" - ") + value(host.versionMinecraft) + cmp(")") +
                        cmp("\n下载地址 ") + link("Modrinth", "https://modrinth.com/project/veinminer") +
                        cmp(" | ") + link("CurseForge", "https://www.curseforge.com/minecraft/mc-mods/veinminer-mod") +
                        cmp("\n点击查看设置指南").link("https://modrinth.com/project/veinminer#config")
                )
            }

            literal("help") {
                executesAsync {
                    source.sendSystemMessage(
                        header("连锁挖矿帮助") +
                            cmp("\n插件已预设矿石规则。想连着挖更多种方块，可以") +
                            link("查看设置指南", "https://modrinth.com/project/veinminer#config") +
                            cmp("，找到「Mod/Plugin Version」一节，再点击对应小节展开说明。")
                    )
                }
            }

            literal("reload") {
                requiresPermission(permissionReload)
                executesAsync {
                    ActiveConfig.bridge.reload(true)
                    source.sendSystemMessage(success("连锁挖矿配置已重新加载。"))
                }
            }

            literal("blocks") {
                requiresPermission(permissionBlocks)
                executesAsync { source.sendSystemMessage(usage("/veinminer blocks <add/remove> <block>")) }

                literal("add") {
                    argument("block", BlockPredicateArgument.blockPredicate(ctx)) {
                        executesAsync {
                            val id = getRaw(3) ?: return@executesAsync source.sendSystemMessage(error("请指定一个方块或方块标签。"))
                            if (ActiveConfig.bridge.veinBlocksRaw.add(id)) {
                                ActiveConfig.bridge.save()
                                source.sendSystemMessage(success("已将 ") + value(id) + success(" 加入连锁挖矿方块列表。"))
                            } else {
                                source.sendSystemMessage(value(id) + error(" 已在连锁挖矿方块列表中。"))
                            }
                        }
                    }
                }

                literal("remove") {
                    argument("block", BlockPredicateArgument.blockPredicate(ctx)) {
                        suggestStrings { c -> c.suggestFilteredList(ActiveConfig.bridge.veinBlocksRaw) }
                        executesAsync {
                            val id = getRaw(3) ?: return@executesAsync source.sendSystemMessage(error("请指定一个方块或方块标签。"))
                            if (ActiveConfig.bridge.veinBlocksRaw.remove(id)) {
                                ActiveConfig.bridge.save()
                                source.sendSystemMessage(success("已从连锁挖矿方块列表移除 ") + value(id))
                            } else {
                                source.sendSystemMessage(value(id) + error(" 不在连锁挖矿方块列表中。"))
                            }
                        }
                    }
                }
            }

            literal("toggle") {
                requiresPermission(permissionToggle)
                executesAsync {
                    if (ActiveHost.host.active) source.sendSystemMessage(error("连锁挖矿已关闭。"))
                    else source.sendSystemMessage(success("连锁挖矿已开启。"))
                    ActiveHost.host.active = !ActiveHost.host.active
                }
            }

            literal("settings") {
                requiresPermission(permissionSettings)
                executesAsync { source.sendSystemMessage(usage("/veinminer settings <setting> [<new-value>]")) }

                applySetting("mustSneak", { ActiveConfig.bridge.settings.mustSneak }) { x, _ -> ActiveConfig.bridge.settings.mustSneak = x }
                applySetting("cooldown", { ActiveConfig.bridge.settings.cooldown }) { x, _ -> ActiveConfig.bridge.settings.cooldown = x }
                applySetting("delay", { ActiveConfig.bridge.settings.delay }) { x, _ -> ActiveConfig.bridge.settings.delay = x }
                applySetting("maxChain", { ActiveConfig.bridge.settings.maxChain }) { x, _ -> ActiveConfig.bridge.settings.maxChain = x }
                applySetting("needCorrectTool", { ActiveConfig.bridge.settings.needCorrectTool }) { x, _ -> ActiveConfig.bridge.settings.needCorrectTool = x }
                applySetting("searchRadius", { ActiveConfig.bridge.settings.searchRadius }) { x, _ -> ActiveConfig.bridge.settings.searchRadius = x }
                applySetting("permissionRestricted", { ActiveConfig.bridge.settings.permissionRestricted }) { x, _ -> ActiveConfig.bridge.settings.permissionRestricted = x }
                applySetting("mergeItemDrops", { ActiveConfig.bridge.settings.mergeItemDrops }) { x, _ -> ActiveConfig.bridge.settings.mergeItemDrops = x }
                applySetting("decreaseDurability", { ActiveConfig.bridge.settings.decreaseDurability }) { x, _ -> ActiveConfig.bridge.settings.decreaseDurability = x }
                applySetting("hungerPerBlock", { ActiveConfig.bridge.settings.hungerPerBlock }) { x, _ -> ActiveConfig.bridge.settings.hungerPerBlock = x }
                applySetting("miningSpeedModifier", { ActiveConfig.bridge.settings.miningSpeedModifier }) { x, _ -> ActiveConfig.bridge.settings.miningSpeedModifier = x }
                applySetting("separateGroupMining", { ActiveConfig.bridge.settings.separateGroupMining }) { x, _ -> ActiveConfig.bridge.settings.separateGroupMining = x }
                applySetting("minEnchantLevel", { ActiveConfig.bridge.settings.minEnchantLevel }) { x, _ -> ActiveConfig.bridge.settings.minEnchantLevel = x }
                applySetting("debug", { debug }) { x, _ -> ActiveConfig.bridge.settings.debug = x }
                literal("client") {
                    applySetting("allow", { ActiveConfig.bridge.settings.client.allow }) { x, _ -> ActiveConfig.bridge.settings.client.allow = x }
                    applySetting("require", { ActiveConfig.bridge.settings.client.require }) { x, _ -> ActiveConfig.bridge.settings.client.require = x }
                    applySetting("translucentBlockHighlight", { ActiveConfig.bridge.settings.client.translucentBlockHighlight }) { x, _ -> ActiveConfig.bridge.settings.client.translucentBlockHighlight = x }
                    applySetting("allowAllBlocks", { ActiveConfig.bridge.settings.client.allBlocks }) { x, _ -> ActiveConfig.bridge.settings.client.allBlocks = x }
                    applySetting("allowClientGroups", { ActiveConfig.bridge.settings.client.allowClientGroups }) { x, _ -> ActiveConfig.bridge.settings.client.allowClientGroups = x }
                    literal("override") {
                        overrides { _ -> ActiveConfig.bridge.settings.client.overrides }
                    }
                }
            }

            literal("enchant") {
                executesAsync {
                    if (!ActiveHost.host.enchantmentActive) return@executesAsync source.sendSystemMessage(
                        header("连锁挖矿附魔") +
                            cmp("\n要使用这项附魔，请先安装附魔插件。下载地址 ", cBase) +
                            link("Modrinth", "https://modrinth.com/mod/veinminer-enchantment") +
                            cmp(" | ") + link("CurseForge", "https://www.curseforge.com/minecraft/mc-mods/veinminer-enchant")
                    )
                    source.sendSystemMessage(
                        header("连锁挖矿附魔") +
                            cmp("\n有管理权限时，可以用这条命令修改附魔设置。\n", cBase) + command("/veinminer enchant <setting> [<new-value>]") +
                            cmp("\n改动会在服务器重启或重新进入单人世界后生效。点击查看设置说明。", cBase)
                                .link("https://modrinth.com/mod/veinminer-enchantment#settings")
                    )
                }

                requiresAll({ ActiveHost.host.enchantmentActive && Permissions.check(it, permissionSettings) }) {
                    val enchant = { ActiveConfig.bridge.enchantSettings }
                    applySetting("maxLevel", { enchant().maxLevel }, restart = true) { x, _ -> enchant().maxLevel = x }
                    applySetting("weight", { enchant().weight }, restart = true) { x, _ -> enchant().weight = x }
                    applySetting("minCost", { enchant().minCost }, restart = true) { x, _ -> enchant().minCost = x }
                    applySetting("minCostPerLevel", { enchant().minCostPerLevel }, restart = true) { x, _ -> enchant().minCostPerLevel = x }
                    applySetting("maxCost", { enchant().maxCost }, restart = true) { x, _ -> enchant().maxCost = x }
                    applySetting("maxCostPerLevel", { enchant().maxCostPerLevel }, restart = true) { x, _ -> enchant().maxCostPerLevel = x }
                    applySetting("anvilCost", { enchant().anvilCost }, restart = true) { x, _ -> enchant().anvilCost = x }
                    applySetting("inEnchantingTable", { enchant().inEnchantingTable }, restart = true) { x, _ -> enchant().inEnchantingTable = x }
                    applySetting("tradeable", { enchant().tradeable }, restart = true) { x, _ -> enchant().tradeable = x }
                    applySetting("lootable", { enchant().lootable }, restart = true) { x, _ -> enchant().lootable = x }
                    applySetting("treasure", { enchant().treasure }, restart = true) { x, _ -> enchant().treasure = x }
                    literal("supportedItems") {
                        executesAsync { source.sendSystemMessage(setting("supportedItems", enchant().supportedItems)) }
                        argument("supportedItems-new", ItemPredicateArgument.itemPredicate(ctx)) {
                            executesAsync {
                                val raw = getRaw(3)
                                if (raw == null || !source.isValidItemTarget(raw)) return@executesAsync source.sendSystemMessage(error("请输入物品 ID 或物品标签，例如 minecraft:diamond_pickaxe 或 #minecraft:pickaxes。"))
                                enchant().supportedItems = raw
                                ActiveConfig.bridge.save()
                                source.sendSystemMessage(settingChanged("supportedItems", raw) + restartNote())
                            }
                        }
                    }
                }
            }

            literal("groups") {
                requiresPermission(permissionGroups)
                fun groupExists(group: String) =
                    ActiveConfig.bridge.groupsRaw.firstOrNull { it.name.equals(group, ignoreCase = true) }

                fun CommandSourceStack.createGroup(name: String, content: MutableSet<String>) {
                    if (groupExists(name) != null) {
                        sendSystemMessage(error("分组 ") + value(name) + error(" 已存在。"))
                        return
                    }
                    ActiveConfig.bridge.groupsRaw.add(BlockGroup(name, content))
                    sendSystemMessage(
                        success("已创建分组 ") + value(name) +
                            cmp("\n用这条命令添加方块。\n", cBase) +
                            command("/veinminer groups edit $name add-block <block>")
                    )
                    ActiveConfig.bridge.save()
                }

                fun CommandSourceStack.editContent(groupName: String, rawKey: String?, isBlock: Boolean, isAdd: Boolean) {
                    val group = groupExists(groupName) ?: return sendSystemMessage(error("找不到分组 ") + value(groupName))
                    val set = if (isBlock) group.blocks else group.tools
                    if (rawKey == null) return sendSystemMessage(error("请指定要修改的方块、工具或标签。"))
                    val label = if (isBlock) "方块" else "工具"
                    if (isAdd) {
                        if (set.add(rawKey)) sendSystemMessage(success("已将$label ") + value(rawKey) + success(" 加入分组 ") + value(groupName))
                        else return sendSystemMessage(value(rawKey) + error(" 已在分组 ") + value(groupName) + error(" 中。"))
                    } else {
                        if (set.remove(rawKey)) sendSystemMessage(success("已从分组 ") + value(groupName) + success(" 移除$label ") + value(rawKey))
                        else return sendSystemMessage(error("分组 ") + value(groupName) + error(" 中找不到 ") + value(rawKey))
                    }
                    ActiveConfig.bridge.save()
                }

                executesAsync {
                    source.sendSystemMessage(
                        header("连锁挖矿分组") +
                            cmp("\n同一组里的不同方块可以一起连锁挖掘。", cBase) +
                            cmp("\n例如，", cBase) + value("Ores") + cmp("（矿石）分组可以同时包含铁矿石和深层铁矿石。", cBase) +
                            cmp("\n你也可以给每个分组指定可用的工具。", cBase)
                    )
                }

                literal("list") {
                    fun BlockGroup<String>.print(source: CommandSourceStack) {
                        source.sendSystemMessage(
                            header("分组 ") + value(name) +
                                cmp("\n方块 ", cBase) + list(blocks) +
                                cmp("\n工具 ", cBase) + list(tools, "不限")
                        )
                    }

                    stringArg("group") {
                        suggestStrings { ActiveConfig.bridge.groupsRaw.map { it.name } }
                        executesAsync {
                            val name = StringArgumentType.getString(this, "group")
                            val group = groupExists(name) ?: return@executesAsync source.sendSystemMessage(error("找不到分组 ") + value(name))
                            group.print(source)
                        }
                    }

                    executesAsync {
                        val groups = ActiveConfig.bridge.groupsRaw
                        if (groups.isEmpty()) source.sendSystemMessage(cmp("还没有设置任何分组。"))
                        else groups.forEach { group -> group.print(source) }
                    }
                }

                literal("create") {
                    stringArg("name") {
                        executesAsync {
                            val name = StringArgumentType.getString(this, "name")
                            source.createGroup(name, mutableSetOf())
                        }
                        argument("block1", BlockPredicateArgument.blockPredicate(ctx)) {
                            executesAsync {
                                val name = StringArgumentType.getString(this, "name")
                                source.createGroup(name, getRaw(4)?.let { mutableSetOf(it) } ?: mutableSetOf())
                            }
                            argument("block2", BlockPredicateArgument.blockPredicate(ctx)) {
                                executesAsync {
                                    val name = StringArgumentType.getString(this, "name")
                                    source.createGroup(name, setOf(getRaw(4), getRaw(5)).mapNotNull { it }.toMutableSet())
                                }
                            }
                        }
                    }
                }

                literal("remove") {
                    stringArg("group") {
                        suggestStrings { ActiveConfig.bridge.groupsRaw.map { it.name } }
                        executesAsync {
                            val name = StringArgumentType.getString(this, "group").lowercase()
                            val group = groupExists(name) ?: return@executesAsync source.sendSystemMessage(error("找不到分组 ") + value(name))
                            ActiveConfig.bridge.groupsRaw.removeIf { it === group } // data class hash changes on edit, set.remove would miss it
                            source.sendSystemMessage(success("已移除分组 ") + value(name))
                            ActiveConfig.bridge.save()
                        }
                    }
                }

                literal("edit") {
                    stringArg("group") {
                        suggestStrings { ActiveConfig.bridge.groupsRaw.map { it.name } }
                        literal("add-block") {
                            argument("block", BlockPredicateArgument.blockPredicate(ctx)) {
                                executesAsync {
                                    val name = StringArgumentType.getString(this, "group")
                                    source.editContent(name, getRaw(5), true, true)
                                }
                            }
                        }
                        literal("remove-block") {
                            argument("block", BlockPredicateArgument.blockPredicate(ctx)) {
                                suggestStrings { c ->
                                    val group = c.getArgument("group", String::class.java)
                                    c.suggestFilteredList(groupExists(group)?.blocks)
                                }
                                executesAsync {
                                    val name = StringArgumentType.getString(this, "group")
                                    source.editContent(name, getRaw(5), true, false)
                                }
                            }
                        }
                        literal("add-tool") {
                            argument("tool", ItemPredicateArgument.itemPredicate(ctx)) {
                                executesAsync {
                                    val name = StringArgumentType.getString(this, "group")
                                    source.editContent(name, getRaw(5), false, true)
                                }
                            }
                        }
                        literal("remove-tool") {
                            argument("tool", ItemPredicateArgument.itemPredicate(ctx)) {
                                suggestStrings { c ->
                                    val group = c.getArgument("group", String::class.java)
                                    c.suggestFilteredList(groupExists(group)?.tools)
                                }
                                executesAsync {
                                    val name = StringArgumentType.getString(this, "group")
                                    source.editContent(name, getRaw(5), false, false)
                                }
                            }
                        }
                        literal("override") {
                            overrides { name -> groupExists(name)?.override }
                        }
                    }
                }
            }

            literal("presets") {
                requiresPermission(permissionGroups)
                executesAsync { source.sendSystemMessage(cmp("用预设可以快速添加一组方块和工具。输入预设名即可添加。")) }
                fun CommandNodeBuilder<CommandSourceStack>.addPreset(name: String, blocks: Set<String>, tools: Set<String>, override: VeinminerSettingsOverride = VeinminerSettingsOverride()) {
                    literal(name) {
                        executesAsync {
                            if (ActiveConfig.bridge.groupsRaw.any { it.name.equals(name, ignoreCase = true) }) {
                                return@executesAsync source.sendSystemMessage(error("分组 ") + value(name) + error(" 已存在。"))
                            }
                            ActiveConfig.bridge.groupsRaw.add(BlockGroup(name, blocks.toMutableSet(), tools.toMutableSet(), override))
                            source.sendSystemMessage(
                                success("已添加预设分组 ") + value(name) +
                                    cmp("\n方块 ", cBase) + list(blocks) +
                                    cmp("\n工具 ", cBase) + list(tools, "不限")
                            )
                            ActiveConfig.bridge.save()
                        }
                    }
                }

                addPreset("Logs", mutableSetOf("#minecraft:logs", "minecraft:mushroom_stem", "minecraft:brown_mushroom_block", "minecraft:red_mushroom_block"), mutableSetOf("#minecraft:axes"))
                addPreset("Leaves", mutableSetOf("#minecraft:leaves"), mutableSetOf("#minecraft:hoes", "minecraft:shears"))
                addPreset("Crops", mutableSetOf("#minecraft:crops"), mutableSetOf())
                addPreset("ConcretePowder", mutableSetOf("#minecraft:concrete_powder"), mutableSetOf("#minecraft:shovels"))
                addPreset("Terracotta", mutableSetOf("#minecraft:terracotta"), mutableSetOf("#minecraft:pickaxes"))
                addPreset("Grassy", mutableSetOf("#minecraft:edible_for_sheep", "minecraft:tall_grass", "minecraft:large_fern", "minecraft:bush", "minecraft:dead_bush"), mutableSetOf(), VeinminerSettingsOverride(searchRadius = 3, maxChain = 50))
                addPreset("Stones", mutableSetOf("#minecraft:base_stone_overworld", "minecraft:cobblestone", "minecraft:cobbled_deepslate"), mutableSetOf("#minecraft:pickaxes"))
                addPreset("NetherStones", mutableSetOf("#minecraft:base_stone_nether", "minecraft:smooth_basalt"), mutableSetOf("#minecraft:pickaxes"))
            }
        }

    private inline fun <reified T> CommandNodeBuilder<CommandSourceStack>.applySetting(
        name: String,
        noinline currentConsumer: (CommandContext<CommandSourceStack>) -> T,
        restart: Boolean = false,
        noinline consumer: (T, CommandContext<CommandSourceStack>) -> Unit,
    ) {
        literal(name) {
            executesAsync {
                source.sendSystemMessage(setting(name, currentConsumer(this)))
            }

            when (typeOf<T>()) {
                typeOf<Boolean>(), typeOf<Boolean?>() -> boolArg("$name-new") {
                    executesAsync {
                        val value = BoolArgumentType.getBool(this, "$name-new") as T
                        consumer.invoke(value, this)
                        ActiveConfig.bridge.save()
                        source.sendSystemMessage(if (restart) settingChanged(name, value) + restartNote() else settingChanged(name, value))
                    }
                }

                typeOf<Int>(), typeOf<Int?>() -> intArg("$name-new", min = 0) {
                    executesAsync {
                        val value = IntegerArgumentType.getInteger(this, "$name-new") as T
                        consumer.invoke(value, this)
                        ActiveConfig.bridge.save()
                        source.sendSystemMessage(if (restart) settingChanged(name, value) + restartNote() else settingChanged(name, value))
                    }
                }

                typeOf<Double>(), typeOf<Double?>() -> doubleArg("$name-new", min = 0.0, max = 1.0) {
                    executesAsync {
                        val value = DoubleArgumentType.getDouble(this, "$name-new") as T
                        consumer.invoke(value, this)
                        ActiveConfig.bridge.save()
                        source.sendSystemMessage(if (restart) settingChanged(name, value) + restartNote() else settingChanged(name, value))
                    }
                }
            }
        }
    }

    private fun CommandContext<CommandSourceStack>.suggestFilteredList(list: Collection<String>?): List<String> {
        val input = input.split(' ').lastOrNull() ?: ""
        return list?.filter { input.isEmpty() || it.startsWith(input) } ?: emptyList()
    }

    private fun CommandContext<CommandSourceStack>.getRaw(idx: Int) = input.split(' ').getOrNull(idx)

    private fun CommandSourceStack.isValidItemTarget(raw: String): Boolean {
        val id = Identifier.tryParse(raw.removePrefix("#")) ?: return false
        val items = server.registryAccess().lookupOrThrow(Registries.ITEM)
        return if (raw.startsWith("#")) items.get(TagKey.create(Registries.ITEM, id)).isPresent else items.containsKey(id)
    }

    private fun CommandNodeBuilder<CommandSourceStack>.overrides(
        resolver: (String) -> VeinminerSettingsOverride?,
    ) {
        fun CommandContext<CommandSourceStack>.resolve(): VeinminerSettingsOverride {
            val name = runCatching { getArgument("group", String::class.java) }.getOrNull() ?: "client"
            val override = resolver.invoke(name)
            return if (override == null) {
                ActiveHost.host.logger.warn("Tried to access override for '${name}', but it does not exist. Changes are not saved!")
                VeinminerSettingsOverride()
            } else override
        }

        literal("unset") {
            stringArg("key") {
                suggestStrings { c -> c.resolve().nonNullKeys() }
                executesAsync {
                    val key = StringArgumentType.getString(this, "key")
                    if (resolve().unset(key)) {
                        source.sendSystemMessage(success("已取消单独设置 ") + value(key))
                        ActiveConfig.bridge.save()
                    } else {
                        source.sendSystemMessage(value(key) + error(" 尚未单独设置。"))
                    }
                }
            }
        }

        applySetting("cooldown", { args -> args.resolve().cooldown }) { x, args -> args.resolve().cooldown = x }
        applySetting("mustSneak", { args -> args.resolve().mustSneak }) { x, args -> args.resolve().mustSneak = x }
        applySetting("delay", { args -> args.resolve().delay }) { x, args -> args.resolve().delay = x }
        applySetting("maxChain", { args -> args.resolve().maxChain }) { x, args -> args.resolve().maxChain = x }
        applySetting("needCorrectTool", { args -> args.resolve().needCorrectTool }) { x, args -> args.resolve().needCorrectTool = x }
        applySetting("searchRadius", { args -> args.resolve().searchRadius }) { x, args -> args.resolve().searchRadius = x }
        applySetting("permissionRestricted", { args -> args.resolve().permissionRestricted }) { x, args -> args.resolve().permissionRestricted = x }
        applySetting("decreaseDurability", { args -> args.resolve().decreaseDurability }) { x, args -> args.resolve().decreaseDurability = x }
        applySetting("hungerPerBlock", { args -> args.resolve().hungerPerBlock }) { x, args -> args.resolve().hungerPerBlock = x }
        applySetting("miningSpeedModifier", { args -> args.resolve().miningSpeedModifier }) { x, args -> args.resolve().miningSpeedModifier = x }
        applySetting("separateGroupMining", { args -> args.resolve().separateGroupMining }) { x, args -> args.resolve().separateGroupMining = x }
        applySetting("minEnchantLevel", { args -> args.resolve().minEnchantLevel }) { x, args -> args.resolve().minEnchantLevel = x }
    }

    private fun header(text: String) = cmp(text, cGreen, bold = true)
    private fun success(text: String) = cmp(text, cGreen)
    private fun error(text: String) = cmp(text, cRed)
    private fun value(value: Any?) = cmp(value.toString(), cGreen)
    private fun command(text: String) = cmp(text, cGreen).suggest(text).hover(cmp("点击填入命令", cWhite))
    fun link(name: String, url: String) = cmp(name, cHighlight, true).link(url).hover(cmp("点击打开链接", cWhite))
    private fun usage(syntax: String) = error("命令用法 ") + command(syntax).withColor(cBase)
    private fun setting(name: String, current: Any?) = value(name) + cmp(" 当前为 ") + settingValue(current)
    private fun settingChanged(name: String, newValue: Any?) = success("已将 ") + value(name) + success(" 设为 ") + settingValue(newValue)
    private fun restartNote() = cmp("（服务器重启或重新进入单人世界后生效）", cBase)

    private fun settingValue(current: Any?) = value(when (current) {
        true -> "开启"
        false -> "关闭"
        null -> "未单独设置"
        else -> current
    })

    private fun list(values: Collection<String>, empty: String = "无"): MutableComponent {
        if (values.isEmpty()) return value(empty)
        val component = cmp("[", cBase)
        values.forEachIndexed { index, item ->
            if (index > 0) component.append(cmp(", ", cBase))
            component.append(value(item))
        }
        return component.append(cmp("]", cBase))
    }

    private fun cmp(text: String, color: Int = cBase, bold: Boolean = false, italic: Boolean = false) =
        Component.literal(text).withColor(color).withStyle { it.withBold(bold).withItalic(italic) }
    private operator fun MutableComponent.plus(other: Component) = this.append(other)
    private fun MutableComponent.suggest(command: String) = this.withStyle { it.withClickEvent(ClickEvent.SuggestCommand(command)) }
    private fun MutableComponent.link(url: String) = this.withStyle { it.withClickEvent(ClickEvent.OpenUrl(URI(url))) }
    private fun MutableComponent.hover(cmp: Component) = this.withStyle { it.withHoverEvent(HoverEvent.ShowText(cmp)) }
}
