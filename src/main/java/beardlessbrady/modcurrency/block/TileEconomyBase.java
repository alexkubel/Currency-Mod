package beardlessbrady.modcurrency.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * This class was created by BeardlessBrady. It is distributed as
 * part of The Currency-Mod. Source Code located on github:
 * https://github.com/BeardlessBrady/Currency-Mod
 * -
 * Copyright (C) All Rights Reserved
 * File Created 2019-02-10
 */

public class TileEconomyBase extends TileEntity {
    protected long cashReserve, cashRegister;
    protected boolean mode;
    protected UUID owner, playerUsing;

    public static UUID EMPTYID = new UUID(0L, 0L);

    public TileEconomyBase(){
        cashReserve = 0;
        cashRegister = 0;
        mode = true;

        owner = new UUID(0L, 0L);
        playerUsing = new UUID(0L, 0L);
    }

    //<editor-fold desc="NBT Stuff">
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setLong("cashReserve", cashReserve);
        compound.setLong("cashRegister", cashRegister);
        compound.setBoolean("mode", mode);
        compound.setString("playerUsing", playerUsing.toString());
        compound.setString("owner", owner.toString());

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("cashReserve")) cashReserve = compound.getLong("cashReserve");
        if(compound.hasKey("cashRegister")) cashRegister = compound.getLong("cashRegister");
        if (compound.hasKey("mode")) mode = compound.getBoolean("mode");
        if (compound.hasKey("playerUsing")) playerUsing = UUID.fromString(compound.getString("playerUsing"));
        if (compound.hasKey("owner")) owner = UUID.fromString(compound.getString("owner"));
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setLong("cashReserve", cashReserve);
        compound.setLong("cashRegister", cashRegister);
        compound.setBoolean("mode", mode);
        compound.setString("playerUsing", playerUsing.toString());
        compound.setString("owner", owner.toString());

        return new SPacketUpdateTileEntity(pos, 1, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound compound = pkt.getNbtCompound();

        mode = compound.getBoolean("mode");
        cashReserve = compound.getLong("cashReserve");
        cashRegister = compound.getLong("cashRegister");
        if(!UUID.fromString(compound.getString("playerUsing")).equals(new UUID(0L, 0L)))playerUsing = UUID.fromString(compound.getString("playerUsing"));
        if(!UUID.fromString(compound.getString("owner")).equals(new UUID(0L, 0L))) owner = UUID.fromString(compound.getString("owner"));
    }
    //</editor-fold>

    //Field Id's
    public static final int FIELD_LONG_CASHRESERVE = 0;
    public static final int FIELD_LONG_CASHREGISTER = 1;

    public static final int FIELD_MODE = 0;


    public int getIntFieldCount(){
        return 1;
    }

    public void setIntField(int id, int value){
        switch(id){
            case FIELD_MODE:
                mode = (value == 1);
                break;
        }
    }

    public int getIntField(int id){
        switch(id){
            case FIELD_MODE:
                return (mode)? 1 : 0;
        }
        return 0;
    }

    public int getLongFieldCount(){
        return 2;
    }

    public void setLongField(int id, long value){
        switch(id){
            case FIELD_LONG_CASHRESERVE:
                cashReserve = value;
                break;
            case FIELD_LONG_CASHREGISTER:
                cashRegister = value;
                break;
        }
    }

    public long getLongField(int id){
        switch(id){
            case FIELD_LONG_CASHRESERVE:
                return cashReserve;
            case FIELD_LONG_CASHREGISTER:
                return cashRegister;
        }
        return -1;
    }


    public UUID getOwner(){
        return owner;
    }

    public void setOwner(UUID uuid){
        owner = uuid;
    }

    public boolean isOwner(){
        return owner.toString().equals(playerUsing.toString());
    }

    public UUID getPlayerUsing(){
        return playerUsing;
    }

    public void setPlayerUsing(UUID uuid){
        playerUsing = uuid;
    }

    public void voidPlayerUsing(){
        playerUsing = EMPTYID;
    }



}