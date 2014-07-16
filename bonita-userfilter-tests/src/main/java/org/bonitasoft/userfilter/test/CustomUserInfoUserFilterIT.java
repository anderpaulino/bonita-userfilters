package org.bonitasoft.userfilter.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.BonitaTestRunner;
import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserFilterDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.CustomUserInfoDefinition;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.userfilter.custom.user.info.CustomUserInfoUserFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializer.class)
public class CustomUserInfoUserFilterIT extends APITestUtil {

    private static final String JAVA = "Java";

    private static final String SKILLS = "skills";

    private User user1;

    private User user2;

    private User user3;

    private User user4;

    private Group group1;

    private Group group2;

    private Role role;

    private CustomUserInfoDefinition userInfoDefinition;

    @Before
    public void setUp() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalLogger();
        user1 = createUser("john", "bpm");
        user2 = createUser("james", "bpm");
        user3 = createUser("paul", "bpm");
        user4 = createUser("jane", "bpm");

        group1 = createGroup("group1");
        group2 = createGroup("group2");

        role = createRole("a role");

        userInfoDefinition = getIdentityAPI().createCustomUserInfoDefinition(new CustomUserInfoDefinitionCreator(SKILLS));

    }

    @After
    public void teardown() throws Exception {

        deleteRoles(role);
        deleteGroups(group1, group2);
        deleteUsers(user1, user2, user3);

        getIdentityAPI().deleteCustomUserInfoDefinition(userInfoDefinition.getId());

        logoutOnTenant();
    }

    @Test
    public void custom_user_info_user_filter_should_return_only_users_with_a_given_user_info_respecting_the_actor_mapping() throws Exception {
        // given
        // map users to groups
        UserMembership membership1 = getIdentityAPI().addUserMembership(user1.getId(), group1.getId(), role.getId());
        UserMembership membership2 = getIdentityAPI().addUserMembership(user2.getId(), group1.getId(), role.getId());
        UserMembership membership3 = getIdentityAPI().addUserMembership(user3.getId(), group1.getId(), role.getId());
        UserMembership membership4 = getIdentityAPI().addUserMembership(user4.getId(), group2.getId(), role.getId());

        // set custom user info
        getIdentityAPI().setCustomUserInfoValue(userInfoDefinition.getId(), user1.getId(), JAVA);
        getIdentityAPI().setCustomUserInfoValue(userInfoDefinition.getId(), user2.getId(), "C");
        getIdentityAPI().setCustomUserInfoValue(userInfoDefinition.getId(), user3.getId(), JAVA);
        getIdentityAPI().setCustomUserInfoValue(userInfoDefinition.getId(), user4.getId(), JAVA);

        // deploy process
        ProcessDefinition processDefinition = deployAndEnableProcessWithCustomUserInfoFilter("step1", SKILLS, JAVA, group1);

        // when
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("step1", processInstance.getId());
        List<HumanTaskInstance> pendingUser1 = getProcessAPI().getPendingHumanTaskInstances(user1.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT);
        List<HumanTaskInstance> pendingUser2 = getProcessAPI().getPendingHumanTaskInstances(user2.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT);
        List<HumanTaskInstance> pendingUser3 = getProcessAPI().getPendingHumanTaskInstances(user3.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT);
        List<HumanTaskInstance> pendingUser4 = getProcessAPI().getPendingHumanTaskInstances(user4.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT);

        // then
        assertThat(pendingUser1).hasSize(1); // group 1 and skills java -> candidate
        assertThat(pendingUser2).isEmpty(); // no skills java -> not candidate
        assertThat(pendingUser3).hasSize(1); // group 1 and skills java -> candidate
        assertThat(pendingUser4).isEmpty(); // not in group1 -> not candidate 
        
        //cleanup
        deleteUserMemberships(membership1, membership2, membership3, membership4);
    }

    private ProcessDefinition deployAndEnableProcessWithCustomUserInfoFilter(String taskName, String userInfoName, String userInfoValue, Group actorGroup)
            throws Exception {
        String actorName = "employee";
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("My process", "4.0");
        builder.addActor(actorName);
        UserFilterDefinitionBuilder filterBuilder = builder.addUserTask(taskName, actorName).addUserFilter("Only java", "custom-user-info", "1.0.0");
        filterBuilder.addInput("customUserInfoName", new ExpressionBuilder().createConstantStringExpression(userInfoName));
        filterBuilder.addInput("customUserInfoValue", new ExpressionBuilder().createConstantStringExpression(userInfoValue));
        
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.setProcessDefinition(builder.done());
        
        final InputStream inputStream = CustomUserInfoUserFilter.class.getResourceAsStream("/custom-user-info-impl-1.0.0.impl");
        try {
            businessArchiveBuilder.addUserFilters(new BarResource("custom-user-info-impl-1.0.0.impl", IOUtils.toByteArray(inputStream)));
        } finally {
            inputStream.close();
        }
        
        ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        getProcessAPI().addGroupToActor(actorName, actorGroup.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        
        return processDefinition;
    }

}
