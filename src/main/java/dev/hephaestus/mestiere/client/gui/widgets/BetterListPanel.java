package dev.hephaestus.mestiere.client.gui.widgets;

import io.github.cottonmc.cotton.gui.widget.WListPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class BetterListPanel<D, W extends WWidget> extends WListPanel<D, W> {

    public BetterListPanel(List<D> data, Supplier<W> supplier, BiConsumer<D, W> configurator) {
        super(data, supplier, configurator);
    }

    @Override
    public void onMouseScroll(int x, int y, double amount) {
        super.onMouseScroll(x, y, amount);
        this.scrollBar.setValue((int) (this.scrollBar.getValue()-amount));
    }
}
