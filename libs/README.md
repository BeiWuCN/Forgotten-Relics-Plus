# libs/

扁平文件形式的模组前置。Thaumaturge 不发布可用的 maven 构件，扁平文件能保证编译期用的就是运行期那一份。

| 文件 | 用途 |
| --- | --- |
| `thaumaturge-*.jar` | 提供奥术合成 API、要素、灵气与研究系统。 |
| `curios-*.jar` | 提供饰品栏 API，替代 1.12.2 的 Baubles。 |
| `TerraBlender-*.jar` | Thaumaturge 的硬依赖。 |

`build.gradle` 用 `implementation fileTree(dir: 'libs', include: ['*.jar'])` 一次性引入。

## 这些 jar 从哪来

构建/运行实例：`E:\Modpacks\versions\Thaumcraft 1.21.1`（1.21.1 / NeoForge 21.1.248）。

- **Thaumaturge** — <https://github.com/Leclowndu93150/Thaumaturge/> 的 0.4.4 发布版。
- **Curios** — <https://modrinth.com/mod/curios> 9.5.1+1.21.1。
- **TerraBlender** — <https://modrinth.com/mod/terrablender> 4.1.0.8。

jar 不入库（见 `.gitignore`），克隆后按上表放入本目录即可。
