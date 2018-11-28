/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.component.jbpm.workitem;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.services.api.service.ServiceRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.manager.RuntimeManager;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CamelWorkItemHandlerTest {

    @Mock
    ProducerTemplate producerTemplate;

    @Mock
    Exchange outExchange;
    
    @Mock
    Message outMessage;
    
    @Mock
    CamelContext camelContext;
    
    @Mock
    RuntimeManager runtimeManager;

    @Test
    public void testExecuteGlobalCamelContext() throws Exception {
    
    	String camelEndpointId = "testCamelRoute";
    	String camelRouteUri = "direct://" + camelEndpointId;
    	
    	String testReponse = "testResponse";
    	
    	when(producerTemplate.send(eq(camelRouteUri), any(Exchange.class))).thenReturn(outExchange);
    	when(producerTemplate.getCamelContext()).thenReturn(camelContext);
    	
    	when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
    	
    	when(outExchange.getOut()).thenReturn(outMessage);
    	when(outMessage.getBody()).thenReturn(testReponse);
    	
    	ServiceRegistry.get().register("GlobalCamelService", camelContext);
    	
    	
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("camel-endpoint-id", camelEndpointId);
        workItem.setParameter("request", "someRequest");
        
        CamelWorkItemHandler handler = new CamelWorkItemHandler();
        
        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
        Map<String, Object> results = manager.getResults(workItem.getId());
        assertEquals(2, results.size());
        assertEquals(testReponse, results.get("response"));
    }
    
    @Test
    public void testExecuteLocalCamelContext() throws Exception {
    
    	String camelEndpointId = "testCamelRoute";
    	String camelRouteUri = "direct://" + camelEndpointId;
    	
    	String testReponse = "testResponse";
    	
    	String runtimeManagerId = "testRuntimeManager";
    	
    	when(runtimeManager.getIdentifier()).thenReturn(runtimeManagerId);
    	
    	when(producerTemplate.send(eq(camelRouteUri), any(Exchange.class))).thenReturn(outExchange);
       	when(producerTemplate.getCamelContext()).thenReturn(camelContext);
    	
    	when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
    	
    	when(outExchange.getOut()).thenReturn(outMessage);
    	when(outMessage.getBody()).thenReturn(testReponse);
    	
    	//Register the RuntimeManager bound camelcontext.
    	ServiceRegistry.get().register(runtimeManagerId + "_CamelService", camelContext);
    	
    	
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("camel-endpoint-id", camelEndpointId);
        workItem.setParameter("request", "someRequest");
        
        CamelWorkItemHandler handler = new CamelWorkItemHandler(runtimeManager);
        
        TestWorkItemManager manager = new TestWorkItemManager();
        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));
        Map<String, Object> results = manager.getResults(workItem.getId());
        assertEquals(2, results.size());
        assertEquals(testReponse, results.get("response"));
    }


}
