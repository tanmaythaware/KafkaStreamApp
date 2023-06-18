package com.example.streamappdummy.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import java.util.Map;


@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Event {

    @NotNull
    private String eventId;

    @NotNull
    private String type;

    @NotNull
    private Long originTimestamp;

    @NotNull
    private Long publishedTimestamp;

    @NotNull
    private Map<String, Object> payload;

    private String userName;

}
