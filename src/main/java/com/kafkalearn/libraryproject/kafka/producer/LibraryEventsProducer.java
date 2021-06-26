package com.kafkalearn.libraryproject.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkalearn.libraryproject.domain.LibraryEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.concurrent.ExecutionException;


@Component
@Slf4j
@AllArgsConstructor
public class LibraryEventsProducer
{
    private final KafkaTemplate<Integer, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    private static final String TOPIC = "library-topic";


    public SendResult<Integer, String> sendLibraryEventSynchronous( LibraryEvent libraryEvent )
          throws ExecutionException, InterruptedException, JsonProcessingException
    {
        final Integer key = libraryEvent.getLibraryEventId();
        final String value = objectMapper.writeValueAsString( libraryEvent );

        SendResult<Integer, String> sendResult = null;

        try
        {
            //This makes that the next steps in the code wait for this to finish, making this method synchronous
            sendResult = kafkaTemplate.sendDefault( key, value ).get();
        }
        catch ( ExecutionException | InterruptedException e )
        {
            log.error( "ExecutionException/InterruptedException sending the message and the exception is {}", e.getMessage() );
            throw e;
        }
        catch ( Exception e )
        {
            log.error( "Exception sending the message an the exception is {}", e.getMessage() );
            throw e;
        }

        return sendResult;
    }


    public void sendLibraryEvent( LibraryEvent libraryEvent )
          throws JsonProcessingException
    {
        final Integer key = libraryEvent.getLibraryEventId();
        final String value = objectMapper.writeValueAsString( libraryEvent );

        final ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault( key, value );

        listenableFuture.addCallback( new ListenableFutureCallback<SendResult<Integer, String>>()
        {
            @Override
            public void onFailure( final Throwable ex )
            {
                handleFailure( key, value, ex );
            }


            @Override
            public void onSuccess( final SendResult<Integer, String> result )
            {
                handleSuccess( key, value, result );
            }
        } );
    }


    public void sendLibraryEvent_Approach2( LibraryEvent libraryEvent )
          throws JsonProcessingException
    {
        final Integer key = libraryEvent.getLibraryEventId();
        final String value = objectMapper.writeValueAsString( libraryEvent );

        ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value, TOPIC);

        //This is another way of producing when using kafka
//        final ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send( TOPIC, key, value );
        final ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send( producerRecord);

        listenableFuture.addCallback( new ListenableFutureCallback<SendResult<Integer, String>>()
        {
            @Override
            public void onFailure( final Throwable ex )
            {
                handleFailure( key, value, ex );
            }


            @Override
            public void onSuccess( final SendResult<Integer, String> result )
            {
                handleSuccess( key, value, result );
            }
        } );
    }


    private ProducerRecord<Integer, String> buildProducerRecord( final Integer key,
                                                                 final String value,
                                                                 final String topic )
    {
        List<Header> recordHeaders = List.of( new RecordHeader( "event-source", "scanner".getBytes() ) );
        return new ProducerRecord<>( topic, null, key, value, recordHeaders );
    }


    private void handleSuccess( Integer key,
                                String value,
                                SendResult<Integer, String> result )
    {
        log.info( "Message sent successfully for key {}, value {}, to partition {}", key, value, result.getRecordMetadata().partition() );
    }


    private void handleFailure( Integer key,
                                String value,
                                Throwable exception )
    {
        log.info( "Error sending message: {}", exception.getMessage() );

        try
        {
            throw exception;
        }
        catch ( Throwable throwable )
        {
            log.error( "Error in OnFailure {}", throwable.getMessage() );
        }
    }
}
