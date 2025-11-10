package org.dailyquestionsms.model;

import java.util.List;

public class DailyQuestion {
    private String date;
    private String link;
    private Question question;

    public static class Question {
        private double acRate;
        private String difficulty;
        private String frontendQuestionId;
        private String title;
        private String titleSlug;
        private List<TopicTag> topicTags;

        // Getters and Setters
        public double getAcRate() { return acRate; }
        public void setAcRate(double acRate) { this.acRate = acRate; }

        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

        public String getFrontendQuestionId() { return frontendQuestionId; }
        public void setFrontendQuestionId(String frontendQuestionId) { this.frontendQuestionId = frontendQuestionId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getTitleSlug() { return titleSlug; }
        public void setTitleSlug(String titleSlug) { this.titleSlug = titleSlug; }

        public List<TopicTag> getTopicTags() { return topicTags; }
        public void setTopicTags(List<TopicTag> topicTags) { this.topicTags = topicTags; }
    }

    public static class TopicTag {
        private String name;
        private String slug;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
    }

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
} 