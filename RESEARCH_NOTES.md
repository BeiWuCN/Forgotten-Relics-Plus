# 研究笔记难度

所有 35 个合成研究都需要先完成并学习一份研究笔记，再推进至第二页查看合成方式。原有前置研究、观测要素、获得/制作物品要求仍然生效。两项基础理论不额外增加合成笔记；休眠核心共享浑浊之核的研究，因其不是独立合成物品。欧米伽之核的独立研究和配方仅在安装 Re:Avaritia（模组 ID：avaritia）时加载。

本模组支持复杂度 2～8。网格半径维持在上游界面支持的范围内，难度通过主题锚点数量和空洞数量递增；生成器保持所有剩余格子连通，并保留每个锚点至少两个邻格。其他上游研究继续使用原有复杂度范围与生成器。

| 遗物研究 | 复杂度 | 锚点要素 |
| --- | ---: | --- |
| 挖掘之魅 | 2 | terra、instrumentum、potentia |
| 不和谐之戒 | 2 | permutatio、potentia、praecantatio |
| 符文天象石 | 2 | aer、aqua、potentia |
| 神圣护身符 | 3 | victus、praemunio、praecantatio |
| 永恒智慧之书 | 3 | cognitio、sensus、praecantatio |
| 人造地精法杖 | 3 | praecantatio、desiderium、humanus |
| 日耀石 | 3 | lux、victus、potentia |
| 七阳之戒 | 4 | tenebrae、praemunio、spiritus、praecantatio |
| 空间魔镜 | 4 | permutatio、vacuos、vitreus、praecantatio |
| 叠加之戒 | 4 | permutatio、vinculum、spiritus、praecantatio |
| 霹雳咒书 | 4 | potentia、aer、aversio、praecantatio |
| 命运巨著 | 4 | motus、vinculum、potentia、praecantatio |
| 挖掘之魅-空灵 | 5 | instrumentum、metallum、potentia、praecantatio |
| 混沌之核 | 5 | perditio、permutatio、vitium、potentia |
| 遗忘护身符 | 5 | vacuos、praemunio、perditio、praecantatio |
| 千魂号令之典 | 5 | spiritus、mortuus、exanimis、praecantatio |
| 恐惧之冠 | 5 | sensus、tenebrae、aversio、praecantatio |
| 错位之典 | 5 | permutatio、motus、vacuos、praecantatio |
| 远古之庇护 | 6 | praemunio、spiritus、alienis、praecantatio、metallum |
| 浑浊之核 | 6 | auram、praecantatio、permutatio、vacuos、vitreus |
| 血腥咒书 | 6 | aversio、ignis、spiritus、praecantatio、vitium |
| 炼狱放逐咒书 | 6 | aversio、potentia、ignis、motus、praecantatio |
| 邪术咒书 | 6 | alienis、tenebrae、vacuos、praecantatio、aversio |
| 食尸鬼之颅 | 6 | mortuus、spiritus、aversio、tenebrae、praecantatio |
| 元始混沌之书 | 6 | perditio、permutatio、potentia、vitium、praecantatio |
| 虚空吞噬者之书 | 7 | vacuos、alienis、desiderium、perditio、praecantatio |
| 虚伪审判 | 7 | aversio、praemunio、permutatio、spiritus、alienis |
| 原子之怒 | 7 | potentia、ignis、perditio、aversio、praecantatio |
| 湮灭之钥 | 7 | vacuos、perditio、tenebrae、permutatio、praecantatio |
| 悖论之刃 | 7 | aversio、permutatio、metallum、praecantatio、alienis |
| 月耀咒书 | 7 | lux、aer、aversio、praecantatio、alienis |
| 深渊之魔书 | 7 | vacuos、tenebrae、alienis、motus、praecantatio |
| 王之宝典 | 8 | praecantatio、aversio、lux、cognitio、potentia、alienis |
| 欧米伽之核 | 8 | auram、praecantatio、alienis、potentia、spiritus、permutatio |
| 破碎的命运巨著 | 8 | permutatio、victus、mortuus、spiritus、alienis、praecantatio |

`runData` 会校验全部 35 个研究的笔记门槛，每份笔记测试 32 个种子，并验证复杂度没有被上游截断为 3。`build` 会检查所有灌注配方都绑定对应研究。
