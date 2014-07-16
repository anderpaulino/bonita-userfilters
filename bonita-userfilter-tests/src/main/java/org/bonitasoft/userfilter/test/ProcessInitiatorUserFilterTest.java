/**
 * Copyright (C) 2012-2014 BonitaSoft S.A.
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

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.BonitaTestRunner;
import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.userfilter.initiator.ProcessInitiatorUserFilter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Matthieu Chaffotte
 */
@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializer.class)
public class ProcessInitiatorUserFilterTest extends APITestUtil {
	
	private User matti;
	private User aleksi;
	private User juho;
	private ProcessDefinition definition;
	private User processManager;

	@Before
	public void setUp() throws Exception {
		final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
		final String delivery = "Delivery men";
		
		final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
		final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("ProcessWithAllConnector", "1.0");
		designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
		designProcessDefinition.addUserTask("step1", delivery).addUserFilter("initiator", "initiator", "1.0.0")
		.addInput("autoAssign", expressionBuilder.createConstantBooleanExpression(true));
		businessArchiveBuilder.setProcessDefinition(designProcessDefinition.done());
		
		final InputStream inputStream = ProcessInitiatorUserFilter.class.getResourceAsStream("/initiator-impl-1.0.0.impl");
		Assert.assertNotNull(inputStream);
		
		businessArchiveBuilder.addUserFilters(new BarResource("initiator-impl-1.0.0.impl", IOUtils.toByteArray(inputStream)));
		inputStream.close();
		
		loginOnDefaultTenantWithDefaultTechnicalLogger();
		matti = getIdentityAPI().createUser("matti", "bpm");
		aleksi = getIdentityAPI().createUser("aleksi", "bpm");
		juho = getIdentityAPI().createUser("juho", "bpm");
		processManager = getIdentityAPI().createUser("processManager", "bpm");
		
		definition = getProcessAPI().deploy(businessArchiveBuilder.done());
		getProcessAPI().addUserToActor(delivery, definition, matti.getId());
		getProcessAPI().addUserToActor(delivery, definition, aleksi.getId());
		getProcessAPI().addUserToActor(delivery, definition, juho.getId());
		getProcessAPI().enableProcess(definition.getId());
		logoutOnTenant();
		
	}
	
	@After
	public void tearDown() throws Exception {
    loginOnDefaultTenantWithDefaultTechnicalLogger();
        disableAndDeleteProcess(definition);
        deleteUser(matti);
        deleteUser(aleksi);
        deleteUser(juho);
        deleteUser(processManager);
        logoutOnTenant();
	}
	
    @Test
    public void testProcessInitiatorUserFilterTest() throws Exception {
        loginOnDefaultTenantWith("matti", "bpm");
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        
        waitForUserTask("step1", processInstance);
        checkAssignations();
    }
    
    @Test
    public void testProcessInitiatorUserFilterTestWithStartFor() throws Exception {
        loginOnDefaultTenantWith("processManager", "bpm");
        final ProcessInstance processInstance = getProcessAPI().startProcess(matti.getId(), definition.getId());
        
        waitForUserTask("step1", processInstance);
        checkAssignations();
    }

	private void checkAssignations() throws BonitaException {
		checkNumberOfAssignationFor(1, matti);
		checkNumberOfAssignationFor(0, processManager);
        logoutOnTenant();
        loginOnDefaultTenantWith("aleksi", "bpm");
        checkNumberOfAssignationFor(0, aleksi);
        logoutOnTenant();
        loginOnDefaultTenantWith("juho", "bpm");
        checkNumberOfAssignationFor(0, juho);
        logoutOnTenant();
	}

	private void checkNumberOfAssignationFor(final int expected, final User user) {
		Assert.assertEquals("There is no the right number of assigned task for user "+user, expected, getNumberOfAssignedTasks(user.getId()));
	}

	private int getNumberOfAssignedTasks(final long userId) {
		return getProcessAPI().getAssignedHumanTaskInstances(userId, 0, 10, ActivityInstanceCriterion.NAME_DESC).size();
	}

}
