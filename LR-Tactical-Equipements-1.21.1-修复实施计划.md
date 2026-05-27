# LesRaisins Tactical Equipements 1.21.1 修复实施计划

> **基于**: 移植质量评估报告 (6.5/10)
> **目标**: 解决所有 3 个关键(CRIT)问题 + 6 个建议(REC)问题 + 3 个参考(INFO)问题
> **优先级**: 🔴 短期(1-2周) → 🟡 中期(2-4周) → 🟢 长期(1-3月)

---

## 阶段一：🔴 短期修复（1-2周）

### 任务 1.1：消耗品子系统 - 决策与清理（CRIT-1）

**现状分析：**
- 原版 0.4.1 中 `item/consumable/` 目录包含完整的消耗品子系统
- 当前移植版：`consumable` 相关 Java 文件 (~24个) + 资源文件 (~21个) 被完全移除
- `ModItems.java` 中已无任何 `CONSUMABLE` 相关注册代码
- 语言文件缺少 8 条消耗品相关翻译

**选择方案 A：完整移植（推荐）**

工作量估算：
| 类别 | 文件数 | 来源 |
|------|:------:|------|
| Java 源文件 | ~24 | 原版 `item/consumable/`, `item/index/`, `init/`, `network/`, `handler/`, `client/` |
| Lua 状态机 | 1 | `consumable_state_machine.lua` (113 行) |
| 纹理/模型/动画 | ~12 | `textures/`, `models/`, `animations/` |
| 显示配置 JSON | 3 | `display/` |
| 音效 OGG | ~8 | `sounds/` |
| 配方 JSON | 3 | `recipes/` |
| 语言条目 | 8 | `lang/` |

**移植注意事项（NeoForge 1.21.1 适配）：**
1. Capability → AttachmentType：消耗品的 `IConsumable` 能力接口需用 `AttachmentType` 重写
2. SimpleChannel → Payload：消耗品网络消息（`CCancelToggleConsumableUse` 等）需迁移为 Payload 模式
3. `ConsumableItem.java` — 使用 `Item` 直接继承模式（如同 `ThrowableItem`）
4. 注册模式：`RegistryObject` → `DeferredHolder` + `BuiltInRegistries`
5. 消耗品输入处理器（`ConsumableInputHandler`）需适配 NeoForge 按键绑定 API

**具体文件清单（从原版移植）：**

**Java 源文件（按优先级）：**
1. `item/ConsumableItem.java` — 消耗品物品基类
2. `item/consumable/` 目录下所有文件 — 各消耗品实现
3. `item/index/ConsumableIndex.java` — 消耗品索引（参考 `ThrowableIndex` 模式）
4. `api/item/IConsumable.java` — 消耗品接口（参考 `IThrowable` 模式）
5. `api/animation/ConsumableAnimationStateContext.java` — 动画上下文
6. `init/ModConsumables.java` — 消耗品注册（按 `ModCustomTypes` 模式）
7. `network/message/CCancelToggleConsumableUse.java` — 网络消息
8. `handler/ConsumableHandler.java` — 事件处理
9. `client/input/ConsumableInputHandler.java` — 输入处理
10. `client/renderer/item/ConsumableItemRenderer.java` — 渲染器

**资源文件：**
- `textures/item/blood_pack_uv.png`, `condensed_milk_uv.png`, `ibuprofen_uv.png`
- `models/entity/blood_pack_geo.json`, `condensed_milk_geo.json`, `ibuprofen_geo.json`
- `animations/entity/blood_pack.animation.json`, `condensed_milk.animation.json`, `ibuprofen.animation.json`
- `display/` 下各消耗品显示配置
- `scripts/consumable_state_machine.lua`
- `sounds/` 下消耗品相关音效
- `recipes/` 下合成配方
- `lang/zh_cn.json` 和 `lang/en_us.json` 语言条目

**选择方案 B：正式标记移除（备选）**
- 在文档和 `ModItems.java` 中添加明确注释说明「消耗品已计划在未来版本中恢复」
- 清理残留引用，确保一致状态

---

### 任务 1.2：近战同步逻辑修复（CRIT-2）

**现状分析：**

当前 `CombatProperties.java` 缺失以下字段和方法：
| 缺失项 | 原版用途 | 当前状态 |
|--------|---------|:--------:|
| `actionCounts` Map | 记录每种攻击动作的连击计数 | ❌ 移除 |
| `preparingAttackCnt` | 攻击准备阶段的 tick 计数器 | ❌ 移除 |
| `resetMeleeSync()` | 武器切换时重置同步状态 | ❌ 移除 |
| `forceResetMeleeSync()` | 强制重置同步（网络包触发） | ❌ 移除 |
| `DelayAttack.actionCount` | 延迟攻击携带的连击数 | ❌ 移除 |

**修复步骤：**

