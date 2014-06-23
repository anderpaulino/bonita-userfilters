/**
 * Copyright (C) 2013-2014 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.userfilter.test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.BonitaTestRunner;
import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.userfilter.identity.UserManagerUserFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializer.class)
public class SameTaskUserFilterTest extends APITestUtil {
	
	private ProcessInstance processInstance;
	private User aDev;
	private ProcessDefinition definition;
	private User processManager;
	private final static String TASK2_NAME = "step2";
	private final static String TASK1_NAME = "step1";


	@Before
	public void setUp() throws Exception{
		final String qualityGuys = "Quality Guys";
		final String devName = "aDeveloper";
		
		loginOnDefaultTenantWithDefaultTechnicalLogger();
		aDev = getIdentityAPI().createUser(devName, "bpm");
		processManager = getIdentityAPI().createUser("processManager", "bpm");
		logoutThenloginAs(devName, "bpm");
		
		final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
				"test with Provided SameTask User implem of UserFilter", "1.0");
		designProcessDefinition.addActor(qualityGuys);
		final String userFilterDefinitionId = "same-task-user";
		designProcessDefinition.addUserTask(TASK1_NAME, qualityGuys);
		designProcessDefinition.addUserTask(TASK2_NAME, qualityGuys).addUserFilter("Filters the executor of a previous task", userFilterDefinitionId, "1.0.0")
		.addInput("usertaskName", new ExpressionBuilder().createConstantStringExpression(TASK1_NAME))
		.addInput("autoAssign", new ExpressionBuilder().createConstantBooleanExpression(true));
		designProcessDefinition.addTransition(TASK1_NAME, TASK2_NAME);
		final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
		businessArchiveBuilder.setProcessDefinition(designProcessDefinition.done());
		
		final InputStream inputStream = UserManagerUserFilter.class.getResourceAsStream("/same-task-user-impl-1.0.0.impl");
		businessArchiveBuilder.addUserFilters(new BarResource("same-task-user-impl-1.0.0.impl", IOUtils.toByteArray(inputStream)));
		inputStream.close();
		
		definition = getProcessAPI().deploy(businessArchiveBuilder.done());
		getProcessAPI().addUserToActor(qualityGuys, definition, aDev.getId());
		final long processDefinitionId = definition.getId();
		getProcessAPI().enableProcess(processDefinitionId);
		
		processInstance = getProcessAPI().startProcess(processDefinitionId);
	}
	
    @After
	public void tearDown() throws BonitaException {
		loginOnDefaultTenantWithDefaultTechnicalLogger();
        disableAndDeleteProcess(definition);
        deleteUser(aDev);
        deleteUser(processManager);
        logoutOnTenant();
	}
	
	
    @Test
    public void testSameTaskUserFilter() throws Exception {
        final HumanTaskInstance task1 = (HumanTaskInstance) waitForTaskInState(processInstance, TASK1_NAME, TestStates.getReadyState());
        final long userId = aDev.getId();
        assignAndExecuteStep(task1, userId);

        final HumanTaskInstance task2 = (HumanTaskInstance) waitForTaskInState(processInstance, TASK2_NAME, TestStates.getReadyState());
        assertEquals(aDev.getId(), task2.getAssigneeId());
        assertEquals(TestStates.getReadyState(), task2.getState());
        logoutOnTenant();
    }
    
    @Test
    public void testSameTaskUserFilterWithDoFor() throws Exception {
        final HumanTaskInstance task1 = (HumanTaskInstance) waitForTaskInState(processInstance, TASK1_NAME, TestStates.getReadyState());
        final long userId = aDev.getId();
        logoutOnTenant();
        loginOnDefaultTenantWith(processManager.getUserName(), "bpm");
        getProcessAPI().assignUserTask(task1.getId(), userId);
        getProcessAPI().executeFlowNode(userId, task1.getId());
        logoutOnTenant();
        
    loginOnDefaultTenantWithDefaultTechnicalLogger();
        final HumanTaskInstance task2 = (HumanTaskInstance) waitForTaskInState(processInstance, TASK2_NAME, TestStates.getReadyState());
        assertEquals(aDev.getId(), task2.getAssigneeId());
        assertEquals(TestStates.getReadyState(), task2.getState());
        logoutOnTenant();
    }
    
}
