# 锁定依赖

`dependencies.lock.json` 是构建依赖的唯一清单，记录文件名、SHA-256、下载地址、Gradle 配置和可选性。构建不再扫描并引入目录中的所有 jar。

| 依赖 | 版本 | 用途 |
| --- | --- | --- |
| Thaumaturge | 1.1.0，CurseForge 正式发布包 | 编译与运行必需 |
| Lithostitched | 1.8.0，NeoForge 21.1 | Thaumaturge 运行依赖 |
| Botania | 457，本地现有构件 | 编译与运行必需 |
| Curios | 9.5.1+1.21.1 | 编译与运行必需 |
| TerraBlender | 4.1.0.8 | 可选开发依赖，新版 Thaumaturge 已不再要求 |
| Patchouli | 93 | Botania 运行依赖 |
| JEI | 19.64.0.455 | 可选开发依赖，与 Thaumaturge 1.1.0 的 JEI 注入匹配 |
| Mezz Config | 0.6.3 | 新版 JEI 的运行依赖 |
| Jade | 见锁定清单 | 可选开发环境，仅加入运行类路径 |
| Modern UI | 3.13.0.1，Minecraft 1.21.1 NeoForge | 可选开发环境，用于客户端界面与字体渲染 |

Thaumaturge 使用 CurseForge 的 Minecraft 1.21.1 / NeoForge 1.1.0 正式发布包，锁定文件名为 `thaumaturge-1.21.1-NeoForge-1.1.0.jar`，SHA-256 为 `2671390136fad5ac9abd36797cb0a5a938967a5c32399442589bb7720d463900`，可由依赖脚本自动下载。项目 1.8.0 使用新版 `spendVis`、`addVis` 和 `drawFromAura` 充能 API，最低要求 Thaumaturge 1.1.0。研究笔记注入、数据生成及 13 项游戏内测试已在此发布包上验证。

本地光影开发环境使用 Iris 1.8.14-beta.1 与 Sodium 0.8.13，放在 `run/mods`；Iris 1.8.12 与 Sodium 0.6.13 的组合不兼容新版 Thaumaturge。这两项不是本模组的必需依赖。

在项目根目录执行 `pwsh -File scripts/setup-dependencies.ps1 -Source "前置 jar 所在目录"`。可用的公开地址会自动下载；本地构建构件需要从指定目录提供，或设置 `DEPS_BASE_URL` 为存放这些文件的下载目录。添加 `-Optional` 可同时安装可选依赖。

脚本与 Gradle 都会验证文件哈希。替换依赖时需要审核并更新锁定清单；核心前置的重复版本会阻止构建。jar 本身不入库。

GitHub Actions 使用仓库 secret `DEPS_BASE_URL` 获取无公开下载地址的文件。该地址应提供锁定清单中的原文件，CI 不会上传或重新发布依赖 jar。
