package com.simibubi.create.content.kinetics.deployer;

import java.util.Collection;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

import javax.annotation.Nullable;

import io.github.fabricators_of_create.porting_lib.entity.events.EntityEvents;
import io.github.fabricators_of_create.porting_lib.entity.events.LivingEntityEvents;
import io.github.fabricators_of_create.porting_lib.util.UsernameCache;

import net.fabricmc.fabric.api.entity.FakePlayer;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CKinetics;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DeployerFakePlayer extends FakePlayer {

	private static final Connection NETWORK_MANAGER = new Connection(PacketFlow.CLIENTBOUND);
	public static final UUID fallbackID = UUID.fromString("9e2faded-cafe-4ec2-c314-dad129ae971d");
	Pair<BlockPos, Float> blockBreakingProgress;
	ItemStack spawnedItemEffects;
	public boolean placedTracks;
	public boolean onMinecartContraption;
	private UUID owner;

	public DeployerFakePlayer(ServerLevel world, @Nullable UUID owner) {
		super(world, new DeployerGameProfile(fallbackID, "Deployer", owner));
		// fabric: use the default FakePacketListener
//		connection = new FakePlayNetHandler(world.getServer(), this);
		this.owner = owner;
	}

	@Override
	public OptionalInt openMenu(MenuProvider menuProvider) {
		return OptionalInt.empty();
	}

	@Override
	public Component getDisplayName() {
		return Lang.translateDirect("block.deployer.damage_source_name");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public float getEyeHeight(Pose poseIn) {
		return 0;
	}

	@Override
	public Vec3 position() {
		return new Vec3(getX(), getY(), getZ());
	}

	@Override
	public float getCurrentItemAttackStrengthDelay() {
		return 1 / 64f;
	}

	@Override
	public boolean canEat(boolean ignoreHunger) {
		return false;
	}

	@Override
	public ItemStack eat(Level world, ItemStack stack) {
		stack.shrink(1);
		return stack;
	}

	@Override
	public boolean canBeAffected(MobEffectInstance pEffectInstance) {
		return false;
	}

	@Override
	public UUID getUUID() {
		return owner == null ? super.getUUID() : owner;
	}

	public static void deployerHasEyesOnHisFeet(EntityEvents.Size event) {
		if (event.getEntity() instanceof DeployerFakePlayer)
			event.setNewEyeHeight(0);
	}

	public static boolean deployerCollectsDropsFromKilledEntities(LivingEntity target, DamageSource source, Collection<ItemEntity> drops, int lootingLevel, boolean recentlyHit) {
		Entity trueSource = source.getEntity();
		if (trueSource != null && trueSource instanceof DeployerFakePlayer) {
			DeployerFakePlayer fakePlayer = (DeployerFakePlayer) trueSource;
			drops
				.forEach(stack -> fakePlayer.getInventory()
					.placeItemBackInInventory(stack.getItem()));
			return true;
		}
		return false;
	}

	@Override
	protected boolean doesEmitEquipEvent(EquipmentSlot p_217035_) {
		return false;
	}

	@Override
	public void remove(RemovalReason p_150097_) {
		if (blockBreakingProgress != null && !level().isClientSide)
			level().destroyBlockProgress(getId(), blockBreakingProgress.getKey(), -1);
		super.remove(p_150097_);
	}

	public static int deployerKillsDoNotSpawnXP(int i, Player player, LivingEntity entity) {
		if (player instanceof DeployerFakePlayer)
			return 0;
		return i;
	}

	public static void entitiesDontRetaliate(LivingEntityEvents.ChangeTarget.ChangeTargetEvent event) {
		if (!(event.getOriginalTarget() instanceof DeployerFakePlayer))
			return;
		LivingEntity entityLiving = (LivingEntity) event.getEntity();
		if (!(entityLiving instanceof Mob mob))
			return;

		CKinetics.DeployerAggroSetting setting = AllConfigs.server().kinetics.ignoreDeployerAttacks.get();

		switch(setting) {
			case ALL -> event.setCanceled(true);
			case CREEPERS -> {
				if (mob instanceof Creeper)
					event.setCanceled(true);
			}
		}
	}

	// Credit to Mekanism for this approach. Helps fake players get past claims and
	// protection by other mods
	private static class DeployerGameProfile extends GameProfile {

		private UUID owner;

		public DeployerGameProfile(UUID id, String name, UUID owner) {
			super(id, name);
			this.owner = owner;
		}

		@Override
		public UUID getId() {
			return owner == null ? super.getId() : owner;
		}

		@Override
		public String getName() {
			if (owner == null)
				return super.getName();
			String lastKnownUsername = UsernameCache.getLastKnownUsername(owner);
			return lastKnownUsername == null ? super.getName() : lastKnownUsername;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o)
				return true;
			if (!(o instanceof GameProfile otherProfile))
				return false;
			return Objects.equals(getId(), otherProfile.getId()) && Objects.equals(getName(), otherProfile.getName());
		}

		@Override
		public int hashCode() {
			UUID id = getId();
			String name = getName();
			int result = id == null ? 0 : id.hashCode();
			result = 31 * result + (name == null ? 0 : name.hashCode());
			return result;
		}
	}

	// fabric: FakePlayNetHandler removed, unused

}
