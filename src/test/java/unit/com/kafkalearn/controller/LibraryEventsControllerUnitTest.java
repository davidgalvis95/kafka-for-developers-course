package com.kafkalearn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkalearn.controller.LibraryEventsController;
import com.kafkalearn.domain.Book;
import com.kafkalearn.domain.LibraryEvent;
import com.kafkalearn.kafka.producer.LibraryEventsProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@WebMvcTest( LibraryEventsController.class )
@AutoConfigureMockMvc
public class LibraryEventsControllerUnitTest
{
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private LibraryEventsProducer libraryEventsProducer;


    @Test
    void postLibraryEvent()
          throws Exception
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
        final String json = objectMapper.writeValueAsString( libraryEvent );
        when( libraryEventsProducer.sendLibraryEvent_Approach2( isA( LibraryEvent.class ) ) ).thenReturn( null );

        mockMvc.perform( post( "/v1/libraryEvent" )
                               .content( json )
                               .contentType( MediaType.APPLICATION_JSON ) )
               .andExpect( MockMvcResultMatchers.status().isCreated() );
    }


    @Test
    void postLibraryEvent_4xx()
          throws Exception
    {
        final Book book = Book.builder()
                              .bookName( "Kafka using spring boot" )
                              .build();

        final LibraryEvent libraryEvent = LibraryEvent.builder()
                                                      .book( book )
                                                      .build();
        final String json = objectMapper.writeValueAsString( libraryEvent );
        when( libraryEventsProducer.sendLibraryEvent_Approach2( isA( LibraryEvent.class ) ) ).thenReturn( null );
        final String expectedErrorMessage = "book.bookAuthor - must not be null, book.bookId - must not be null";

        mockMvc.perform( post( "/v1/libraryEvent" )
                               .content( json )
                               .contentType( MediaType.APPLICATION_JSON ) )
               .andExpect( MockMvcResultMatchers.status().is4xxClientError() )
               .andExpect( content().string(expectedErrorMessage) );
    }
}
