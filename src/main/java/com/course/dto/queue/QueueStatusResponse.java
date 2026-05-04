package com.course.dto.queue;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueueStatusResponse {

    private Long rank;
    private Long waitingAhead;
    private boolean allowed;

}