**步骤 1：还原 `CombatProperties.java` 中的缺失字段**

```java
// 在 CombatProperties.java 类中添加：
private final Map<String, Integer> actionCounts = new HashMap<>();
private int preparingAttackCnt = 0;

// getter/setter
public int getActionCount(MeleeAction action) { ... }
public void setActionCount(MeleeAction action, int count) { ... }
public int getPreparingAttackCnt() { return preparingAttackCnt; }
public void setPreparingAttackCnt(int cnt) { this.preparingAttackCnt = cnt; }

// 同步方法
public void resetMeleeSync() { ... }
public void forceResetMeleeSync() { ... }
```

**步骤 2：还原 `resetMeleeSync()` 逻辑**
- 在 `CombatProperties.reset()` 方法末尾调用 `resetMeleeSync()`
- 确保武器切换（`lastSelected` 变化或 `lastItem` 变化）时重置连击计数

**步骤 3：为 `DelayAttack` 添加 `actionCount` 字段**
```java
public static class DelayAttack extends DelayTask {
    private final ItemStack stack;
    private final MeleeAction action;
    private final int actionCount;  // 新增

    DelayAttack(int delay, ItemStack stack, MeleeAction action, int actionCount) {
        super(delay);
        this.action = action;
        this.stack = stack;
        this.actionCount = actionCount;  // 新增
    }
    // ...
}
```

**步骤 4：修复 `CMeleeAttackRequest` 网络消息**
- 在 `CMeleeAttackRequest.java` 中添加 `actionCount` 字段
- 编码/解码方法中处理该字段
- 服务端 `handle()` 中恢复 `CombatProperties` 的 `actionCounts`

**步骤 5：更新 `postAttack()` 方法签名**
- 从 `postAttack(MeleeAction action, List<Entity> entities)`
- 改为 `postAttack(MeleeAction action, List<Entity> entities, int actionCount)`
- 在方法体中用 `actionCount` 更新 `actionCounts` Map

---

### 任务 1.3：背刺附魔数据与代码统一（CRIT-3）

**现状分析：**
- `backstab.json` 中 `per_level_above_first: 0.0`，数据驱动声明无加成
- `CriticalHitEventHandler.java` 中硬编码 `level * 0.25f`
- 第 39 行 `// event.setResult(Event.Result.ALLOW);` 被注释

**修复方案（推荐：清理并统一到代码端）：**

**步骤 1：清理 `CriticalHitEventHandler.java`**
```java
// 移除第 39 行注释
// 保留硬编码逻辑，清除调试残留
```

**步骤 2：在 `backstab.json` 中添加注释性说明**
- 由于 JSON 不支持注释，在 `backstab.json` 中给 `effects` 字段设置占位值
- 或者完全移除 `effects` 字段（根据 MC 1.21 附魔数据格式验证可行性）

**步骤 3：验证 JSON 格式兼容性**
- 检查移除 `effects` 后是否仍能被游戏正确加载
- 如果不能，则保留 `effects` 但确保 `per_level_above_first: 0.0` 与代码行为一致

**推荐方案**：保持 `effects` 不变（作为数据驱动的扩展点），但在 `CriticalHitEventHandler.java` 中添加注释：
```java
// 注意：backstab.json 中 effects.damage 设为 0.0，
// 实际伤害加成由此事件处理器通过 level * 0.25f 实现，
// 因为背刺伤害需要方向判定（背后攻击），无法由数据驱动单独完成。
```

---

## 阶段二：🟡 中期修复（2-4周）

### 任务 2.1：清理死代码（REC-1, REC-2）

**文件 1：`EquipmentMod.java`（第 30 行）**
```java
// ModEnchantment.ENCHANTMENTS.register(modEventBus);
```
→ **操作**：删除此行注释

**文件 2：`CriticalHitEventHandler.java`（第 39 行）**
```java
// event.setResult(Event.Result.ALLOW);
```
→ **操作**：删除此行注释

**文件 3：`MeleeItem.java`**
```java
// @Override
// public Multimap<Attribute, AttributeModifier> getAttributeModifiers(...) { ... }
```
```java
// @Override
// public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) { ... }
```
```java
// public boolean canPerformAction(ItemStack stack, ToolAction toolAction) { ... }
```
→ **操作**：删除 3 处注释代码块

**文件 4：`CriticalHitEventHandler.java` 第 27-28 行**
```java
@SuppressWarnings("deprecation")
int level = EnchantmentHelper.getItemEnchantmentLevel(...)
```
→ **操作**：检查 `getItemEnchantmentLevel` 的废弃替代方案，尝试替换为最新 API

### 任务 2.2：Player Animator 兼容层评估（REC-3）

**现状：**
- 原版包含 `compat/player_animator/` 目录（~7 个文件）
- 移植版将其完全移除

