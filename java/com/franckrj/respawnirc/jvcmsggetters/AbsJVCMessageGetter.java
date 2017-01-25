package com.franckrj.respawnirc.jvcmsggetters;

import android.os.AsyncTask;
import android.os.Bundle;

import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public abstract class AbsJVCMessageGetter {
    public static final int STATE_LOADING = 0;
    public static final int STATE_NOT_LOADING = 1;

    protected static final String SAVE_TOPIC_URL_TO_FETCH = "saveTopicUrlToFetch";
    protected static final String SAVE_LATEST_LIST_OF_INPUT = "saveLatestListOfInputInAString";
    protected static final String SAVE_LATEST_AJAX_INFO_LIST = "saveLatestAjaxInfoList";
    protected static final String SAVE_LATEST_AJAX_INFO_MOD = "saveLatestAjaxInfoMod";
    protected static final String SAVE_LATEST_AJAX_INFO_PREF = "saveLatestAjaxInfoPref";
    protected static final String SAVE_LAST_ID_OF_MESSAGE = "saveLastIdOfMessage";
    protected static final String SAVE_TOPIC_ID = "saveTopicID";
    protected static final String SAVE_LOCK_REASON = "saveLockReason";
    protected static final String SAVE_SURVEY_TITLE = "saveSurveyTitle";
    protected static final String SAVE_TOPIC_IS_IN_FAV = "saveTopicIsInFav";

    protected String urlForTopic = "";
    protected String latestListOfInputInAString = null;
    protected JVCParser.AjaxInfos latestAjaxInfos = new JVCParser.AjaxInfos();
    protected long lastIdOfMessage = 0;
    protected AbsGetJVCLastMessages currentAsyncTaskForGetMessage = null;
    protected String cookieListInAString = "";
    protected NewMessagesListener listenerForNewMessages = null;
    protected NewGetterStateListener listenerForNewGetterState = null;
    protected NewForumAndTopicNameAvailable listenerForNewForumAndTopicName = null;
    protected JVCParser.ForumAndTopicName currentNames = new JVCParser.ForumAndTopicName();
    protected Boolean isInFavs = null;
    protected String topicID = "";
    protected NewReasonForTopicLock listenerForNewReasonForTopicLock = null;
    protected String lockReason = "";
    protected NewSurveyForTopic listenerForNewSurveyForTopic = null;
    protected String surveyTitle = null;

    public String getUrlForTopic() {
        return urlForTopic;
    }

    public String getLatestListOfInputInAString() {
        return latestListOfInputInAString;
    }

    public JVCParser.AjaxInfos getLatestAjaxInfos() {
        return latestAjaxInfos;
    }

    public long getLastIdOfMessage() {
        return lastIdOfMessage;
    }

    public Boolean getIsInFavs() {
        return isInFavs;
    }

    public String getTopicID() {
        return topicID;
    }

    public String getSurveyTitle() {
        return surveyTitle;
    }

    public void setIsInFavs(Boolean newVal) {
        isInFavs = newVal;
    }

    public void setCookieListInAString(String newCookieListInAString) {
        cookieListInAString = newCookieListInAString;
    }

    public void setListenerForNewMessages(NewMessagesListener thisListener) {
        listenerForNewMessages = thisListener;
    }

    public void setListenerForNewGetterState(NewGetterStateListener thisListener) {
        listenerForNewGetterState = thisListener;
    }

    public void setListenerForNewForumAndTopicName(NewForumAndTopicNameAvailable thisListener) {
        listenerForNewForumAndTopicName = thisListener;
    }

    public void setListenerForNewReasonForTopicLock(NewReasonForTopicLock thisListener) {
        listenerForNewReasonForTopicLock = thisListener;
    }

    public void setListenerForNewSurveyForTopic(NewSurveyForTopic thisListener) {
        listenerForNewSurveyForTopic = thisListener;
    }

    public void stopAllCurrentTask() {
        if (currentAsyncTaskForGetMessage != null) {
            currentAsyncTaskForGetMessage.cancel(true);
            currentAsyncTaskForGetMessage = null;
        }

        if (listenerForNewGetterState != null) {
            listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
        }
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        urlForTopic = savedInstanceState.getString(SAVE_TOPIC_URL_TO_FETCH, "");
        latestListOfInputInAString = savedInstanceState.getString(SAVE_LATEST_LIST_OF_INPUT, null);
        latestAjaxInfos.list = savedInstanceState.getString(SAVE_LATEST_AJAX_INFO_LIST, null);
        latestAjaxInfos.mod = savedInstanceState.getString(SAVE_LATEST_AJAX_INFO_MOD, null);
        latestAjaxInfos.pref = savedInstanceState.getString(SAVE_LATEST_AJAX_INFO_PREF, null);
        lastIdOfMessage = savedInstanceState.getLong(SAVE_LAST_ID_OF_MESSAGE, 0);
        topicID = savedInstanceState.getString(SAVE_TOPIC_ID, "");
        lockReason = savedInstanceState.getString(SAVE_LOCK_REASON, "");
        surveyTitle = savedInstanceState.getString(SAVE_SURVEY_TITLE, "");
        if (savedInstanceState.containsKey(SAVE_TOPIC_IS_IN_FAV)) {
            isInFavs = savedInstanceState.getBoolean(SAVE_TOPIC_IS_IN_FAV, false);
        } else {
            isInFavs = null;
        }
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(SAVE_TOPIC_URL_TO_FETCH, urlForTopic);
        savedInstanceState.putString(SAVE_LATEST_LIST_OF_INPUT, latestListOfInputInAString);
        savedInstanceState.putString(SAVE_LATEST_AJAX_INFO_LIST, latestAjaxInfos.list);
        savedInstanceState.putString(SAVE_LATEST_AJAX_INFO_MOD, latestAjaxInfos.mod);
        savedInstanceState.putString(SAVE_LATEST_AJAX_INFO_PREF, latestAjaxInfos.pref);
        savedInstanceState.putLong(SAVE_LAST_ID_OF_MESSAGE, lastIdOfMessage);
        savedInstanceState.putString(SAVE_TOPIC_ID, topicID);
        savedInstanceState.putString(SAVE_LOCK_REASON, lockReason);
        savedInstanceState.putString(SAVE_SURVEY_TITLE, surveyTitle);
        if (isInFavs != null) {
            savedInstanceState.putBoolean(SAVE_TOPIC_IS_IN_FAV, isInFavs);
        }
    }

    protected TopicPageInfos downloadAndParseTopicPage(String topicLink, String cookies) {
        WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
        TopicPageInfos newPageInfos = null;
        String pageContent;
        currentWebInfos.followRedirects = false;
        pageContent = WebManager.sendRequest(topicLink, "GET", "", cookies, currentWebInfos);

        if (pageContent != null) {
            newPageInfos = new TopicPageInfos();
            newPageInfos.lastPageLink = JVCParser.getLastPageOfTopic(pageContent);
            newPageInfos.nextPageLink = JVCParser.getNextPageOfTopic(pageContent);
            newPageInfos.listOfMessages = JVCParser.getMessagesOfThisPage(pageContent);
            newPageInfos.listOfInputInAString = JVCParser.getListOfInputInAStringInTopicFormForThisPage(pageContent);
            newPageInfos.ajaxInfosOfThisPage = JVCParser.getAllAjaxInfos(pageContent);
            newPageInfos.newNames = JVCParser.getForumAndTopicNameInTopicPage(pageContent);
            newPageInfos.newIsInFavs = JVCParser.getIsInFavsFromPage(pageContent);
            newPageInfos.newTopicID = JVCParser.getTopicIDInThisTopicPage(pageContent);
            newPageInfos.newLockReason = JVCParser.getLockReasonFromPage(pageContent);
            newPageInfos.newSurveyTitle = JVCParser.getSurveyTitleFromPage(pageContent);
        }

        return newPageInfos;
    }

    protected void fillBaseClassInfoFromPageInfo(TopicPageInfos infoOfCurrentPage) {
        latestListOfInputInAString = infoOfCurrentPage.listOfInputInAString;
        latestAjaxInfos = infoOfCurrentPage.ajaxInfosOfThisPage;
        isInFavs = infoOfCurrentPage.newIsInFavs;
        topicID = infoOfCurrentPage.newTopicID;

        if (!latestListOfInputInAString.isEmpty()) {
            latestListOfInputInAString = latestListOfInputInAString + "&form_alias_rang=1";
        }

        if (!infoOfCurrentPage.newNames.equals(currentNames)) {
            currentNames = infoOfCurrentPage.newNames;
            if (listenerForNewForumAndTopicName != null) {
                listenerForNewForumAndTopicName.getNewForumAndTopicName(currentNames);
            }
        }

        if (!Utils.compareStrings(infoOfCurrentPage.newLockReason, lockReason)) {
            lockReason = infoOfCurrentPage.newLockReason;
            if (listenerForNewReasonForTopicLock != null) {
                listenerForNewReasonForTopicLock.getNewLockReason(lockReason);
            }
        }

        if (!Utils.compareStrings(infoOfCurrentPage.newSurveyTitle, surveyTitle)) {
            surveyTitle = infoOfCurrentPage.newSurveyTitle;
            if (listenerForNewSurveyForTopic != null) {
                listenerForNewSurveyForTopic.getNewSurveyTitle(surveyTitle);
            }
        }
    }

    protected abstract class AbsGetJVCLastMessages extends AsyncTask<String, Void, TopicPageInfos> {
    }

    protected static class TopicPageInfos {
        ArrayList<JVCParser.MessageInfos> listOfMessages;
        String lastPageLink;
        String nextPageLink;
        String listOfInputInAString;
        JVCParser.AjaxInfos ajaxInfosOfThisPage;
        JVCParser.ForumAndTopicName newNames;
        Boolean newIsInFavs;
        String newTopicID;
        String newLockReason;
        String newSurveyTitle;
    }

    public interface NewForumAndTopicNameAvailable {
        void getNewForumAndTopicName(JVCParser.ForumAndTopicName newNames);
    }

    public interface NewMessagesListener {
        void getNewMessages(ArrayList<JVCParser.MessageInfos> listOfNewMessages);
    }

    public interface NewGetterStateListener {
        void newStateSetted(int newState);
    }

    public interface NewReasonForTopicLock {
        void getNewLockReason(String newReason);
    }

    public interface NewSurveyForTopic {
        void getNewSurveyTitle(String newTitle);
    }

    public abstract boolean reloadTopic();
}
