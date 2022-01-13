package com.robotikflow.core.models.entities;

public enum CollectionAuthRole 
{
    // NOTAS: 1. não alterar order, porque está sendo usado EnumType.ORDINAL
    //        2. quanto menor o valor, mais permissões tem o role
    CREATOR(0),
    EDITOR(10),
    COMMENTER(20),
    READER(30);

    private int value;

    private CollectionAuthRole(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
}
