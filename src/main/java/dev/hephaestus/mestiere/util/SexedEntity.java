package dev.hephaestus.mestiere.util;

public interface SexedEntity {
    enum Sex {
        MALE,
        FEMALE
    }
    void setSex(Sex sex);
    Sex getSex();
}