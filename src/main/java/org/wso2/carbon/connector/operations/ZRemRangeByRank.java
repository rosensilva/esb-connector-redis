/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.connector.operations;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.util.RedisConstants;
import redis.clients.jedis.Jedis;

public class ZRemRangeByRank extends AbstractConnector {

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        RedisServer serverObj = null;
        try {
            serverObj = new RedisServer(messageContext);
            String key = messageContext.getProperty(RedisConstants.KEY).toString();
            long start = Long.parseLong(messageContext.getProperty(RedisConstants.START).toString());
            long end = Long.parseLong(messageContext.getProperty(RedisConstants.END).toString());
            Long response;

            if (serverObj.isClusterEnabled()) {
                response = serverObj.getJedisCluster().zremrangeByRank(key, start, end);
            } else {
                Jedis jedis = null;
                try {
                    jedis = serverObj.getJedis();
                    response = jedis.zremrangeByRank(key, start, end);
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            }
            if (response != null) {
                messageContext.setProperty(RedisConstants.RESULT, response);
            } else {
                handleException("Redis server throw null response", messageContext);
            }
        } catch (Exception e) {
            handleException("Error while connecting the server or calling the redis method", e, messageContext);
        } finally {
            if (serverObj != null) {
                serverObj.close();
            }
        }
    }
}
