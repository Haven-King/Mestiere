package dev.hephaestus.mestiere.client.gui;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ScrollingGuiDescription<W extends WWidget> extends LightweightGuiDescription {
    WGridPanel root = new WGridPanel() {{setSize(200, 160);}};

    BetterListPanel<W> listPanel;

    public ScrollingGuiDescription(List<Identifier> data, Supplier<W> supplier, BiConsumer<Identifier, W> configurator) {
        setRootPanel(root);

        listPanel = new BetterListPanel<>(data, supplier, configurator);
        root.add(listPanel, 0, 1, root.getWidth()/18, root.getHeight()/18);

        root.validate(this);
    }

    public void scroll(int x, int y, double amount) {
        this.listPanel.onMouseScroll(x, y, amount);
    }

    public static class BetterListPanel<W extends WWidget> extends WListPanel<Identifier, W> {

        public BetterListPanel(List<Identifier> data, Supplier<W> supplier, BiConsumer<Identifier, W> configurator) {
            super(data, supplier, configurator);
        }

        @Override
        public void onMouseScroll(int x, int y, double amount) {
            super.onMouseScroll(x, y, amount);
            this.scrollBar.setValue((int) (this.scrollBar.getValue()-amount));
        }
    }
}
