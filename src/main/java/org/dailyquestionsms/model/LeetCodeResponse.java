package org.dailyquestionsms.model;

public class LeetCodeResponse {
    private Data data;

    public static class Data {
        private DailyQuestion activeDailyCodingChallengeQuestion;

        public DailyQuestion getActiveDailyCodingChallengeQuestion() { 
            return activeDailyCodingChallengeQuestion; 
        }
        
        public void setActiveDailyCodingChallengeQuestion(DailyQuestion question) { 
            this.activeDailyCodingChallengeQuestion = question; 
        }
    }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
} 