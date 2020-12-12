package me.george0030.creators.misc;

import java.sql.Timestamp;
import java.util.UUID;

public class CreatorsRow {
    
    public final UUID playerUUID;
    public final String youtube;
    public final long subcount;
    public final Timestamp lastLogout;
    public final String playerName;
    
    public CreatorsRow(UUID playerUUID, String youtube, long subcount, Timestamp lastLogout, String playerName) {
        this.playerUUID = playerUUID;
        this.youtube = youtube;
        this.subcount = subcount;
        this.lastLogout = lastLogout;
        this.playerName = playerName;
    }
}
