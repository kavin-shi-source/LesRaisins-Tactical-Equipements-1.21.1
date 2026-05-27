# LesRaisins Tactical Equipements 1.21.1 移植质量评估报告

> **评估日期**: 2026-05-27
> **评估方法**: 全量静态代码分析（未构建运行）
> **评估版本**: 原版 0.4.1（Forge 1.20.1）→ 移植版 0.3.0（NeoForge 1.21.1）

---

## 目录

1. [概述](#1-概述)
2. [测试方法](#2-测试方法)
3. [功能完整性分析](#3-功能完整性分析)
4. [代码质量分析](#4-代码质量分析)
5. [兼容性分析](#5-兼容性分析)
6. [性能分析](#6-性能分析)
7. [问题与 BUG 清单](#7-问题与-bug-清单)
8. [移植质量评分表](#8-移植质量评分表)
9. [改进建议](#9-改进建议)
10. [文件差异汇总](#10-文件差异汇总)

---

## 1. 概述

### 1.1 项目简介

**LesRaisins Tactical Equipements**（简称 LR Tactical）是一个 Minecraft 模组，为游戏添加了战术装备，包括投掷物（手雷、闪光弹、烟雾弹等）、近战武器、闪光盾和消耗品等。原版模组基于 Forge 1.20.1 开发，本次评估的移植版本将其迁移至 NeoForge 1.21.1。

### 1.2 版本对比

| 项目 | 原版 | 移植版 |
|------|------|--------|
| **目标版本** | Minecraft 1.20.1 Forge | Minecraft 1.21.1 NeoForge |
| **模组版本** | 0.4.1 | 0.3.0 |
| **Java 版本** | Java 17 | Java 21 |
| **Gradle** | 8.1.1（Groovy DSL） | 8.8（Kotlin DSL） |
| **Mixin 数量** | 8 个 | 6 个 |
| **前置依赖** | TaCZ 1.1.5+ | TaCZ 1.0.4+ |
| **Java 源文件** | 115 个 | 93 个（减少 22 个） |
| **包路径** | `me.xjqsh.lrtactical` | `me.xjqsh.lrtactical`（不变） |

### 1.3 总体评价

**总分：6.5 / 10 ★★★★☆☆☆☆☆☆**

移植工作完成了**核心武器功能**（投掷物、近战武器、闪光盾）的迁移，技术基础架构（网络、能力系统、GUI）适配基本正确。但**消耗品子系统完全缺失**、**近战同步逻辑被大幅简化**、**Player Animator 兼容层被移除**，导致功能完整性严重不足。

> 移植版 README 中自述「an early port in progress」，本次评估也证实了这一状态。

---

## 2. 测试方法

本次评估采用**全量静态代码分析（Static Code Analysis）**，具体方法如下：

| 方法 | 说明 |
|------|------|
| **结构对比** | 递归对比两个仓库的全部目录和文件清单（115 → 93 Java 文件 + ~340 → ~310 资源文件） |
| **逐文件差异分析** | 对比核心配置（`mods.toml`）、主入口类、网络处理器、物品/方块注册类、能力（Capability → Attachment）系统、Mixin 配置等关键文件 |
| **API 映射验证** | 验证 Forge → NeoForge 的 API 迁移是否正确（Capability → Attachment、SimpleChannel → Payload、注册系统等） |
| **资源文件完整性检查** | 对比 textures / models / animations 目录、sounds.json、语言文件 |
| **Lua 脚本还原度对比** | 对比 Lua 状态机的状态数量和逻辑完整性 |
| **配方/数据文件检查** | 检查 JSON 配方和数据驱动文件的存在性 |

### 未执行的操作

- 未进行实际构建和游戏内测试
- 未进行性能基准测试
- 未在多人联机环境下测试
- 未检查 TaCZ API 兼容性（需实际运行时验证）

---

## 3. 功能完整性分析

### 3.1 已成功移植的核心功能

| 功能 | 状态 | 说明 |
|------|------|------|
| **投掷物系统** | ✅ 完整移植 | 5 种投掷物（EXPLODE / STICKY / SMOKE / STUN / EFFECT_CLOUD）全部迁移 |
| **近战武器系统** | ⚠️ 部分移植 | 类型注册完成，但攻击同步逻辑大幅简化 |
| **闪光盾（Flash Shield）** | ✅ 完整移植 | 渲染器和模型文件均保留 |
| **背刺附魔** | ✅ 已移植 | 改为数据驱动方式（`backstab.json`） |
| **自定义工作台 GUI** | ✅ 已移植 | GunSmithTableScreenMixin 保留 |
| **致盲效果叠加层** | ✅ 改进移植 | 从 GameRendererMixin 重构为独立的 BlindnessOverlay 类 |
| **抗爆性附魔** | ✅ 已移植 | 数据驱动方式实现 |
| **C4 炸弹** | ✅ 补全 | 从原版的 [WIP] 变为可用状态，有完整纹理和模型 |
| **配置系统** | ✅ 已移植 | NeoForge 配置 API 适配正确 |
| **自定义爆炸逻辑** | ✅ 已迁移 | CustomExplosion 适配 NeoForge API |

### 3.2 缺失的核心功能

#### 3.2.1 消耗品子系统（严重程度：🔴 必须修复）

消耗品子系统的代码和资源被**完全移除**，这是原模组中最显著的功能缺失。

**移除的文件统计：**

| 类别 | 数量 | 说明 |
|------|------|------|
| Java 源文件 | ~24 个 | ConsumableItem.java、ConsumableInputHandler.java、ConsumableAnimationStateContext.java、IConsumable.java 等 |
| 纹理（PNG） | 6 个 | blood_pack_uv.png、condensed_milk_uv.png、ibuprofen_uv.png 及对应 slot 图标 |
| 几何模型（JSON） | 3 个 | blood_pack_geo.json、condensed_milk_geo.json、ibuprofen_geo.json |
| 动画（JSON） | 3 个 | blood_pack.animation.json、condensed_milk.animation.json、ibuprofen.animation.json |
| 显示配置（JSON） | 3 个 | blood_pack.json、condensed_milk.json、ibuprofen.json |
| 索引 JSON | 3 个 | 各消耗品的索引文件 |
| 配方（JSON） | 3 个 | 各消耗品的合成配方 |
| 音效（OGG） | ~8 个 | 消耗品相关音效 |
| Lua 状态机 | 1 个 | consumable_state_machine.lua（113 行） |
| **合计** | **~45+ 个文件** | — |

**影响：** 原版 0.4.1 版本包含的止血包（blood_pack）、炼乳（condensed_milk）、布洛芬（ibuprofen）三个消耗品完全无法使用。

#### 3.2.2 语言文件缺失翻译

**缺失条目（8 条）：**

```diff
- item.lrtactical.blood_pack=止血包
- item.lrtactical.condensed_milk=炼乳
- item.lrtactical.ibuprofen=布洛芬
- tooltip.lrtactical.blood_pack=右键使用以恢复生命值
- tooltip.lrtactical.condensed_milk=右键使用以获得效果
- tooltip.lrtactical.ibuprofen=右键使用以消除负面效果
- itemGroup.lrtactical.consumable=LR Tactical | 消耗品
- subtitle.lrtactical.consumable.use=使用消耗品
```

#### 3.2.3 Player Animator 兼容层

**移除内容（7 个文件）：**

| 文件路径 | 说明 |
|----------|------|
| `compat/player_animator/LRPlayerAnimator.java` | Player Animator 集成主类 |
| `compat/player_animator/PlayerAnimatorMod.java` | 模组入口 |
| `compat/player_animator/PlayerAnimatorAssetManagerMixin.java` | 资源管理 Mixin |
| `compat/player_animator/animation/` 目录下 3 个文件 | 动画定义 |
| `resources/player_animator/melee/baseball_bat.json` | 动画资源文件 |

**影响：** 如果用户同时安装了 Player Animator 模组，将无法获得第一人称近战武器的自定义动画效果，但不至于导致崩溃。

### 3.3 被简化的功能

#### 3.3.1 近战动画同步（严重程度：🟡 建议修复）

移植版对近战武器的网络同步逻辑进行了大幅简化：

| 方面 | 原版 | 移植版 |
|------|------|--------|
| **同步消息** | 3 个（SMeleeAnimationSync + SResetMeleeSyncMessage + CCancelToggleConsumableUse） | 1 个（简化合并） |
| **CombatProperties 字段** | `actionCounts` Map + `preparingAttackCnt` + `resetMeleeSync()` / `forceResetMeleeSync()` | 全部移除 |
| **攻击方法签名** | `attack(xxx, yyy, cnt)` 5 参数 | `attack(xxx, yyy)` 4 参数，无 cnt 参数 |
| **DelayAttack 类** | 含 `actionCount` 字段 | 不含 `actionCount` 字段 |

**潜在影响：** 多人游戏中，近战武器的连击计数和攻击阶段可能在客户端和服务端之间不同步，可能导致：

1. 攻击动画在不同玩家视角下显示不一致
2. 连击伤害计算可能出现偏差
3. 武器切换或收起后攻击状态未能正确重置

#### 3.3.2 Lua 状态机简化

| 项目 | 原版 | 移植版 |
|------|------|--------|
| `default_grenade_state_machine.lua` | 196 行 / 7 个状态 | 113 行 / 4 个状态 |
| 移除的状态 | — | `using_hold`（蓄力保持）、`after_use`（使用后）、`inspect`（检视） |
| `consumable_state_machine.lua` | 113 行 | ❌ 完全移除 |
| 命名风格 | 全小写 + 下划线（snake_case） | 部分使用驼峰命名（如 `runPutAwayAnimation`） |

**影响：** 投掷物的投掷前蓄力动画和投掷后检查动画被移除。Lua 脚本的命名风格与 Lua 社区惯例不一致。

---

## 4. 代码质量分析

### 4.1 做得好的方面

| 方面 | 说明 |
|------|------|
| **能力系统迁移** | Forge Capability → NeoForge AttachmentType 转换正确，`ModCapabilities.java` 定义清晰 |
| **网络系统迁移** | SimpleChannel → Payload 注册模式使用正确，区分 `playToServer` / `playToClient` |
| **注册系统迁移** | `RegistryObject<T>` → `DeferredHolder<T,R>` + `BuiltInRegistries` 适配正确 |
| **附魔系统** | 正确使用 1.21 数据驱动方式（`ResourceKey<Enchantment>` + JSON），紧跟上游变化 |
| **致盲效果重构** | 从 GameRendererMixin 解耦为独立的 `BlindnessOverlay` 类，架构更优 |
| **C4 补全** | 将原版标记为 [WIP] 的 C4 功能补全，增加了实际的游戏内价值 |
| **Mixin 精简** | 从 8 个减少到 6 个，减少了对游戏引擎的侵入式修改 |
| **构建系统升级** | Kotlin DSL + 版本目录（`libs.versions.toml`），构建配置更整洁和可维护 |

### 4.2 存在的问题

| 问题 | 严重程度 | 说明 |
|------|---------|------|
| **消耗品子系统未注释完整** | 🔴 高 | `ModItems.java` 中 `CONSUMABLE_TAB` 和 `CONSUMABLE` 被注释掉但仍有相关实现残留，代码不一致 |
| **附魔效果与实际不符** | 🔴 高 | `backstab.json` 中 `per_level_above_first: 0.0`，实际效果仅通过事件处理器中的硬编码 `level * 0.25f` 实现。数据驱动和硬编码并存造成维护困惑 |
| **注册代码被注释** | 🟡 中 | `EquipmentMod.java` 中 `// ModEnchantment.ENCHANTMENTS.register(modEventBus);` 被注释，虽然功能通过数据驱动方式实现，但代码残留造成理解困难 |
| **事件忙探测用** | 🟡 中 | `CriticalHitEventHandler.java` 中 `// event.setResult(Event.Result.ALLOW);` 被注释，作为调试残留不应出现在生产代码中 |
| **Lua 命名不一致** | 🟢 低 | 部分函数名使用驼峰命名而非 Lua 社区的 snake_case 惯例 |
| **getRandomInt 实现差异** | 🟢 低 | 使用 `RandomAccessWithLock` + `ThreadLocalRandom.current().nextInt()` 替代原版的 `RandomSource.nextInt()`，线程安全但每次调用创建新的 Random 实例，低开销场景无影响 |

---

## 5. 兼容性分析

### 5.1 构建兼容性（✅ 良好）

| 项目 | 状态 | 说明 |
|------|------|------|
| **构建工具** | ✅ 兼容 | Gradle 8.8 + NeoGradle ModDev 2.0.74，与 NeoForge 1.21.1 生态兼容 |
| **Java 版本** | ✅ 兼容 | Java 21，符合 Minecraft 1.21+ 的要求 |
| **Parchment 映射** | ✅ 已升级 | 2024.08.08，适配 1.21.1 |

### 5.2 潜在兼容性风险

| 风险项 | 严重程度 | 说明 |
|--------|---------|------|
| **TaCZ 版本降级** | 🟡 中 | 前置 TaCZ 从 1.1.5+ 降至 1.0.4+，可能丢失 TaCZ 1.0.4 到 1.1.5 之间的功能和 API 变更 |
| **Player Animator 无兼容层** | 🟢 低 | 如果未安装 Player Animator 则无任何影响；如果安装了，也仅影响近战武器第一人称动画，不会导致崩溃 |
| **消耗品代码残留** | 🟢 低 | 仅编译警告级别，不会导致运行时崩溃 |
| **缺少 accesstransformer** | 🟢 低 | 移植版用 Mixin ItemAccessor 替代了原版的 `accesstransformer.cfg`，是合理的替代方案 |

---

## 6. 性能分析

基于静态代码分析的性能评估：

| 方面 | 评估 | 说明 |
|------|------|------|
| **投掷物实体** | ✅ 无退化 | 实体数量和处理逻辑未明显变化 |
| **GUI 渲染** | ✅ 无退化 | 自定义工作台 GUI 渲染方式一致 |
| **致盲叠加层** | ✅ 略有优化 | 从 Mixin 注入改为独立事件监听，减少了对渲染管线的侵入 |
| **Random 使用** | 🟢 可忽略 | `getRandomInt` 使用 `ThreadLocalRandom`，非频繁调用场景下无性能影响 |
| **Lua 脚本** | ✅ 简化 | 状态减少（7 → 4）意味着更少的 Lua VM 调用，实际可能略有提升 |

**结论：** 性能方面无明显退化。致盲效果的重构和 Lua 状态机的简化可能带来微弱的性能改善。

---

## 7. 问题与 BUG 清单

### 7.1 🔴 必须修复（3 个）

| ID | 问题 | 说明 | 修复建议 |
|----|------|------|---------|
| **CRIT-1** | **消耗品子系统缺失** | 所有消耗品（止血包、炼乳、布洛芬）的代码、资源、配方、音效被完全移除，但 `ModItems.java` 中有相关注释残留，物品栏配置中也缺失了消耗品标签页 | 决定方向：要么完整移植消耗品子系统（约 45 个文件），要么在代码中明确注释标记为「已移除」，清理所有残留引用 |
| **CRIT-2** | **近战同步逻辑简化** | `CombatProperties` 中的 `actionCounts`、`preparingAttackCnt`、`resetMeleeSync()` 被移除，多人游戏下近战连击计数器可能在客户端和服务端不同步 | 至少应恢复连击计数器和同步标志位的字段定义，确保 `livingEntity#deathTime` hack 仍然有效 |
| **CRIT-3** | **背刺附魔效果与实际不符** | `backstab.json` 数据驱动部分声明 `per_level_above_first: 0.0`，实际效果由 `CriticalHitEventHandler` 中的硬编码 `level * 0.25f` 处理。数据文件与实现逻辑不一致 | 要么在 JSON 中设置正确的数值，要么删除 JSON 中的 `effects` 字段并添加注释说明完全由代码处理 |

### 7.2 🟡 建议修复（6 个）

| ID | 问题 | 严重程度 |
|----|------|---------|
| **REC-1** | `EquipmentMod.java` 中 `// ModEnchantment.ENCHANTMENTS.register(modEventBus);` 被注释为死代码，应清理 | 低 |
| **REC-2** | `CriticalHitEventHandler.java` 中 `// event.setResult(Event.Result.ALLOW);` 被注释，应清理 | 低 |
| **REC-3** | Player Animator 兼容层缺失，需要决定是否移植 | 中 |
| **REC-4** | Lua 函数命名不一致（snake_case vs camelCase），建议统一 | 低 |
| **REC-5** | TaCZ 前置版本从 1.1.5+ 降至 1.0.4+，建议验证是否为有意降级 | 中 |
| **REC-6** | 语言文件中 8 条消耗品相关翻译完全缺失 | 低 |

### 7.3 🟢 仅供参考（3 个）

| ID | 说明 |
|----|------|
| **INFO-1** | C4 标记从 [WIP] 改为完整实现，这一改动是正向的 |
| **INFO-2** | Lua 状态机从 7 状态减少到 4 状态，如果是移植者有意简化则接受，否则建议还原 |
| **INFO-3** | `getRandomInt` 使用 `ThreadLocalRandom`，虽然 Java 标准库已够用但若追求与原版一致可使用 `RandomSource` |

---

## 8. 移植质量评分表

| 评估维度 | 权重 | 评分（/10） | 加权得分 | 说明 |
|---------|------|:----------:|:--------:|------|
| **功能完整性** | 35% | 5 | 1.75 | 核心武器功能完整，但消耗品子系统完全缺失（-3 分），近战同步简化（-1 分），Player Animator 缺失（-1 分） |
| **代码质量** | 25% | 7 | 1.75 | API 迁移正确，但存在注释残留和死代码，Lua 命名不一致 |
| **兼容性** | 15% | 8 | 1.20 | 构建系统适配良好，数据驱动附魔符合 1.21 标准 |
| **性能** | 10% | 9 | 0.90 | 无退化，部分重构还有微优化 |
| **架构改进** | 15% | 6 | 0.90 | 致盲效果重构、Mixin 精简是加分项；但消耗品完全移除是减分项 |
| **总分** | **100%** | — | **6.50 / 10** | — |

---

## 9. 改进建议

### 9.1 🔴 短期（1-2 周）

1. **优先级最高：** 决定消耗品子系统的未来方向 — 要么完整移植（约 45 个文件），要么正式标记为「已移除」并清理所有残留代码和资源引用
2. **高优先级：** 恢复近战同步逻辑中的 `actionCounts` 和 `resetMeleeSync()`，确保多人联机中近战武器行为一致
3. **高优先级：** 修正 `backstab.json` 使数据驱动部分与代码实现一致

### 9.2 🟡 中期（2-4 周）

4. 评估是否移植 Player Animator 兼容层
5. 清理所有注释掉的死代码（共 5 处）
6. 统一 Lua 函数命名风格
7. 验证 TaCZ 1.0.4+ 的 API 兼容性，必要时更新到最新版

### 9.3 🟢 长期（1-3 个月）

8. 为消耗品子系统编写单元测试
9. 建立 CI/CD 管道（GitHub Actions）进行自动构建验证
10. 进行多人联机的完整功能测试

---

## 10. 文件差异汇总

### 10.1 被移除的文件

| 文件/目录 | 数量 | 说明 |
|-----------|:----:|------|
| `ConsumableItem.java` + 关联类 | ~24 个 | 消耗品核心逻辑 Java 文件 |
| `consumable_state_machine.lua` | 1 个 | 113 行 Lua 状态机 |
| `player_animator/` 目录 | 6 个 Java + 1 个 JSON | Player Animator 兼容层 |
| 消耗品资源目录 | ~15 个 | 模型、纹理、动画文件 |
| 消耗品音效 OGG | ~8 个 | 消耗品相关音效 |
| 消耗品配方 JSON | 3 个 | 合成配方 |
| 消耗品翻译条目 | 8 条 | 语言文件条目 |

### 10.2 被新增的文件

| 文件 | 说明 |
|------|------|
| `ModCapabilities.java` | AttachmentType 定义（替代原版 Capability） |
| `ClientModEvents.java` | MOD 总线事件，注册自定义物品渲染器 |
| `BlindnessOverlay.java` | 致盲效果独立类（重构自 GameRendererMixin） |
| `ItemAccessor.java` | Mixin Accessor（替代 accesstransformer.cfg） |
| `backstab.json` | 数据驱动附魔定义 |
| `enchantment/` 目录 | 多个数据驱动附魔 JSON 文件 |
| C4 相关资源 | 完整实现的 C4 纹理、模型和显示配置 |

### 10.3 被修改的关键文件

| 文件 | 变更说明 |
|------|---------|
| `EquipmentMod.java` | 带参构造器，使用参数注入的 modEventBus 和 ModContainer；未调用 `NetworkHandler.init()` |
| `NetworkHandler.java` | SimpleChannel → Payload 注册模式；消息从 12 个减少到 9 个 |
| `ModItems.java` | `RegistryObject<Item>` → `DeferredHolder<Item,T>`；移除消耗品相关方法和字段 |
| `CombatProperties.java` | 移除 `actionCounts`、`preparingAttackCnt`、同步方法 |
| `CustomExplosion.java` | 适配 NeoForge API（`igniteForSeconds()`、`ignoreExplosion(this)` 等） |
| `build.gradle.kts` | Groovy DSL → Kotlin DSL；ForgeGradle → NeoGradle ModDev |
| `mods.toml` | 适配 NeoForge 格式；更新 loader 版本 |

---

## 附录 A：参考链接

- **原版仓库**: [LesRaisins-Studios/LesRaisins-Tactical-Equipements](https://github.com/LesRaisins-Studios/LesRaisins-Tactical-Equipements)
- **移植版仓库**: [Nahiyus512/LesRaisins-Tactical-Equipements-1.21.1](https://github.com/Nahiyus512/LesRaisins-Tactical-Equipements-1.21.1.git)
- **NeoForge 官方文档**: [https://docs.neoforged.net/](https://docs.neoforged.net/)
- **本文档路径**: `e:\java-xuexi\downandfix\Leaky\LR-Tactical-Equipements-1.21.1-移植质量评估报告.md`

---

*报告编写完成。如需针对特定模块的深入分析、代码级修复方案或逐步执行的移植实施计划，请告知。*
