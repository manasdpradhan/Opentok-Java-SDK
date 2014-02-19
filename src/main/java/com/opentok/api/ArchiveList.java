package com.opentok.api;

import java.util.List;

public class ArchiveList {
    
    private final List<Archive> items;
    private final int count;
    
    public ArchiveList(int count, List<Archive> items) {
        this.items = items;
        this.count = count;
    }
    
    public int getCount() {
        return count;
    }
    
    public List<Archive> getItems() {
        return items;
    }
}
