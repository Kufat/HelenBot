package com.helen.database;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

public class Page implements Selectable {


    private String pageLink;
    private String title;
    private Integer rating;
    private String createdBy;
    private java.sql.Timestamp createdAt;
    private Boolean scpPage = false;
    private String scpTitle;
    private ArrayList<Tag> tags;
    public Page() {

    }
    /**
     * @param pageLink  scp wikidot name the-best-laid-plans-of-mice-and-men
     * @param title     The display title.  The Best Laid Plans of Mice And Men
     * @param rating    Integer rating
     * @param createdBy Who wrote it
     * @param createdAt When it was created
     * @param scpPage   is this an scp article?
     * @param scpTitle  If above is true, this is the title from the series page
     */
    public Page(String pageLink, String title, Integer rating, String createdBy,
                java.sql.Timestamp createdAt, Boolean scpPage, String scpTitle, ArrayList<Tag> tags) {
        this.pageLink = pageLink;
        this.title = title;
        this.rating = rating;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.scpPage = scpPage;
        this.scpTitle = scpTitle;
        this.tags = tags;
    }

    public Page(String pageLink, String title, Boolean scpPage, String scpTitle) {
        this.pageLink = pageLink;
        this.title = title;
        this.scpPage = scpPage;
        this.scpTitle = scpTitle;
    }
    public Page(String pageLink, String title, Integer rating, String createdBy,
                java.sql.Timestamp createdAt, Boolean scpPage, String scpTitle) {
        this.pageLink = pageLink;
        this.title = title;
        this.rating = rating;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.scpPage = scpPage;
        this.scpTitle = scpTitle;
    }

    public boolean searchTest(String[] terms) {
        String search = null;
        if (scpPage) {
            search = scpTitle;
        } else {
            search = title;
        }

        String strLow = search.toLowerCase();
        ArrayList<String> words = new ArrayList<String>();
        words.addAll(Arrays.asList(strLow.split(" ")));
        for (int i = 1; i < terms.length; i++) {
            if (!words.contains(terms[i].toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    public String getPageLink() {
        return pageLink;
    }

    public void setPageLink(String pageLink) {
        this.pageLink = pageLink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getScpPage() {
        return scpPage;
    }

    public void setScpPage(Boolean page) {
        this.scpPage = page;
    }

    public String getScpTitle() {
        return scpTitle;
    }

    public void setScpTitle(String title) {
        this.scpTitle = title;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public void setTags(ArrayList<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public Object selectResource() {
        return pageLink;
    }

    @Override
    public String toString() {
        return "Page{" +
                "pageLink='" + pageLink + '\'' +
                ", title='" + title + '\'' +
                ", rating=" + rating +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                ", scpPage=" + scpPage +
                ", scpTitle='" + scpTitle + '\'' +
                ", tags=" + tags +
                '}';
    }
}
