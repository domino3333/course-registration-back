package com.course.service;

import com.course.dto.queue.QueueStatusResponse;

public interface QueueService {

    Long enterQueue(String email);
    Long getMyRank(String email);
    void leaveQueue(String email);
    QueueStatusResponse getQueueStatus(String email);

}
