package com.indracompany.sofia2.examples.chatbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ApiRestController {

	private static final Logger LOG =   LoggerFactory.getLogger(ApiRestController.class);
	
	private final RestTemplate restTemplate = new RestTemplate();
	
    @RequestMapping("/greeting")
    public String index() {
        return "Greetings from Spring Boot!";
    }
    
    @RequestMapping("/message")
    public String message(@RequestParam("msg") String msgReceived) {
    	
    	LOG.info("MSG RECIEVED: " +  msgReceived);
    	//String msg = processMsg(msgRecieved);
    	
    	String msg = postMsg(msgReceived);
    	
    	return msg;
    }
    
    private String postMsg(String msg) {

    	String url = "http://localhost:5003/ask_s4c_bot";
    	String requestJson = "{\"msg\":\""+msg+"\"}";
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_JSON);

    	HttpEntity<String> entity = new HttpEntity<String>(requestJson,headers);
    	String answer = restTemplate.postForObject(url, entity, String.class);
    	System.out.println(answer);
    	return answer;
    }

}
