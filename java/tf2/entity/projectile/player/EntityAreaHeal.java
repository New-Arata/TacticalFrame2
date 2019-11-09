package tf2.entity.projectile.player;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tf2.TF2Core;
import tf2.TFDamageSource;
import tf2.util.RegistryHandler;


public class EntityAreaHeal extends Entity implements IProjectile
{
	protected int xTile;
	protected int yTile;
	protected int zTile;
	protected Block inTile;
	protected int inData;
	protected boolean inGround;

	protected EntityLivingBase thrower;

	private int ticksInGround;
	protected int ticksInAir;

	protected double damage;

	public EntityAreaHeal(World worldIn)
	{
		super(worldIn);
		this.xTile = -1;
		this.yTile = -1;
		this.zTile = -1;
		this.setSize(0.2F, 0.2F);
	}

	public EntityAreaHeal(World worldIn, double x, double y, double z)
	{
		this(worldIn);
		this.setPosition(x, y, z);
	}

	public EntityAreaHeal(World worldIn, EntityLivingBase throwerIn)
	{
		this(worldIn, throwerIn.posX, throwerIn.posY + (double) throwerIn.getEyeHeight() - 0.10000000149011612D, throwerIn.posZ);
		this.thrower = throwerIn;
	}
	public static void registerEntity(Class<EntityAreaHeal> clazz, ResourceLocation registryName, String name, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates)
	{
		EntityRegistry.registerModEntity(registryName, clazz, registryName.getResourceDomain() + "." + registryName.getResourcePath(), RegistryHandler.entityId++, TF2Core.INSTANCE, trackingRange, updateFrequency, sendsVelocityUpdates);
	}

	protected void entityInit()
	{}

