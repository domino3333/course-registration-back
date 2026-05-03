package com.course.service;

public interface QueueService {

    Long enterQueue(String email);
    Long getMyRank(String email);
    void leaveQueue(String email);

}
