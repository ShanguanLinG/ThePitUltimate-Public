package net.mizukilab.pit.util.worldedit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.patterns.Pattern;

public class JustAirBlockPattern implements Pattern {

    private BaseBlock block;

    public JustAirBlockPattern(BaseBlock block) {
        this.block = block;
    }

    public BaseBlock getBlock() {
        return this.block;
    }

    @Override
    public boolean apply(Extent extent, Vector setPosition, Vector getPosition) throws WorldEditException {
        if (!extent.getBlock(getPosition).isAir()) {
            return false;
        }
        return extent.setBlock(setPosition, this.next(getPosition));
    }

    public BaseBlock next(Vector position) {
        return this.block;
    }

    public BaseBlock next(int x, int y, int z) {
        return this.block;
    }
}
