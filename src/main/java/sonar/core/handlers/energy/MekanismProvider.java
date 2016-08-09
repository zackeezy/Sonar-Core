package sonar.core.handlers.energy;

import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.util.CapabilityUtils;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Loader;
import sonar.core.api.energy.EnergyHandler;
import sonar.core.api.energy.EnergyType;
import sonar.core.api.energy.StoredEnergyStack;
import sonar.core.api.utils.ActionType;

public class MekanismProvider extends EnergyHandler {

	public static String name = "Mekanism-Provider";

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean canProvideEnergy(TileEntity tile, EnumFacing dir) {
		if (tile != null && CapabilityUtils.hasCapability(tile, Capabilities.ENERGY_STORAGE_CAPABILITY, dir) || CapabilityUtils.hasCapability(tile, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, dir)) {
			return true;
		}
		return tile instanceof IStrictEnergyStorage;
	}

	@Override
	public void getEnergy(StoredEnergyStack energyStack, TileEntity tile, EnumFacing dir) {
		IStrictEnergyStorage storage = CapabilityUtils.getCapability(tile, Capabilities.ENERGY_STORAGE_CAPABILITY, null);
		if (storage == null && tile instanceof IStrictEnergyStorage) {
			storage = (IStrictEnergyStorage) tile;
		}
		if (storage != null) {
			energyStack.setStorageValues((long) (storage.getEnergy() / 10), (long) (storage.getMaxEnergy() / 10));
		}
	}

	@Override
	public StoredEnergyStack addEnergy(StoredEnergyStack transfer, TileEntity tile, EnumFacing dir, ActionType action) {
		IStrictEnergyAcceptor acceptor = CapabilityUtils.getCapability(tile, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, dir);
		if (acceptor == null && tile instanceof IStrictEnergyAcceptor) {
			acceptor = (IStrictEnergyAcceptor) tile;
		}
		if (acceptor != null) {
			if (acceptor.canReceiveEnergy(dir)) {
				transfer.stored -= action.shouldSimulate() ? Math.min(acceptor.getMaxEnergy() - acceptor.getEnergy(), transfer.stored) : acceptor.transferEnergyToAcceptor(dir, transfer.stored);
			}
		}
		if (transfer.stored == 0)
			transfer = null;
		return transfer;
	}

	@Override
	public StoredEnergyStack removeEnergy(StoredEnergyStack transfer, TileEntity tile, EnumFacing dir, ActionType action) {
		IStrictEnergyStorage storage = CapabilityUtils.getCapability(tile, Capabilities.ENERGY_STORAGE_CAPABILITY, null);
		if (storage == null && tile instanceof IStrictEnergyStorage) {
			storage = (IStrictEnergyStorage) tile;
		}
		if (storage != null) {
			double maxRemove = Math.min(transfer.stored, storage.getEnergy());
			transfer.stored -= maxRemove;
			if (!action.shouldSimulate())
				storage.setEnergy(storage.getEnergy() - maxRemove);
		}
		return transfer;
	}

	public boolean isLoadable() {
		return Loader.isModLoaded("Mekanism");
	}

	@Override
	public EnergyType getProvidedType() {
		return EnergyType.MJ;
	}

}
