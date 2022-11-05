package net.tiffit.rotcg.screen.character;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.tiffit.realmnetapi.auth.data.PlayerChar;

public class CharSelectList extends ObjectSelectionList<CharSelectListEntry> {
    private final CharSelectScreen parent;

    public CharSelectList(Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight, CharSelectScreen screen) {
        super(pMinecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
        this.parent = screen;
        for (PlayerChar pc : screen.chars) {
            addEntry(new CharSelectListEntry(pc, screen.env));
        }
    }
}
