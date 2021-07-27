package com.kafkalearn.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;


@Configuration
@Profile( "local" )
public class AutoCreateConfig
{
    //This is a way of creating a topic programmatically, by creating this config class
    @Bean
    public NewTopic libraryEvents(){
        return TopicBuilder.name( "library-events" )
                           .partitions( 3 )
                           //generally the replicas are the same number of servers running in our cluster, in this case 3
                           .replicas( 3 )
                           .build();
    }

}
