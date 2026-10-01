package com.vyaparguard.dto;

public class AiChatRequestDto {
    private String question;

    public AiChatRequestDto() {
    }

    public AiChatRequestDto(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
