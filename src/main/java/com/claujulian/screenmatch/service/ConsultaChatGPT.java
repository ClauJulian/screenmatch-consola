package com.claujulian.screenmatch.service;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;

public class ConsultaChatGPT {
    public static String obtenerTraduccion(String texto) {
        OpenAiService service = new OpenAiService("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");

        CompletionRequest requisicion = CompletionRequest.builder()
                .model("gpt-3.5-turbo-instruct")
                .prompt("traduce a español el siguiente texto: " + texto)
                .maxTokens(1000)
                .temperature(0.7)
                .build();

        var respuesta = service.createCompletion(requisicion);
        return respuesta.getChoices().get(0).getText();
    }
}
