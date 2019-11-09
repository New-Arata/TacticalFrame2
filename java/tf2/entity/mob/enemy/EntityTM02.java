package tf2.entity.mob.enemy;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import tf2.TF2Core;
import tf2.TFSoundEvents;
import tf2.entity.mob.ai.EntityAIAttackRangedGun;
import tf2.entity.projectile.enemy.EntityEnemyBullet;

public class EntityTM02 extends EntityMobTF implements IRangedAttackMob
{
	public EntityTM02(World worldIn)
	{
		super(worldIn);
		this.setSize(1.2F, 1.2F);
	}

	protected void initEntityAI()
	{
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIAttackRangedGun(this, 1.0D, 18.0F));
		this.tasks.addTask(2, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(4, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityGolem.class, true));
		this.targetTasks.addTask(4, new EntityAINearestAttackableTarget(this, EntityVillager.class, true));
		this.experienceValue = 5;
	}

	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.31D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
	}

	protected void entityInit()
	{
		super.entityInit();
	}

	@Nullable
	protected ResourceLocation getLootTable()
	{
		return TF2Core.ENTITIES_TM06;
	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase var1, float var2)
	{
		double var3 = var1.posX - this.posX;
		double var8 = var1.posY - this.posY;
		double var5 = var1.posZ - this.posZ;

		if (this.attackTime <= 16 && this.attackTime % 8 == 0)
		{
			EntityEnemyBullet var7 = new EntityEnemyBullet(this.world, this);
			var7.setDamage(var7.getDamage() + 3.0D);
			this.playSound(TFSoundEvents.M16, 2.3F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
			var7.shoot(var3, var8, var5, 2.0F, 3.0F);
			this.world.spawnEntity(var7);
		}
		if (this.attackTime <= 0)
		{
			this.attackTime = 100;
		}
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return TFSoundEvents.TM_SAY;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return TFSoundEvents.TM_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn)
	{
		return SoundEvents.ENTITY_IRONGOLEM_HURT;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block blockIn)
	{
	}

	@Override
	public void setSwingingArms(boolean swingingArms)
	{
	}

	@Override
	public boolean getCanSpawnHere()
	{
		boolean flag = TF2Core.CONFIG.spawnMobTMtier1 || TF2Core.CONFIG.spawnMobTMtier2 || TF2Core.CONFIG.spawnMobTMtier3;
		return TF2Core.CONFIG.spawnMobTMtier0 && !flag && this.isValidLightLevel() && this.world.getDifficulty() != EnumDifficulty.PEACEFUL;
	}
}