	public void setHeal()
	{
		List var71 = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(6.0D));
		int var31;
		for (var31 = 0; var31 < var71.size(); ++var31)
		{
			EntityLivingBase var8 = (EntityLivingBase) var71.get(var31);
			if (!var8.world.isRemote)
			{
				var8.heal(3F);
			}
		}
		for (int i = 0; i < 50; ++i)
		{
			double r = 0.2F + this.rand.nextDouble() * 0.5F;
			double t = this.rand.nextDouble() * 2 * Math.PI;

			double e0 = r * Math.sin(t);
			double e2 = r * Math.cos(t);

			double r1 = 1.0D;

			double d4 = this.posX + 0.5D + r1 * Math.sin(t);
			double d6 = this.posZ + 0.5D + r1 * Math.cos(t);
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, d4, this.posY, d6, e0, 0F, e2, new int[0]);
		}
	}

	/**
	 * Checks if the entity is in range to render.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance)
	{
		double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

		if (Double.isNaN(d0))
		{
			d0 = 4.0D;
		}

		d0 = d0 * 64.0D;
		return distance < d0 * d0;
	}

	/**
	 * Sets throwable heading based on an entity that's throwing it
	 */

	public void setHeadingFromThrower(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy)
	{
		float f = -MathHelper.sin(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
		float f1 = -MathHelper.sin((rotationPitchIn + pitchOffset) * 0.017453292F);
		float f2 = MathHelper.cos(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
		this.shoot((double) f, (double) f1, (double) f2, velocity, inaccuracy);
		this.motionX += entityThrower.motionX;
		this.motionZ += entityThrower.motionZ;

		if (!entityThrower.onGround)
		{
			this.motionY += entityThrower.motionY;
		}
	}

	/**
	 * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
	 */
	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy)
	{
		float f = MathHelper.sqrt(x * x + y * y + z * z);
		x = x / (double) f;
		y = y / (double) f;
		z = z / (double) f;
		x = x + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
		y = y + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
		z = z + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
		x = x * (double) velocity;
		y = y * (double) velocity;
		z = z * (double) velocity;
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		float f1 = MathHelper.sqrt(x * x + z * z);
		this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
		this.rotationPitch = (float) (MathHelper.atan2(y, (double) f1) * (180D / Math.PI));
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
		this.ticksInGround = 0;
	}

	/**
	 * Updates the velocity of the entity to a new value.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void setVelocity(double x, double y, double z)
	{
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
		{
			float f = MathHelper.sqrt(x * x + z * z);
			this.rotationPitch = (float) (MathHelper.atan2(y, (double) f) * (180D / Math.PI));
			this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
			this.prevRotationPitch = this.rotationPitch;
			this.prevRotationYaw = this.rotationYaw;
		}
	}

	public void onUpdate()
	{
		super.onUpdate();

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
		{
			float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
			this.rotationPitch = (float) (MathHelper.atan2(this.motionY, (double) f) * (180D / Math.PI));
			this.prevRotationYaw = this.rotationYaw;
			this.prevRotationPitch = this.rotationPitch;
		}

		BlockPos blockpos = new BlockPos(this.xTile, this.yTile, this.zTile);
		IBlockState iblockstate = this.world.getBlockState(blockpos);
		Block block = iblockstate.getBlock();

		if (iblockstate.getMaterial() != Material.AIR)
		{
			AxisAlignedBB axisalignedbb = iblockstate.getCollisionBoundingBox(this.world, blockpos);

			if (axisalignedbb != Block.NULL_AABB && axisalignedbb.offset(blockpos).contains(new Vec3d(this.posX, this.posY, this.posZ)))
			{
				this.inGround = true;
			}
		}
		if (this.ticksInAir % 60 == 0)
		{
			this.setHeal();
		}
		if (!this.inGround && this.ticksInAir <= 600)
        {
			++this.ticksInAir;
			Vec3d vec3d1 = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d vec3d = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			RayTraceResult raytraceresult = this.world.rayTraceBlocks(vec3d1, vec3d, false, true, false);
			vec3d1 = new Vec3d(this.posX, this.posY, this.posZ);
			vec3d = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

			if (raytraceresult != null)
			{
				vec3d = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
			}

			Entity entity = this.findEntityOnPath(vec3d1, vec3d);

			if (entity != null)
			{
				raytraceresult = new RayTraceResult(entity);
			}

			if (raytraceresult != null && raytraceresult.entityHit != null && raytraceresult.entityHit instanceof EntityPlayer)
			{
				EntityPlayer entityplayer = (EntityPlayer) raytraceresult.entityHit;

				if (this.thrower instanceof EntityPlayer && !((EntityPlayer) this.thrower).canAttackPlayer(entityplayer))
				{
					raytraceresult = null;
				}
			}

			if (raytraceresult != null && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult))
			{
				this.onHit(raytraceresult);
			}

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			float f4 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));

			for (this.rotationPitch = (float) (MathHelper.atan2(this.motionY, (double) f4) * (180D / Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
			{
				;
			}

			while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
			{
				this.prevRotationPitch += 360.0F;
			}

			while (this.rotationYaw - this.prevRotationYaw < -180.0F)
			{
				this.prevRotationYaw -= 360.0F;
			}

			while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
			{
				this.prevRotationYaw += 360.0F;
			}

			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
			float f1 = 0.99F;

			if (this.isWet())
			{
				this.extinguish();
			}

			this.motionX *= (double) f1;
			this.motionY *= (double) f1;
			this.motionZ *= (double) f1;

			if (!this.hasNoGravity() && this.motionX != 0F && this.motionZ != 0F)
            {
                this.motionY -= 0.03D;
            }

			this.setPosition(this.posX, this.posY, this.posZ);
			this.doBlockCollisions();

		}
		else
        {
            this.setDead();
        }
	}

	protected DamageSource damageSource()
	{
		if (this.thrower == null)
		{
			return TFDamageSource.causeGrenadeDamage(this);
		}
		else
		{
			return TFDamageSource.causeGrenadeDamage(this.thrower);
		}
	}

	protected void onHit(RayTraceResult raytraceResultIn)
	{
		Entity entity = raytraceResultIn.entityHit;
		if (entity != null)
		{
			DamageSource damagesource = this.damageSource();

			if (entity.attackEntityFrom(damagesource, (float)damage))
			{
				if (entity instanceof EntityLivingBase)
				{
					EntityLivingBase entitylivingbase = (EntityLivingBase) entity;

					if (this.thrower instanceof EntityLivingBase)
					{
						EnchantmentHelper.applyThornEnchantments(entitylivingbase, this.thrower);
						EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) this.thrower, entitylivingbase);
					}

					this.bulletHit(entitylivingbase);
					entitylivingbase.hurtResistantTime = 0;

					if (this.thrower != null && entitylivingbase != this.thrower && entitylivingbase instanceof EntityPlayer && this.thrower instanceof EntityPlayerMP)
					{
						((EntityPlayerMP) this.thrower).connection.sendPacket(new SPacketChangeGameState(6, 0.0F));
					}
				}

				 this.motionX *= -0.02D;
                 this.motionY *= -0.1D;
                 this.motionZ *= -0.02D;
                 this.rotationYaw += 180.0F;
                 this.prevRotationYaw += 180.0F;
			}
		}
		else
		{
			this.motionX *= 0D;
            this.motionY *= 0D;
            this.motionZ *= 0D;
            this.rotationYaw += 180.0F;
            this.prevRotationYaw += 180.0F;
		}
	}

	public static void registerFixesThrowable(DataFixer fixer, String name)
	{}

	@Override
	public void move(MoverType type, double x, double y, double z)
	{
		super.move(type, x, y, z);

		if (this.inGround)
		{
			this.xTile = MathHelper.floor(this.posX);
			this.yTile = MathHelper.floor(this.posY);
			this.zTile = MathHelper.floor(this.posZ);
		}
	}

	@Override
	protected boolean canTriggerWalking()
	{
		return false;
	}

	@Override
	public boolean canBeAttackedWithItem()
	{
		return false;
	}

	protected void bulletHit(EntityLivingBase living)
	{}

	@Nullable
	protected Entity findEntityOnPath(Vec3d start, Vec3d end)
	{
		Entity entity = null;
		List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D));
		double d0 = 0.0D;
		Entity entity1;
		double d1;

		if (!this.world.isRemote)
		{
			for (int k = 0; k < list.size(); ++k)
			{
				entity1 = (Entity) list.get(k);

				if (entity1.canBeCollidedWith() && (entity1 != this.thrower || this.ticksInAir >= 5))
				{
					AxisAlignedBB aabb = entity1.getEntityBoundingBox().grow(0.30000001192092896D);
					RayTraceResult result = aabb.calculateIntercept(start, end);

					if (result != null)
					{
						d1 = start.distanceTo(result.hitVec);

						if (d1 < d0 || d0 == 0.0D)
						{
							entity = entity1;
							d0 = d1;
						}
					}
				}
			}
		}
		return entity;
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	@Override
	public void writeEntityToNBT(NBTTagCompound compound)
	{
		compound.setInteger("xTile", this.xTile);
		compound.setInteger("yTile", this.yTile);
		compound.setInteger("zTile", this.zTile);
		compound.setShort("life", (short) this.ticksInGround);
		ResourceLocation resourcelocation = Block.REGISTRY.getNameForObject(this.inTile);
		compound.setString("inTile", resourcelocation == null ? "" : resourcelocation.toString());
		compound.setByte("inData", (byte) this.inData);
		compound.setByte("inGround", (byte) (this.inGround ? 1 : 0));
		compound.setDouble("damage", this.damage);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readEntityFromNBT(NBTTagCompound compound)
	{
		this.xTile = compound.getInteger("xTile");
		this.yTile = compound.getInteger("yTile");
		this.zTile = compound.getInteger("zTile");
		this.ticksInGround = compound.getShort("life");
		if (compound.hasKey("inTile", 8))
		{
			this.inTile = Block.getBlockFromName(compound.getString("inTile"));
		}
		else
		{
			this.inTile = Block.getBlockById(compound.getByte("inTile") & 255);
		}
		this.inData = compound.getByte("inData") & 255;
		this.inGround = compound.getByte("inGround") == 1;
		if (compound.hasKey("damage", 99))
		{
			this.damage = compound.getDouble("damage");
		}

	}

	@Override
	public void onCollideWithPlayer(EntityPlayer entityIn)
	{}

	public void setDamage(double damageIn)
	{
		this.damage = damageIn;
	}

	public double getDamage()
	{
		return this.damage;
	}
}
