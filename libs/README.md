# 锁定依赖

`dependencies.lock.json` 是构建依赖的唯一清单，记录文件名、SHA-256、下载地址、Gradle 配置和可选性。构建不再扫描并引入目录中的所有 jar。

| 依赖 | 版本 | 用途 |
| --- | --- | --- |
| Thaumaturge | 1.0.0，本地上游源码构建 | 编译与运行必需 |
| Lithostitched | 1.8.0，NeoForge 21.1 | Thaumaturge 运行依赖 |
| Botania | 457，本地现有构件 | 编译与运行必需 |
| Curios | 9.5.1+1.21.1 | 编译与运行必需 |
| TerraBlender | 4.1.0.8 | 可选开发依赖，新版 Thaumaturge 已不再要求 |
| Patchouli | 93 | Botania 运行依赖 |
| JEI、Mezz Config、Jade | 见锁定清单 | 可选开发环境，仅加入运行类路径 |

Thaumaturge 构建来自上游 `1.21.1` 分支提交 `b4670aeacb76f22a53dd3b57ba04de2eaa0f70de`，包含上游 datagen 生成资源。JEI 固定为上游使用的 19.36.0.360，避免其内部搜索接口与较新版本不兼容。锁定清单中的 `sourceCommit` 用于辨认同一版本号下的源码修订。

本地光影开发环境使用 Iris 1.8.14-beta.1 与 Sodium 0.8.13，放在 `run/mods`；Iris 1.8.12 与 Sodium 0.6.13 的组合不兼容新版 Thaumaturge。这两项不是本模组的必需依赖。

在项目根目录执行 `pwsh -File scripts/setup-dependencies.ps1 -Source "前置 jar 所在目录"`。可用的公开地址会自动下载；本地构建构件需要从指定目录提供，或设置 `DEPS_BASE_URL` 为存放这些文件的下载目录。添加 `-Optional` 可同时安装可选依赖。

脚本与 Gradle 都会验证文件哈希。替换依赖时需要审核并更新锁定清单；核心前置的重复版本会阻止构建。jar 本身不入库。

GitHub Actions 使用仓库 secret `DEPS_BASE_URL` 获取无公开下载地址的文件。该地址应提供锁定清单中的原文件，CI 不会上传或重新发布依赖 jar。
