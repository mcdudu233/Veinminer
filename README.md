![banner](https://cdn.modrinth.com/data/cached_images/1aca4c6d88e1b7701c06596e78e51b229eea2395.png)

Mine a single block to mine the whole vein!
Highly configurable and works everywhere, even on your vanilla server.
Make the tedious mining experience to something satisfying and fun!<br>
Veinminer works server side, so all clients are supported. Even cross loaders & versions for addons!

## Paper 中文版

本分支基于上游 Veinminer 2.14.0，为 Paper 26.3 提供中文命令反馈和「连锁挖矿」附魔。两个插件请使用同一个汉化版本。停服后替换原有的主插件和附魔插件 JAR，继续使用 `plugins/Veinminer/` 下的配置。

Paper 启动时会根据 `enchantmentSettings.json` 生成附魔数据，固定显示名为「连锁挖矿」。已有物品仍通过 `veinminer_enchantment:veinminer` 识别附魔。若世界数据包也定义了这个 ID，生效的定义取决于数据包的加载顺序。

使用 JDK 25 编译 Paper 主插件和附魔插件。

~~~sh
./gradlew -PpaperVersion=26.3.build.227-stable :veinminer:veinminer-paper:build :veinminer-enchant:build
~~~

成品位于 `veinminer/paper/build/libs/` 和 `veinminer-enchant/build/libs/`。主插件使用文件名不带 `-dev` 的 JAR。启动后用 `/veinminer help` 查看中文玩法说明，并在物品提示中检查附魔名。

## 🔻 Download
- [**Veinminer Mod/Plugin**](https://modrinth.com/project/veinminer) [(R)](./veinminer/README.md) - Full customizable mod & plugin for Fabric/Quilt, Paper/PurPur/Folia & NeoForge servers
- [**Veinminer Client Addon**](https://modrinth.com/project/veinminer-client) [(R)](./veinminer-client/README.md) - Only veinmine when pressing a hotkey & get a mining preview
- [**Veinminer Enchantment Addon**](https://modrinth.com/project/veinminer-enchantment) [(R)](./veinminer-enchant/README.md) - Add a veinmine enchantment to limit veinmining to only enchanted tools

## ️🧩 Preview
![](https://cdn-raw.modrinth.com/data/OhduvhIc/images/f4c0ad7fa3b8b579753c1f757e80151798717c68.gif)
If you need any help or want to share some ideas to add, just hop on our Discord ([dc.mutils.net](https://dc.mutils.net))

## 🛠️ Development

Task shortcuts for easier testing.

### Run Servers
- `./gradlew :veinminer:veinminer-paper:runServer`
- `./gradlew :veinminer:veinminer-fabric:runServer`
- `./gradlew :veinminer:veinminer-neoforge:runServer`

### Run Clients
- `./gradlew :veinminer-client:veinminer-client-fabric:runClient`
- `./gradlew :veinminer-client:veinminer-client-neoforge:runClient`

### Building (bulked or project separated)
```shell
./gradlew :veinminer:veinminer-paper:build :veinminer:veinminer-fabric:build :veinminer:veinminer-neoforge:build
./gradlew :veinminer-client:veinminer-client-fabric:build :veinminer-client:veinminer-client-neoforge:build
./gradlew :veinminer-enchant:build
```

### Publishing
```shell
./gradlew :veinminer:veinminer-paper:modPublish :veinminer:veinminer-fabric:modPublish :veinminer:veinminer-neoforge:modPublish
./gradlew :veinminer-client:veinminer-client-fabric:modPublish :veinminer-client:veinminer-client-neoforge:modPublish
./gradlew :veinminer-enchant:modPublish
```
