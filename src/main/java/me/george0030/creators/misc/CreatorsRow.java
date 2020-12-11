package me.george0030.creators.misc;

import java.util.UUID;

public class CreatorsRow {
    
    public final UUID playerUUID;
    public final String youtube;
    public final long subcount;
    
    public CreatorsRow(UUID playerUUID, String youtube, long subcount) {
        this.playerUUID = playerUUID;
        this.youtube = youtube;
        this.subcount = subcount;
    }
}
