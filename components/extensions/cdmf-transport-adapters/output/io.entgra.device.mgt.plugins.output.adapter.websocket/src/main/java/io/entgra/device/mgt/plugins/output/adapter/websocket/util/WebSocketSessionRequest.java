/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.plugins.output.adapter.websocket.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * This is wrapper class over the javax.websocket.Session implementation. This class contains additional attributes
 * of the Session object derived from processing some of the (default) existing attributes.
 * Ex: Query-String's [Key:Value] Map derived from the queryString attribute of the original class.
 */
public class WebSocketSessionRequest {
    private static final Log log = LogFactory.getLog(WebSocketSessionRequest.class);

    private static final String QUERY_STRING_SEPERATOR = "&";
    private static final String QUERY_KEY_VALUE_SEPERATOR = "=";
    private Map<String, String> queryParamValuePairs = new HashMap<>();
    private Session session;

    public WebSocketSessionRequest(Session session) {
        this.session = session;
        setQueryParamValuePairs();
    }

    public Map<String, String> getQueryParamValuePairs() {
        return queryParamValuePairs;
    }

    public Session getSession() {
        return session;
    }

    /**
     * Processes the queryString from the current instance's Session attribute and constructs a map of Query
     * Key:Value pair.
     */
    private void setQueryParamValuePairs() {
        if (session.getQueryString() != null) {
            String queryString = session.getQueryString();
            String[] allQueryParamPairs = queryString.split(QUERY_STRING_SEPERATOR);
            for (String keyValuePair : allQueryParamPairs) {
                String[] thisQueryParamPair = keyValuePair.split(QUERY_KEY_VALUE_SEPERATOR);
                if (thisQueryParamPair.length != 2) {
                    log.warn("Invalid query string [" + queryString + "] passed in.");
                    break;
                }
                queryParamValuePairs.put(thisQueryParamPair[0], thisQueryParamPair[1]);
            }
        }
    }
}
