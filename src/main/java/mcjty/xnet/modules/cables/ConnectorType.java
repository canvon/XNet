package mcjty.xnet.modules.cables;

import net.minecraft.util.IStringSerializable;

public enum ConnectorType implements IStringSerializable {
    NONE,
    CABLE,
    BLOCK;

    public static final ConnectorType[] VALUES = values();

    @Override
    public String getString() {
        return name().toLowerCase();
    }
}
