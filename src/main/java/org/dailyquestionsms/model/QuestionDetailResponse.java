package org.dailyquestionsms.model;

import java.util.List;

public class QuestionDetailResponse {
    private QuestionDetailData data;

    public static class QuestionDetailData {
        private QuestionDetail question;

        public QuestionDetail getQuestion() { return question; }
        public void setQuestion(QuestionDetail question) { this.question = question; }
    }

    public static class QuestionDetail {
        private String questionId;
        private String questionFrontendId;
        private String title;
        private String titleSlug;
        private String difficulty;
        private boolean isPaidOnly;
        private String content;
        private String translatedTitle;
        private String translatedContent;
        private String sampleTestCase;
        private boolean enableRunCode;
        private String metaData;
        private String stats;
        private List<String> hints;
        private List<CodeSnippet> codeSnippets;
        private List<TopicTag> topicTags;
        private Object companyTagStats;
        private int likes;
        private int dislikes;
        private String similarQuestions;

        // Getters and Setters
        public String getQuestionId() { return questionId; }
        public void setQuestionId(String questionId) { this.questionId = questionId; }

        public String getQuestionFrontendId() { return questionFrontendId; }
        public void setQuestionFrontendId(String questionFrontendId) { this.questionFrontendId = questionFrontendId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getTitleSlug() { return titleSlug; }
        public void setTitleSlug(String titleSlug) { this.titleSlug = titleSlug; }

        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

        public boolean isPaidOnly() { return isPaidOnly; }
        public void setPaidOnly(boolean paidOnly) { isPaidOnly = paidOnly; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getTranslatedTitle() { return translatedTitle; }
        public void setTranslatedTitle(String translatedTitle) { this.translatedTitle = translatedTitle; }

        public String getTranslatedContent() { return translatedContent; }
        public void setTranslatedContent(String translatedContent) { this.translatedContent = translatedContent; }

        public String getSampleTestCase() { return sampleTestCase; }
        public void setSampleTestCase(String sampleTestCase) { this.sampleTestCase = sampleTestCase; }

        public boolean isEnableRunCode() { return enableRunCode; }
        public void setEnableRunCode(boolean enableRunCode) { this.enableRunCode = enableRunCode; }

        public String getMetaData() { return metaData; }
        public void setMetaData(String metaData) { this.metaData = metaData; }

        public String getStats() { return stats; }
        public void setStats(String stats) { this.stats = stats; }

        public List<String> getHints() { return hints; }
        public void setHints(List<String> hints) { this.hints = hints; }

        public List<CodeSnippet> getCodeSnippets() { return codeSnippets; }
        public void setCodeSnippets(List<CodeSnippet> codeSnippets) { this.codeSnippets = codeSnippets; }

        public List<TopicTag> getTopicTags() { return topicTags; }
        public void setTopicTags(List<TopicTag> topicTags) { this.topicTags = topicTags; }

        public Object getCompanyTagStats() { return companyTagStats; }
        public void setCompanyTagStats(Object companyTagStats) { this.companyTagStats = companyTagStats; }

        public int getLikes() { return likes; }
        public void setLikes(int likes) { this.likes = likes; }

        public int getDislikes() { return dislikes; }
        public void setDislikes(int dislikes) { this.dislikes = dislikes; }

        public String getSimilarQuestions() { return similarQuestions; }
        public void setSimilarQuestions(String similarQuestions) { this.similarQuestions = similarQuestions; }
    }

    public static class CodeSnippet {
        private String lang;
        private String langSlug;
        private String code;

        public String getLang() { return lang; }
        public void setLang(String lang) { this.lang = lang; }

        public String getLangSlug() { return langSlug; }
        public void setLangSlug(String langSlug) { this.langSlug = langSlug; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    public static class TopicTag {
        private String name;
        private String slug;
        private String translatedName;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }

        public String getTranslatedName() { return translatedName; }
        public void setTranslatedName(String translatedName) { this.translatedName = translatedName; }
    }

    public QuestionDetailData getData() { return data; }
    public void setData(QuestionDetailData data) { this.data = data; }
}
