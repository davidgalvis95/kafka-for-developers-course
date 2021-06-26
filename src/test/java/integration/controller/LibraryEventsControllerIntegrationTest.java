package controller;

import com.kafkalearn.libraryproject.domain.Book;
import com.kafkalearn.libraryproject.domain.LibraryEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;



@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT )
@EmbeddedKafka( topics = "library-events",
                partitions = 3 )
//These are properties that are injected by the EmbeddedKafkaBroker class that is coming with kafka libraries and brought up with spring context,
//So we need to override the properties set up in the app.props file, so that it won't take the default kafka broker that is may down
@TestPropertySource( properties = { "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
                                    "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}" } )
public class LibraryEventsControllerIntegrationTest
{
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp(){
        final Map<String, Object> configs = new HashMap<>( KafkaTestUtils.consumerProps( "group1", "true", embeddedKafkaBroker ) );
        consumer = new DefaultKafkaConsumerFactory<>( configs, new IntegerDeserializer(), new StringDeserializer() ).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics( consumer );
    }

    @AfterEach
    void tearDown(){
        consumer.close();
    }

    @Test
    //This is another way to make the test to wait what we want it to wait until the async process of the producer-consumer is complete to assert
    @Timeout( 5 )
    void postLibraryEvent()
          throws InterruptedException
    {
        //given
        final Book book = Book.builder()
                              .bookId( 123 )
                              .bookAuthor( "David" )
                              .bookName( "Kafka using spring boot" )
                              .build();

        final LibraryEvent libraryEvent = LibraryEvent.builder()
                                                      .libraryEventId( null )
                                                      .book( book )
                                                      .build();
        final HttpHeaders headers = new HttpHeaders();
        headers.set( "content-type", MediaType.APPLICATION_JSON.toString() );
        final HttpEntity<LibraryEvent> request = new HttpEntity<>( libraryEvent, headers );

        //when
        final ResponseEntity<LibraryEvent> responseEntity = restTemplate.exchange( "v1/libraryEvent", HttpMethod.POST, request, LibraryEvent.class );
        //then
        assertEquals( HttpStatus.CREATED, responseEntity.getStatusCode() );
        //This is introduced due that consumer and producers and the test itself is run in different threads asynchronously
//        Thread.sleep( 3000 );
        final ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord( consumer, "library-events" );
        final String value = consumerRecord.value();
        //TODO make this to be compared against a JSON value
        assertEquals( "", value );
    }
}
