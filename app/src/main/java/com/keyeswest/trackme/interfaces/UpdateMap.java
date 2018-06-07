package com.keyeswest.trackme.interfaces;


import com.keyeswest.trackme.models.Segment;

public interface UpdateMap {
    void addSegment(Segment segment);
    void removeSegment(Segment segment);
}