**评估标准：**
1. 检查用户社区中 Player Animator 模组的安装率
2. 评估第一人称近战动画缺失的视觉影响程度
3. 确认 NeoForge 1.21.1 的 Player Animator API 变化

**备选方案 A：完整移植（推荐）**
- 从原版恢复所有 7 个文件
- 适配 NeoForge 1.21.1 API
- 注册 CompatRegistry 中

**备选方案 B：标记为已知限制**
- 在 README 和 JEI 兼容页面标注

### 任务 2.3：Lua 命名统一（REC-4）

**文件：`default_grenade_state_machine.lua`（第 19 行）**
```lua
local function runPutAwayAnimation(context)
```
→ **操作**：改为 snake_case 命名 `run_put_away_animation`

**检查其他 Lua 文件是否也有类似问题：**
- `c4_state_machine.lua`
- `default_melee_state_machine.lua`
- `flash_shield_state_machine.lua`

### 任务 2.4：TaCZ API 版本验证（REC-5）

**操作步骤：**
1. 查阅 TaCZ 1.0.4 → 1.1.5 的 Changelog
2. 检查项目中使用的 TaCZ API 是否涉及已变更的接口
3. 在 `gradle.properties` 中更新依赖版本声明
4. 在文档中说明版本降级原因

### 任务 2.5：补充语言文件（REC-6）

**需添加的 8 条翻译：**

**`zh_cn.json`：**
```json
"item.lrtactical.blood_pack": "止血包",
"item.lrtactical.condensed_milk": "炼乳",
"item.lrtactical.ibuprofen": "布洛芬",
"tooltip.lrtactical.blood_pack": "右键使用以恢复生命值",
"tooltip.lrtactical.condensed_milk": "右键使用以获得效果",
"tooltip.lrtactical.ibuprofen": "右键使用以消除负面效果",
"itemGroup.lrtactical.consumable": "LR Tactical | 消耗品",
"subtitle.lrtactical.consumable.use": "使用消耗品"
```

**`en_us.json`：**
```json
"item.lrtactical.blood_pack": "Blood Pack",
"item.lrtactical.condensed_milk": "Condensed Milk",
"item.lrtactical.ibuprofen": "Ibuprofen",
"tooltip.lrtactical.blood_pack": "Use [RMB] to heal",
"tooltip.lrtactical.condensed_milk": "Use [RMB] to get effects",
"tooltip.lrtactical.ibuprofen": "Use [RMB] to remove negative effects",
"itemGroup.lrtactical.consumable": "LR Tactical | Consumable",
"subtitle.lrtactical.consumable.use": "Consumable Used"
```

> ⚠️ 注意：如果选择方案 B（标记移除）而非完整移植消耗品，则 REC-6 可跳过。

---

## 阶段三：🟢 长期改进（1-3个月）

### 任务 3.1：消耗品子系统单元测试

- 为 `ConsumableItem` 编写基本功能测试
- 测试消耗品使用、效果应用、耐久消耗逻辑

### 任务 3.2：CI/CD 管道建设

- 使用 GitHub Actions 配置自动构建
- 每次 PR 自动运行 Gradle 构建验证
- 缓存 Gradle 依赖以加速构建

### 任务 3.3：多人联机完整测试

- 测试近战武器连击同步
- 测试投掷物在不同网络延迟下的行为
- 测试消耗品使用同步（如果恢复）

---

## 附录 A：代码差异参考

### 当前问题代码位置总表

| 文件路径 | 行号 | 问题类型 | 严重程度 |
|---------|:----:|---------|:--------:|
| `src/main/java/.../init/ModItems.java` | — | 消耗品完全缺失 | 🔴 CRIT-1 |
| `src/main/java/.../capability/CombatProperties.java` | — | 近战同步简化 | 🔴 CRIT-2 |
| `src/main/resources/.../enchantment/backstab.json` | 全文件 | 数据与代码不一致 | 🔴 CRIT-3 |
| `src/main/java/.../handler/CriticalHitEventHandler.java` | 39 | 注释残留 | 🟡 REC-2 |
| `src/main/java/.../EquipmentMod.java` | 30 | 注释残留 | 🟡 REC-1 |
| `src/main/java/.../item/MeleeItem.java` | 39-45, 218-226 | 注释残留 | 🟡 REC-1 |
| `src/main/resources/.../scripts/default_grenade_state_machine.lua` | 19 | Lua 命名不一致 | 🟢 INFO-2 |
| `src/main/resources/.../lang/zh_cn.json` | — | 缺失 8 条翻译 | 🟡 REC-6 |
| `src/main/resources/.../lang/en_us.json` | — | 缺失 8 条翻译 | 🟡 REC-6 |

### 原版关键文件参考

所有移植操作应以原版仓库为准：
```
https://github.com/LesRaisins-Studios/LesRaisins-Tactical-Equipements
分支: 1.20.1
```

---
