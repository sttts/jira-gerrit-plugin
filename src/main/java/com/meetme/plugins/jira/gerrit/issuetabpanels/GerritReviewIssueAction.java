/*
 * Copyright 2012 MeetMe, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.meetme.plugins.jira.gerrit.issuetabpanels;

import static com.meetme.plugins.jira.gerrit.issuetabpanels.GerritEventKeys.LAST_UPDATED;
import static com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.GerritEventKeys.APPROVALS;
import static com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.GerritEventKeys.CHANGE;
import static com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.GerritEventKeys.PATCHSET;
import static com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.GerritEventKeys.PROJECT;
import static com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.GerritEventKeys.SUBJECT;
import static com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.GerritEventKeys.URL;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.meetme.plugins.jira.gerrit.data.dto.GerritApproval;
import com.meetme.plugins.jira.gerrit.data.dto.GerritChange;

public class GerritReviewIssueAction extends AbstractIssueAction implements IssueAction {
    private String baseUrl;
    private GerritChange change;
    private DateTimeFormatter dateTimeFormatter;

    public GerritReviewIssueAction(IssueTabPanelModuleDescriptor descriptor, GerritChange change,
            DateTimeFormatter dateTimeFormatter, String baseUrl) {
        super(descriptor);
        this.dateTimeFormatter = dateTimeFormatter;
        this.baseUrl = baseUrl;
        this.change = change;
    }

    @Override
    @SuppressWarnings({ "unchecked", "deprecation" })
    protected void populateVelocityParams(@SuppressWarnings("rawtypes") Map params) {
        params.putAll(EasyMap.build(URL, change.getUrl(),
                SUBJECT, change.getSubject(),
                PROJECT, change.getProject(),
                CHANGE, change.getNumber(),
                PATCHSET, change.getPatchSet().getNumber(),
                LAST_UPDATED, formatLastUpdated(),
                "isoLastUpdated", isoFormatLastUpdated(),
                APPROVALS, change.getPatchSet().getApprovals(),
                "mostSignificantScore", getMostSignificantScore(change.getPatchSet().getApprovals()),
                "baseurl", this.baseUrl));
    }

    String formatLastUpdated() {
        return dateTimeFormatter.format(change.getLastUpdated());
    }

    String isoFormatLastUpdated() {
        final DateTimeFormatter formatter = dateTimeFormatter.withStyle(DateTimeStyle.ISO_8601_DATE_TIME);
        return formatter.format(change.getLastUpdated());
    }

    @Override
    public Date getTimePerformed() {
        return change.getLastUpdated();
    }

    @Override
    public boolean isDisplayActionAllTab() {
        return true;
    }

    /**
     * Returns the lowest score below 0 if available; otherwise the highest score above 0.
     * 
     * @param approvals
     * @return
     */
    GerritApproval getMostSignificantScore(final List<GerritApproval> approvals) {
        if (approvals != null) {
            try {
                GerritApproval min = Collections.min(approvals);
                GerritApproval max = Collections.max(approvals);

                if (min == max) {
                    // Means there was only 1 vote, so show that one.
                    return max;
                }

                if (min.getValueAsInt() < 0) {
                    // There exists a negative vote, so show that one.
                    return min;
                } else {
                    // NOTE: Technically not possible to have a 0-score, but if one exists, use it!
                    // No negative votes, so show the highest positive vote
                    return max;
                }
            } catch (NoSuchElementException nsee) {
                // Collection was empty
            }
        }

        return null;
    }
}
