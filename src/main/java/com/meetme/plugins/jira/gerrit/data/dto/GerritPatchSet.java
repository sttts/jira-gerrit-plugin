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
package com.meetme.plugins.jira.gerrit.data.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.GerritEventKeys;
import com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.attr.PatchSet;

public class GerritPatchSet extends PatchSet {

    private List<GerritApproval> approvals;

    public GerritPatchSet() {
        super();
    }

    public GerritPatchSet(JSONObject json) {
        super(json);
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);

        if (json.containsKey(GerritEventKeys.APPROVALS)) {
            JSONArray eventApprovals = json.getJSONArray(GerritEventKeys.APPROVALS);
            approvals = new ArrayList<GerritApproval>(eventApprovals.size());

            for (int i = 0; i < eventApprovals.size(); i++) {
                approvals.add(new GerritApproval(eventApprovals.getJSONObject(i)));
            }
        }
    }

    public List<GerritApproval> getApprovals() {
        return approvals;
    }

    public Map<String, List<GerritApproval>> getApprovalsByLabel()
    {
        Map<String, List<GerritApproval>> map = new HashMap<String, List<GerritApproval>>();
        List<GerritApproval> l;

        for (GerritApproval approval : approvals) {
            l = map.get(approval.getType());

            if (l != null) {
                l.add(approval);
            } else {
                map.put(approval.getType(), new ArrayList<GerritApproval>(Collections.singletonList(approval)));
            }
        }

        return map;
    }

    public List<GerritApproval> getApprovalsForLabel(String label)
    {
        List<GerritApproval> filtered = new ArrayList<GerritApproval>();

        if (approvals != null) {
            for (GerritApproval approval : approvals) {
                if (approval.getType().equals(label)) {
                    filtered.add(approval);
                }
            }
        }

        return filtered;
    }

    public void setApprovals(List<GerritApproval> approvals) {
        this.approvals = approvals;
    }
}
