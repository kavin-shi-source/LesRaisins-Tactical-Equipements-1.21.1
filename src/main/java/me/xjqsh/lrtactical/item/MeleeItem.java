package me.xjqsh.lrtactical.item;

import com.tacz.guns.api.item.IAnimationItem;
import me.xjqsh.lrtactical.api.collision.ITargetFilter;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.client.renderer.item.MeleeItemRenderer;
import me.xjqsh.lrtactical.config.CommonConfig;
import me.xjqsh.lrtactical.init.ModCapabilities;
import me.xjqsh.lrtactical.item.index.MeleeWeaponIndex;
import me.xjqsh.lrtactical.item.melee.CombatData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MeleeItem extends Item implements IAnimationItem, IMeleeWeapon {
    public MeleeItem() {
        super(new Properties().stacksTo(1).setNoRepair());
    }

    @Override
    public boolean isSame(ItemStack stack1, ItemStack stack2) {
        return IMeleeWeapon.super.isSame(stack1, stack2);
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack pStack) {
        ensureEnchantableComponent(pStack);
        return true;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        int value = getMeleeIndex(stack).map(index -> index.getData().getEnchantmentValue()).orElse(0);
        if (value > 0) {
            MeleeWeaponIndex.trySetEnchantableComponent(stack, value);
        }
        return value;
    }

    @NotNull
    @Override
    public String getDescriptionId(@NotNull ItemStack stack) {
        return this.getMeleeIndex(stack).map(MeleeWeaponIndex::getDescriptionId).orElse(super.getDescriptionId(stack));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        return super.getTooltipImage(pStack);
    }

    @Override
    public int getAttackCoolDown(ItemStack stack, MeleeAction action) {
        return this.getMeleeIndex(stack)
                .map(index -> index.getData().getAttackInfo())
                .map(attackInfos -> attackInfos.getAttackInfo(action))
                .map(CombatData.MeleeAttackInfo::getCooldown)
                .orElse(0);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return this.getMeleeIndex(stack).map(MeleeWeaponIndex::getMaxDurability).orElse(0);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return this.getMaxDamage(stack) > 0;
    }

    @Override
    public int getDrawTime(ItemStack stack) {
        return getMeleeIndex(stack).map(index -> index.getData().getDrawTime()).orElse(0);
    }

    @Override
    public int getPutAwayTime(ItemStack stack) {
        return getMeleeIndex(stack).map(index -> index.getData().getPutAwayTime()).orElse(0);
    }

    @Override
    public int getAttackDelay(Player attacker, ItemStack stack, MeleeAction action) {
        return getMeleeIndex(stack)
                .map(index -> index.getData().getAttackInfo())
                .map(attackInfos -> attackInfos.getAttackInfo(action))
                .map(CombatData.MeleeAttackInfo::getDelay)
                .orElse(0);
    }

    @Override
    public CombatData.MeleeMovement getAttackMovement(Player entity, ItemStack stack, MeleeAction action) {
        return getMeleeIndex(stack)
                .map(index -> index.getData().getAttackInfo())
                .map(attackInfos -> attackInfos.getAttackInfo(action))
                .map(CombatData.MeleeAttackInfo::getMovement)
                .orElse(null);
    }

    @Override
    public List<Entity> collectTargets(Player attacker, ItemStack stack, MeleeAction action, Vec3 origin, Vec3 direction) {
        List<Entity> entities = new ArrayList<>();
        this.getMeleeIndex(stack).ifPresent(index -> {
            CombatData combatData = index.getData().getAttackInfo();
            if (combatData == null) {
                return;
            }
            var attackInfo = combatData.getAttackInfo(action);
            if (attackInfo == null) {
                return;
            }
            ITargetFilter filter = attackInfo.getHitbox();
            entities.addAll(filter.filterTargets(attacker, origin, direction));
        });
        return entities;
    }

    @Override
    public void attack(Player attacker, ItemStack stack, MeleeAction action, List<Entity> targets) {
        this.getMeleeIndex(stack).ifPresent(index -> {
            CombatData combatData = index.getData().getAttackInfo();
            if (combatData == null) {
                return;
            }
            int actionIndex = attacker.getData(ModCapabilities.COMBAT_PROPERTIES).getActionCount(action);

            var attackInfo = combatData.getAttackInfo(action, actionIndex);
            if (attackInfo == null) {
                attackInfo = combatData.getAttackInfo(action, 0);
            }
            if (attackInfo == null) {
                return;
            }
            ITargetFilter filter = attackInfo.getHitbox();
            IMeleeWeapon.playMeleeSound(attacker, index.getId(), action.getId(), 2, 1, true);

            double baseDamage = attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
            var damageModifiers = index.getDefaultModifiers().get(Attributes.ATTACK_DAMAGE.value());
            if (damageModifiers != null && !damageModifiers.isEmpty()) {
                double value = baseDamage;
                for (AttributeModifier modifier : damageModifiers) {
                    value += switch (modifier.operation()) {
                        case ADD_VALUE -> modifier.amount();
                        case ADD_MULTIPLIED_BASE -> baseDamage * modifier.amount();
                        case ADD_MULTIPLIED_TOTAL -> value * modifier.amount();
                    };
                }
                baseDamage = value;
            }

            double baseKnockback = attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
            var knockbackModifiers = index.getDefaultModifiers().get(Attributes.ATTACK_KNOCKBACK.value());
            if (knockbackModifiers != null && !knockbackModifiers.isEmpty()) {
                double value = baseKnockback;
                for (AttributeModifier modifier : knockbackModifiers) {
                    value += switch (modifier.operation()) {
                        case ADD_VALUE -> modifier.amount();
                        case ADD_MULTIPLIED_BASE -> baseKnockback * modifier.amount();
                        case ADD_MULTIPLIED_TOTAL -> value * modifier.amount();
                    };
                }
                baseKnockback = value;
            }

            float damage = (float) (baseDamage * attackInfo.getFactor());
            float knockback = (float) (baseKnockback + attackInfo.getKnockback());

            if (damage <= 0) return;
            boolean hit = false;
            boolean crit = false;
            for (Entity livingentity : targets) {
                boolean flag = !(livingentity instanceof ArmorStand armorStand) || !armorStand.isMarker();
                boolean inRange = livingentity.distanceToSqr(attacker) <= filter.getMaxRange() * filter.getMaxRange();

                if (livingentity != attacker && flag && inRange) {
                    var result = this.performAttack(attacker, livingentity, stack, damage, knockback);
                    hit |= result.hit();
                    crit |= result.crit();
                }
            }

            if (hit) {
                if (CommonConfig.MELEE_ITEM_CONSUME_DURABILITY.get()) {
                    stack.hurtAndBreak(attackInfo.getDurabilityDamage(), attacker, EquipmentSlot.MAINHAND);
                }
                IMeleeWeapon.playMeleeSound(attacker, index.getId(), crit ? "crit" : action.getId() + "_hit", 2, 1);
            }
        });
    }

    private void ensureEnchantableComponent(ItemStack stack) {
        int value = getMeleeIndex(stack).map(index -> index.getData().getEnchantmentValue()).orElse(0);
        if (value > 0) {
            MeleeWeaponIndex.trySetEnchantableComponent(stack, value);
        }
    }
}
