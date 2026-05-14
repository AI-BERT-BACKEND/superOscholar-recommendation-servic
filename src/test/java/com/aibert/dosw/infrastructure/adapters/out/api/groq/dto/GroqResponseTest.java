package com.aibert.dosw.infrastructure.adapters.out.api.groq.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GroqResponseTest {

    @Test
    void lombokGeneratedMethods_areCoveredForResponseChoiceAndMessage() {
        GroqResponse.Message message1 = new GroqResponse.Message();
        message1.setRole("assistant");
        message1.setContent("respuesta");

        GroqResponse.Message message2 = new GroqResponse.Message();
        message2.setRole("assistant");
        message2.setContent("respuesta");

        GroqResponse.Message message3 = new GroqResponse.Message();
        message3.setRole("system");
        message3.setContent("otra");

        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(null, message1);
        assertNotEquals(new Object(), message1);
        assertEquals(message1.hashCode(), message2.hashCode());
        assertNotNull(message1.toString());

        GroqResponse.Choice choice1 = new GroqResponse.Choice();
        choice1.setMessage(message1);
        GroqResponse.Choice choice2 = new GroqResponse.Choice();
        choice2.setMessage(message2);
        GroqResponse.Choice choice3 = new GroqResponse.Choice();
        choice3.setMessage(message3);

        assertEquals(choice1, choice2);
        assertNotEquals(choice1, choice3);
        assertNotEquals(null, choice1);
        assertNotEquals(new Object(), choice1);
        assertEquals(choice1.hashCode(), choice2.hashCode());
        assertNotNull(choice1.toString());

        GroqResponse response1 = new GroqResponse();
        response1.setChoices(List.of(choice1));
        GroqResponse response2 = new GroqResponse();
        response2.setChoices(List.of(choice2));
        GroqResponse response3 = new GroqResponse();
        response3.setChoices(List.of(choice3));

        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertNotEquals(null, response1);
        assertNotEquals(new Object(), response1);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertEquals(1, response1.getChoices().size());
        assertNotNull(response1.toString());
    }
}
