package tf2.entity.projectile.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tf2.common.TFExplosion;
import tf2.entity.projectile.EntityTFProjectile;

public class EntityGrenade extends EntityTFProjectile
{
	protected double spread;

	public EntityGrenade(World worldIn)
	{
		super(worldIn);
		this.setSize(0.2F, 0.2F);
	}

	public EntityGrenade(World worldIn, double x, double y, double z)
	{
		super(worldIn, x, y, z);
	}

	public EntityGrenade(World worldIn, EntityLivingBase throwerIn)
	{
		super(worldIn, throwerIn);
		this.setTickAir(100);
	}

	@Override
	public float inWaterSpeed()
	{
		return 0.8F;
	}

	@Override
	public void setEntityDead()
	{
		TFExplosion.doExplosion(this.world, this.thrower, this.posX, this.posY, this.posZ, this.spread, this.getDamage());
		this.world.createExplosion((Entity) null, this.posX, this.posY, this.posZ, 0.0F, false);
		super.setDead();
	}

	@Override
	public void setDead()
	{
		this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D, new int[0]);
		super.setDead();
	}

	@Override
	public void isGravity()
	{
		if (!this.hasNoGravity())
		{
			this.motionY -= 0.02D;
		}
	}

	@Override
	protected float directHitDamage()
	{
		return 0F;
	}


	@SideOnly(Side.CLIENT)
	protected void generateRandomParticles()
	{
		double var211 = this.prevPosX - this.posX;
		double var231 = this.prevPosY - this.posY;
		double var23 = this.prevPosZ - this.posZ;

		if (this.world.isRemote)
		{
			for (int var24 = 0; var24 < 5; ++var24)
			{
				float var16 = 0.2F * (float) var24;

				this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + var211 * (double) var16, this.posY + 0.1D + var231 * (double) var16, this.posZ + var23 * (double) var16, 0.0D, 0.0D, 0.0D, new int[15]);
			}
		}
	}

	public void setSpread(double damageIn)
	{
		this.spread = damageIn;
	}
	public double getSpread()
	{
		return this.spread;
	}
	@Override
	public void writeEntityToNBT(NBTTagCompound compound)
	{
		super.writeEntityToNBT(compound);
		compound.setDouble("spread", this.spread);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readEntityFromNBT(NBTTagCompound compound)
	{
		super.readEntityFromNBT(compound);
		this.spread = compound.getDouble("spread");
	}
}
