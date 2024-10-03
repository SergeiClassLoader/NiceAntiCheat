package pro.cyrent.anticheat.util.block.box;


import pro.cyrent.anticheat.util.vec.Vec3;

public class HydroMovingPosition {

    private HydroBlockPos blockPos;

    /** What type of ray trace hit was this? 0 = block, 1 = entity */
    public MovingObjectType typeOfHit;
    public EnumFacing sideHit;

    /** The vector position of the hit */
    public Vec3 hitVec;

    /** The hit entity */
//    public Entity entityHit;

    public HydroMovingPosition(Vec3 hitVecIn, EnumFacing facing, HydroBlockPos blockPosIn)
    {
        this(MovingObjectType.BLOCK, hitVecIn, facing, blockPosIn);
    }

    public HydroMovingPosition(Vec3 p_i45552_1_, EnumFacing facing)
    {
        this(MovingObjectType.BLOCK, p_i45552_1_, facing, HydroBlockPos.ORIGIN);
    }

//    public MovingObjectPosition(Entity p_i2304_1_)
//    {
//        this(p_i2304_1_, new Vec3(p_i2304_1_.locX, p_i2304_1_.locY, p_i2304_1_.locZ));
//    }

    public HydroMovingPosition(MovingObjectType typeOfHitIn, Vec3 hitVecIn, EnumFacing sideHitIn, HydroBlockPos blockPosIn)
    {
        this.typeOfHit = typeOfHitIn;
        this.blockPos = blockPosIn;
        this.sideHit = sideHitIn;
        this.hitVec = new Vec3(hitVecIn.xCoord, hitVecIn.yCoord, hitVecIn.zCoord);
    }

//    public MovingObjectPosition(Entity entityHitIn, Vec3 hitVecIn)
//    {
//        this.typeOfHit = MovingObjectType.ENTITY;
//        this.entityHit = entityHitIn;
//        this.hitVec = hitVecIn;
//    }

    public HydroMovingPosition(Vec3 hitVecIn)
    {
        this.typeOfHit = MovingObjectType.ENTITY;
//        this.entityHit = null;
        this.hitVec = hitVecIn;
    }

//    public BlockPos getBlockPos()
//    {
//        return this.blockPos;
//    }

    public String toString()
    {
        return "HitResult{type=" + this.typeOfHit + ", blockpos=" + this.blockPos + ", f=" + this.sideHit + ", pos=" + this.hitVec + '}';
    }

    public static enum MovingObjectType
    {
        MISS,
        BLOCK,
        ENTITY;
    }
}
