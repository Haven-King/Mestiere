package dev.hephaestus.mestiere.client.gui.widgets;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ScrollingGuiDescription<W extends WWidget> extends LightweightGuiDescription implements ScrollingGui {
    WGridPanel root = new WGridPanel() {{setSize(200, 190);}};

    BetterListPanel<Identifier, W> listPanel;

    public ScrollingGuiDescription(List<Identifier> data, Supplier<W> supplier, BiConsumer<Identifier, W> configurator) {
        setRootPanel(root);

        listPanel = new BetterListPanel<>(data, supplier, configurator);
        root.add(listPanel, 0, 1, root.getWidth()/18, root.getHeight()/18);

        root.validate(this);
    }

    public void scroll(int x, int y, double amount) {
        this.listPanel.onMouseScroll(x, y, amount);
    }
}
