package com.kafkalearn.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafkalearn.domain.LibraryEvent;
import com.kafkalearn.domain.LibraryEventType;
import com.kafkalearn.kafka.producer.LibraryEventsProducer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.concurrent.ExecutionException;


@Slf4j
@RestController
@AllArgsConstructor
public class LibraryEventsController
{
    private final LibraryEventsProducer libraryEventsProducer;


    @PostMapping( "v1/libraryEvent" )
    public ResponseEntity<LibraryEvent> postLibraryEvent( @RequestBody @Valid LibraryEvent libraryEvent )
          throws JsonProcessingException, ExecutionException, InterruptedException
    {
        //invoke kafka producer
        log.info( "invoking producer" );
        //        libraryEventsProducer.sendLibraryEvent( libraryEvent );
        //        SendResult<Integer, String> sendResult = libraryEventsProducer.sendLibraryEventSynchronous( libraryEvent );
        libraryEvent.setLibraryEventType( LibraryEventType.NEW );
        libraryEventsProducer.sendLibraryEvent_Approach2( libraryEvent );
        //        log.info( "after invoking producer {}", sendResult );
        log.info( "after invoking producer" );
        return ResponseEntity.status( HttpStatus.CREATED ).body( libraryEvent );
    }


    @PutMapping( "v1/libraryEvent/update" )
    public ResponseEntity<?> putLibraryEvent( @RequestBody @Valid LibraryEvent libraryEvent )
          throws JsonProcessingException, ExecutionException, InterruptedException
    {
        if ( libraryEvent.getLibraryEventId() == null )
        {
            return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body( "Please pass the library event id" );
        }
        //invoke kafka producer
        log.info( "invoking producer" );
        //        libraryEventsProducer.sendLibraryEvent( libraryEvent );
        //        SendResult<Integer, String> sendResult = libraryEventsProducer.sendLibraryEventSynchronous( libraryEvent );
        libraryEvent.setLibraryEventType( LibraryEventType.UPDATE );
        libraryEventsProducer.sendLibraryEvent_Approach2( libraryEvent );
        //        log.info( "after invoking producer {}", sendResult );
        log.info( "after invoking producer" );
        return ResponseEntity.status( HttpStatus.OK ).body( libraryEvent );
    }
}
