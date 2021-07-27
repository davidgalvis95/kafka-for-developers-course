package com.kafkalearn.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkalearn.domain.Book;
import com.kafkalearn.domain.LibraryEvent;
import com.kafkalearn.kafka.producer.LibraryEventsProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.SettableListenableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;


@ExtendWith( MockitoExtension.class )
public class LibraryEventProducerUnitTest
{
    @Mock
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private LibraryEventsProducer libraryEventsProducer;


    @Test
    void sendLibraryEvent_Approach2_failure()
    {
        final Book book = Book.builder()
                              .bookId( 123 )
                              .bookAuthor( "David" )
                              .bookName( "Kafka using spring boot" )
                              .build();

        final LibraryEvent libraryEvent = LibraryEvent.builder()
                                                      .libraryEventId( null )
                                                      .book( book )
                                                      .build();

        final SettableListenableFuture future = new SettableListenableFuture();
        future.setException( new RuntimeException( "Exception calling Kafka" ) );

        when( kafkaTemplate.send( isA( ProducerRecord.class ) ) ).thenReturn( future );

        assertThrows( RuntimeException.class, () -> libraryEventsProducer.sendLibraryEvent_Approach2( libraryEvent ).get() );
    }


    @Test
    void sendLibraryEvent_Approach2_success()
          throws JsonProcessingException
    {
        final Book book = Book.builder()
                              .bookId( 123 )
                              .bookAuthor( "David" )
                              .bookName( "Kafka using spring boot" )
                              .build();

        final LibraryEvent libraryEvent = LibraryEvent.builder()
                                                      .libraryEventId( null )
                                                      .book( book )
                                                      .build();

        final String record = objectMapper.writeValueAsString( libraryEvent );
        final ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>( "library-events", libraryEvent.getLibraryEventId(), record );
        final RecordMetadata recordMetadata = new RecordMetadata( new TopicPartition( "library-events", 1 ),
                                                                  1,
                                                                  1,
                                                                  1123456789,
                                                                  System.currentTimeMillis(),
                                                                  1,
                                                                  2 );
        final SendResult<Integer, String> sendResult = new SendResult<>( producerRecord, recordMetadata );
        final SettableListenableFuture future = new SettableListenableFuture();
        future.set( sendResult );

        when( kafkaTemplate.send( isA( ProducerRecord.class ) ) ).thenReturn( future );

        assertThrows( RuntimeException.class, () -> libraryEventsProducer.sendLibraryEvent_Approach2( libraryEvent ).get() );
        var listenableFuture = libraryEventsProducer.sendLibraryEvent_Approach2( libraryEvent );
        assertEquals( 1, sendResult.getRecordMetadata().partition() );
    }
}
