package com.example.myfinalproject;

import java.util.List;

public class Vote {
    private String voteId;
    private String voteTitle;
    private List<String> optionsText;
    private List<String> optionsImages;

    public Vote() {
        // Default constructor required for calls to DataSnapshot.getValue(Vote.class)
    }

    public Vote(String voteId, String voteTitle, List<String> optionsText, List<String> optionsImages) {
        this.voteId = voteId;
        this.voteTitle = voteTitle;
        this.optionsText = optionsText;
        this.optionsImages = optionsImages;
    }

    public String getVoteId() {
        return voteId;
    }

    public void setVoteId(String voteId) {
        this.voteId = voteId;
    }

    public String getVoteTitle() {
        return voteTitle;
    }

    public void setVoteTitle(String voteTitle) {
        this.voteTitle = voteTitle;
    }

    public List<String> getOptionsText() {
        return optionsText;
    }

    public void setOptionsText(List<String> optionsText) {
        this.optionsText = optionsText;
    }

    public List<String> getOptionsImages() {
        return optionsImages;
    }

    public void setOptionsImages(List<String> optionsImages) {
        this.optionsImages = optionsImages;
    }
}
