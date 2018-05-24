package com.keyeswest.trackme;



import com.keyeswest.trackme.models.Segment;

public interface UpdateMap {
    void addSegment(Segment segment);
    void removeSegment(Segment segment);
}

