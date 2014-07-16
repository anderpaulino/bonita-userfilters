package org.bonitasoft.userfilter.custom.user.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomUserInfoUserFilterTest {

    private static final String INFO_NAME_KEY = "customUserInfoName";

    private static final String INFO_VALUE_KEY = "customUserInfoValue";
    
    private static final String USE_PARTIAL_MATCH_KEY = "usePartialMatch";
    
    private static final String AUTO_ASSIGN_KEY = "autoAssign";

    private static final int MAX_RESULTS = 3;

    private static final String ACTOR_NAME = "employee";

    private static final String INFO_NAME = "skills";

    private static final String INFO_VALUE = "java";

    private static final long PROCESS_DEFINITION_ID = 10;

    @Mock
    private APIAccessor accessor;

    @Mock
    private IdentityAPI identityAPI;

    @Mock
    private ProcessAPI processAPI;

    private CustomUserInfoUserFilter filter;

    @Before
    public void setUp() throws Exception {
        filter = new CustomUserInfoUserFilter();
        filter.setAPIAccessor(accessor);
        filter.setMaxResults(3);
        EngineExecutionContext executionContext = new EngineExecutionContext();
        executionContext.setProcessDefinitionId(PROCESS_DEFINITION_ID);
        filter.setExecutionContext(executionContext);

        Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put(INFO_NAME_KEY, INFO_NAME);
        parameters.put(INFO_VALUE_KEY, INFO_VALUE);
        filter.setInputParameters(parameters);

        when(accessor.getIdentityAPI()).thenReturn(identityAPI);
        when(accessor.getProcessAPI()).thenReturn(processAPI);
    }

    @Test
    public void validateInputParameters_throws_IllegalStateException_if_customUserInfoName_is_not_set() throws Exception {
        // given
        // only value is set
        filter = new CustomUserInfoUserFilter();
        filter.setInputParameters(Collections.<String, Object> singletonMap(INFO_VALUE_KEY, INFO_VALUE));

        // when
        try {
            filter.validateInputParameters();
            fail("Exception excpected");
        } catch (IllegalStateException e) {
            // then ok
        }
    }

    @Test
    public void validateInputParameters_throws_ConnectorValidationException_if_customUserInfoName_is_set_to_null_or_empty() throws Exception {
        // given
        // only value has valid value
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(INFO_NAME_KEY, null);
        parameters.put(INFO_VALUE_KEY, INFO_VALUE);
        filter.setInputParameters(parameters);

        // when
        try {
            filter.validateInputParameters();
            fail("Exception excpected");
        } catch (ConnectorValidationException e) {
            // then ok
        }
    }

    @Test
    public void validateInputParameters_throws_IllegalStateException_if_customUserInfoValue_is_not_set() throws Exception {
        // given
        // only name is set
        filter = new CustomUserInfoUserFilter();
        filter.setInputParameters(Collections.<String, Object> singletonMap(INFO_NAME_KEY, INFO_NAME));

        // when
        try {
            filter.validateInputParameters();
            fail("Exception excpected");
        } catch (IllegalStateException e) {
            // then ok
        }
    }

    @Test
    public void validateInputParameters_throws_ConnectorValidationException_if_customUserInfoValue_is_set_to_null_or_empty() throws Exception {
        // given
        // only name has valid value
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(INFO_NAME_KEY, INFO_NAME);
        parameters.put(INFO_VALUE_KEY, null);
        filter.setInputParameters(parameters);

        try {
            // when
            filter.validateInputParameters();
            fail("Exception excpected");
        } catch (ConnectorValidationException e) {
            // then ok
        }
    }

    @Test
    public void filter_should_return_all_users_with_given_user_info_when_all_users_are_actors() throws Exception {
        // given
        List<Long> usersWithInfo = Arrays.asList(1L, 2L);
        List<Long> usersOfActor = Arrays.asList(1L, 2L, 3L);
        when(identityAPI.getUserIdsWithCustomUserInfo(INFO_NAME, INFO_VALUE, false, 0, MAX_RESULTS)).thenReturn(usersWithInfo);
        when(processAPI.getUserIdsForActor(PROCESS_DEFINITION_ID, ACTOR_NAME, 0, MAX_RESULTS)).thenReturn(usersOfActor);

        // when
        List<Long> filteredUsers = filter.filter(ACTOR_NAME);

        // then
        assertThat(filteredUsers).containsExactlyElementsOf(usersWithInfo);
    }

    @Test
    public void filter_should_return_only_users_with_given_user_info_and_actor() throws Exception {
        // given
        List<Long> usersWithInfo = Arrays.asList(1L, 2L, 3L, 7L);
        List<Long> usersOfActor = Arrays.asList(2L, 3L, 8L);
        when(identityAPI.getUserIdsWithCustomUserInfo(INFO_NAME, INFO_VALUE, false, 0, MAX_RESULTS)).thenReturn(usersWithInfo);
        when(processAPI.getUserIdsForActor(PROCESS_DEFINITION_ID, ACTOR_NAME, 0, MAX_RESULTS)).thenReturn(usersOfActor);

        // when
        List<Long> filteredUsers = filter.filter(ACTOR_NAME);

        // then
        assertThat(filteredUsers).containsExactly(2L, 3L);
    }
    
    
    @Test
    public void shoulAutoAssign_should_return_true_if_property_autoAssign_is_not_set() throws Exception {
        //when
        boolean autoAssign = filter.shouldAutoAssignTaskIfSingleResult();

        //then
        assertThat(autoAssign).isTrue();
    }

    @Test
    public void shoulAutoAssign_should_return_false_if_property_autoAssign_is_set_to_false() throws Exception {
        //given
        filter.setInputParameters(Collections.<String, Object>singletonMap(AUTO_ASSIGN_KEY, false));
        
        //when
        boolean autoAssign = filter.shouldAutoAssignTaskIfSingleResult();
        
        //then
        assertThat(autoAssign).isFalse();
    }

    @Test
    public void shoulAutoAssign_should_return_true_if_property_autoAssign_is_set_to_true() throws Exception {
        //given
        filter.setInputParameters(Collections.<String, Object>singletonMap(AUTO_ASSIGN_KEY, true));
        
        //when
        boolean autoAssign = filter.shouldAutoAssignTaskIfSingleResult();
        
        //then
        assertThat(autoAssign).isTrue();
    }
    
    
    @Test
    public void shouldUsePartialMatch_should_return_false_if_key_userPartialMatch_is_not_set() throws Exception {
        //when
        Boolean usePartialMatch = filter.shouldUsePartialMatch();

        //then
        assertThat(usePartialMatch).isFalse();
    }

    @Test
    public void shouldUsePartialMatch_should_return_true_if_key_userPartialMatch_is_set_to_true() throws Exception {
        filter.setInputParameters(Collections.<String, Object>singletonMap(USE_PARTIAL_MATCH_KEY, true));
        //when
        Boolean usePartialMatch = filter.shouldUsePartialMatch();
        
        //then
        assertThat(usePartialMatch).isTrue();
    }

    @Test
    public void shouldUsePartialMatch_should_return_false_if_key_userPartialMatch_is_set_to_false() throws Exception {
        filter.setInputParameters(Collections.<String, Object>singletonMap(USE_PARTIAL_MATCH_KEY, false));
        //when
        Boolean usePartialMatch = filter.shouldUsePartialMatch();
        
        //then
        assertThat(usePartialMatch).isFalse();
    }

    @Test
    public void shouldUsePartialMatch_should_return_false_if_key_userPartialMatch_is_set_to_null() throws Exception {
        filter.setInputParameters(Collections.<String, Object>singletonMap(USE_PARTIAL_MATCH_KEY, null));
        //when
        Boolean usePartialMatch = filter.shouldUsePartialMatch();
        
        //then
        assertThat(usePartialMatch).isFalse();
    }

}
