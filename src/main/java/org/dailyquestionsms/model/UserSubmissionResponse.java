package org.dailyquestionsms.model;

import java.util.List;

public class UserSubmissionResponse {
    private Data data;

    public static class Data {
        private List<Submission> recentAcSubmissionList;

        public List<Submission> getRecentAcSubmissionList() {
            return recentAcSubmissionList;
        }

        public void setRecentAcSubmissionList(List<Submission> recentAcSubmissionList) {
            this.recentAcSubmissionList = recentAcSubmissionList;
        }
    }

    public static class Submission {
        private String id;
        private String title;
        private String titleSlug;
        private String timestamp;
        private String lang;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getTitleSlug() { return titleSlug; }
        public void setTitleSlug(String titleSlug) { this.titleSlug = titleSlug; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public String getLang() { return lang; }
        public void setLang(String lang) { this.lang = lang; }
    }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
}
