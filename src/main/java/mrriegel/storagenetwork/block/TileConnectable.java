package mrriegel.storagenetwork.block;

import javax.annotation.Nullable;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.api.data.DimPos;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.capabilities.CapabilityConnectable;
import mrriegel.storagenetwork.capabilities.StorageNetworkCapabilities;
import mrriegel.storagenetwork.config.ConfigHandler;
import mrriegel.storagenetwork.data.EnumSortType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Base class for Cable, Control, Request
 *
 */
public class TileConnectable extends TileEntity {

  private World worldCreate;
  protected CapabilityConnectable connectable;
  public boolean downwards;
  public EnumSortType sort = EnumSortType.NAME;

  public TileConnectable() {
    connectable = new CapabilityConnectable();
  }

  public DimPos getDimPos() {
    return new DimPos(world == null ? worldCreate : world, pos);
  }

  public boolean isDownwards() {
    return downwards;
  }

  public void setDownwards(boolean downwards) {
    this.downwards = downwards;
  }

  public EnumSortType getSort() {
    return sort;
  }

  public void setSort(EnumSortType sort) {
    this.sort = sort;
  }

  @Override
  public void setPos(BlockPos posIn) {
    super.setPos(posIn);
    connectable.setPos(getDimPos());
  }

  @Override
  protected void setWorldCreate(World worldIn) {
    super.setWorldCreate(worldIn);
    this.worldCreate = worldIn;
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
    if (compound.hasKey("connectable")) {
      connectable.deserializeNBT(compound.getCompoundTag("connectable"));
    }
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    NBTTagCompound result = super.writeToNBT(compound);
    result.setTag("connectable", connectable.serializeNBT());
    return result;
  }

  @Override
  public NBTTagCompound getUpdateTag() {
    return writeToNBT(new NBTTagCompound());
  }

  @Override
  public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
    return oldState.getBlock() != newSate.getBlock();
  }

  @Override
  public SPacketUpdateTileEntity getUpdatePacket() {
    NBTTagCompound syncData = new NBTTagCompound();
    this.writeToNBT(syncData);
    return new SPacketUpdateTileEntity(this.pos, 1, syncData);
  }

  @Override
  public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
    readFromNBT(pkt.getNbtCompound());
  }

  @Override
  public void onChunkUnload() {
    if (ConfigHandler.reloadNetworkWhenUnloadChunk && connectable != null && connectable.getMasterPos() != null) {
      try {
        TileMaster maybeMaster = StorageNetwork.helpers.getTileMasterForConnectable(connectable);
        if (maybeMaster != null) {
          maybeMaster.refreshNetwork();
        }
      }
      catch (Exception e) {
        StorageNetwork.instance.logger.error("Error on chunk unload ", e);
      }
    }
  }

  @Override
  public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
    if (capability == StorageNetworkCapabilities.CONNECTABLE_CAPABILITY) {
      return true;
    }
    return super.hasCapability(capability, facing);
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
    if (capability == StorageNetworkCapabilities.CONNECTABLE_CAPABILITY) {
      return (T) connectable;
    }
    return super.getCapability(capability, facing);
  }

  public DimPos getMaster() {
    return connectable.getMasterPos();
  }
}
