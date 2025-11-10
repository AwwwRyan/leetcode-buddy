package org.dailyquestionsms.model;

public class UserQuestionStatusResponse {
    private String username;
    private String todayQuestionTitle;
    private String todayQuestionTitleSlug;
    private boolean hasSolved;
    private String message;
    private String submissionLanguage;
    private String submissionTimestamp;

    public UserQuestionStatusResponse() {}

    public UserQuestionStatusResponse(String username, String todayQuestionTitle, String todayQuestionTitleSlug, 
                                   boolean hasSolved, String message) {
        this.username = username;
        this.todayQuestionTitle = todayQuestionTitle;
        this.todayQuestionTitleSlug = todayQuestionTitleSlug;
        this.hasSolved = hasSolved;
        this.message = message;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTodayQuestionTitle() { return todayQuestionTitle; }
    public void setTodayQuestionTitle(String todayQuestionTitle) { this.todayQuestionTitle = todayQuestionTitle; }

    public String getTodayQuestionTitleSlug() { return todayQuestionTitleSlug; }
    public void setTodayQuestionTitleSlug(String todayQuestionTitleSlug) { this.todayQuestionTitleSlug = todayQuestionTitleSlug; }

    public boolean isHasSolved() { return hasSolved; }
    public void setHasSolved(boolean hasSolved) { this.hasSolved = hasSolved; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSubmissionLanguage() { return submissionLanguage; }
    public void setSubmissionLanguage(String submissionLanguage) { this.submissionLanguage = submissionLanguage; }

    public String getSubmissionTimestamp() { return submissionTimestamp; }
    public void setSubmissionTimestamp(String submissionTimestamp) { this.submissionTimestamp = submissionTimestamp; }
}
