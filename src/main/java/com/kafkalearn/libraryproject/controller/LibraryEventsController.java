package com.kafkalearn.libraryproject.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafkalearn.libraryproject.domain.LibraryEvent;
import com.kafkalearn.libraryproject.domain.LibraryEventType;
import com.kafkalearn.libraryproject.kafka.producer.LibraryEventsProducer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;


@Slf4j
@RestController
@AllArgsConstructor
public class LibraryEventsController
{
    private final LibraryEventsProducer libraryEventsProducer;

    @PostMapping("v1/libraryEvent")
    public ResponseEntity<LibraryEvent> postLibraryEvent( @RequestBody LibraryEvent libraryEvent )
          throws JsonProcessingException, ExecutionException, InterruptedException
    {
        //invoke kafka producer
        log.info( "invoking producer" );
//        libraryEventsProducer.sendLibraryEvent( libraryEvent );
//        SendResult<Integer, String> sendResult = libraryEventsProducer.sendLibraryEventSynchronous( libraryEvent );
        libraryEvent.setLibraryEventType( LibraryEventType.NEW );
        libraryEventsProducer.sendLibraryEvent_Approach2( libraryEvent );
//        log.info( "after invoking producer {}", sendResult );
        log.info( "after invoking producer");
        return ResponseEntity.status( HttpStatus.CREATED ).body( libraryEvent );
    }

}
