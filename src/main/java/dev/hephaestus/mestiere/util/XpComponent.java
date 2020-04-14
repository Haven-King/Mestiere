package dev.hephaestus.mestiere.util;

import dev.hephaestus.mestiere.skills.Skill;
import nerdhub.cardinal.components.api.component.Component;

public interface XpComponent extends Component {
    int getLevel(Skill skill);
    int getXp(Skill skill);
    void setXp(Skill skill, int xp);
    void addXp(Skill skill, int xp);
}
