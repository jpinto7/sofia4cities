/**
 * Copyright Indra Sistemas, S.A.
 * 2013-2018 SPAIN
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package streaming.twitter.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.Stream;
import org.springframework.stereotype.Component;

import com.indracompany.sofia2.config.components.TwitterConfiguration;
import com.indracompany.sofia2.config.model.Configuration;
import com.indracompany.sofia2.config.services.configuration.ConfigurationService;
import com.indracompany.sofia2.libraries.social.twitter.TwitterServiceFactory;
import com.indracompany.sofia2.libraries.social.twitter.TwitterServiceSpringSocialImpl;

import lombok.extern.slf4j.Slf4j;
import streaming.twitter.listener.TwitterStreamListener;

@Component
@Slf4j
public class TwitterStreamService {

	@Autowired
	private ConfigurationService configurationService;


	
	private Map<String, TwitterStreamListener> listenersMap = new HashMap<String, TwitterStreamListener>(); 
	private Map<String, Stream> streamMap = new HashMap<String, Stream>();
	
	private TwitterServiceSpringSocialImpl getTwitterConfiguration(String configurationId) {
		
		//TODO get default config  throw exception
		Configuration configuration = this.configurationService.getConfiguration(configurationId);
		TwitterConfiguration twitterConfiguration = this.configurationService.
				getTwitterConfiguration(configuration.getEnvironment(), configuration.getSuffix());
		return TwitterServiceFactory.getSpringSocialImpl(twitterConfiguration.getConsumerKey(), 
				twitterConfiguration.getConsumerSecret(), twitterConfiguration.getAccessToken(), 
				twitterConfiguration.getAccessTokenSecret());
		
	}
	
	public void suscribe(TwitterStreamListener twitterStreamListener) throws Exception {
		String listenerId = twitterStreamListener.getId();
		
		if(listenersMap.containsKey(listenerId)) 
			throw new Exception("Error listener already created");
		
		String keywords= "";
		for(String keyword: twitterStreamListener.getKeywords()) {
			keywords = keywords + keyword + ",";
		}
		if(keywords.equals("")) 
			throw new Exception("No keywords found for this Listener");
		
		//close existing stream
		if(streamMap.containsKey(listenerId)) {
			streamMap.get(listenerId).close();
			streamMap.remove(listenerId);
		}
		
		Stream stream = this.getTwitterConfiguration(twitterStreamListener.getConfigurationId())
				.createFilterStreaming(keywords, twitterStreamListener);
		
		//twitterStreamListener.connectSIBREST();
		
		listenersMap.put(listenerId, twitterStreamListener);
		streamMap.put(listenerId, stream);
		log.debug("Listener registered with id "+listenerId+", keywords: "+keywords);
	}
	
	public void unsubscribe(String listenerId) throws Exception {
		TwitterStreamListener listener = listenersMap.get(listenerId);
		
		//TODO check if its necessary to do something to remove from StreamingOperations
		if(listener!=null) {
			Stream stream = streamMap.get(listenerId);
			stream.close();
			listenersMap.remove(listenerId);
			streamMap.remove(listenerId);
		}else
			throw new Exception("Error listener not found");
	}
	
	public boolean isSuscribe(String id) {
		if(listenersMap.containsKey(id))
			return true;
		else
			return false;
	}
	
}
