/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tansun.jlogstash.distributed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tansun.jlogstash.distributed.util.CountUtil;
import com.tansun.jlogstash.exception.ExceptionUtil;



/**
 * 
 * Reason: TODO ADD REASON(可选)
 * Date: 2016年12月28日 下午1:16:37
 * Company: www.dtstack.com
 * @author sishu.yss
 *
 */
public class HearBeat implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(HearBeat.class);

	private final static int HEATBEAT = 1000;
	
	private ZkDistributed zkDistributed;
	
	private String localAddress;
	
	public HearBeat(ZkDistributed zkDistributed,String localAddress){
		this.zkDistributed  = zkDistributed;
		this.localAddress = localAddress;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			int index =0 ;
			while(true){
				++index;
				BrokerNode brokerNode = BrokerNode.initNullBrokerNode();
				brokerNode.setSeq(1);
				brokerNode.setAlive(true);
				zkDistributed.updateBrokerNodeWithLock(this.localAddress, brokerNode);
				if(CountUtil.count(index, 10))logger.warn("HearBeat start again...");
				Thread.sleep(HEATBEAT);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Hearbeat fail:{}",ExceptionUtil.getErrorMessage(e));
		}
	}
}
