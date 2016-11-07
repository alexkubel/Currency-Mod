package gunn.modcurrency.tiles;

import gunn.modcurrency.items.ModItems;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

/**
 * This class was created by <Brady Gunn>.
 * Distributed with the Currency Mod for Minecraft.
 *
 * The Currency Mod is open source and distributed
 * under the General Public License
 *
 * File Created on 2016-10-30.
 */
public class TileVendor extends TileEntity implements ICapabilityProvider, ITickable{

    public static final int MONEY_SLOT_COUNT = 1;
    public static final int VEND_SLOT_COUNT = 30;
    public static final int TOTAL_SLOTS_COUNT = MONEY_SLOT_COUNT + VEND_SLOT_COUNT;

    private int bank;
    private ItemStackHandler itemStackHandler = new ItemStackHandler(TOTAL_SLOTS_COUNT) {
        @Override
        protected void onContentsChanged(int slot) { markDirty(); }
    };

    public TileVendor(){
        bank = 0;
    }

    @Override
    public void update() {
        if(!worldObj.isRemote){
            if(itemStackHandler.getStackInSlot(0) != null){
                int amnt;
                switch(itemStackHandler.getStackInSlot(0).getItemDamage()){
                    case 0:         //One Dollar Bill
                        amnt = 1;
                        break;
                    case 1:         //Five Dollar Bill
                        amnt = 5;
                        break;
                    case 2:         //Ten Dollar Bill
                        amnt = 10;
                        break;
                    case 3:         //Twenty Dollar Bill
                        amnt = 20;
                        break;
                    case 4:         //Fifty Dollar Bill
                        amnt = 50;
                        break;
                    case 5:         //One Hundred Dollar Bill
                        amnt = 100;
                        break;
                    default:
                        amnt = -1;
                        break;
                }
                amnt = amnt * itemStackHandler.getStackInSlot(0).stackSize;
                itemStackHandler.setStackInSlot(0, null);
                bank = bank + amnt;
                markDirty();
            }
        }
    }
    
    //Outputs change in least amount of bills
    public void outChange(){
        int amount = bank;
        int[] out = new int[6];

        out[5] = Math.round(amount / 100);
        amount = amount - (out[5] * 100);

        out[4] =  Math.round(amount / 50);
        amount = amount - (out[4] * 50);

        out[3] = Math.round(amount / 20);
        amount = amount - (out[3] * 20);

        out[2] = Math.round(amount / 10);
        amount = amount - (out[2] * 10);

        out[1] = Math.round(amount / 5);
        amount = amount - (out[1] * 5);

        out[0] = Math.round(amount);
        amount = amount - out[0];

        if(amount != 0) System.err.print("Calculating Change messed up somewhere....");
        bank = 0;

        if(!worldObj.isRemote) {
            for(int i = 0; i < out.length; i++) {
                if(out[i] != 0) {
                    ItemStack item = new ItemStack(ModItems.itembanknote);
                    item.setItemDamage(i);
                    item.stackSize = out[i];
                    worldObj.spawnEntityInWorld(new EntityItem(worldObj, getPos().getX(), getPos().getY(), getPos().getZ(), item));
                }
            }
        }
    }
    

    
    
    //<editor-fold desc="Item Handler Methods">
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return (T) itemStackHandler;
        }
        return super.getCapability(capability, facing);
    }
    
    public boolean canInteractWith(EntityPlayer player){
        return !isInvalid() && player.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }
    //</editor-fold>
    
    //<editor-fold desc="Packet and Server-to-Client Mumbo Jumbo">
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasKey("items")) itemStackHandler.deserializeNBT((NBTTagCompound) compound.getTag("items"));
        if(compound.hasKey("bank")) bank = compound.getInteger("bank");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("items", itemStackHandler.serializeNBT());
        compound.setInteger("bank", bank);
        return compound;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("bank", bank);
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        bank = getUpdatePacket().getNbtCompound().getInteger("bank");
    }

    public int getFieldCount(){
        return 1;
    }

    public void setField(int id, int value){
        if(id == 0){
            bank = value;
        }
    }

    public int getField(int id){
        if(id == 0){
            return bank;
        }
        return -1;
    }
    //</editor-fold>
}