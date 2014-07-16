package org.bonitasoft.userfilter.custom.user.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.IdentityAPI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UsersWithCustomUserInfoPageRetrieverTest {
    
    @Mock
    private IdentityAPI identityAPI;
    
    private UsersWithCustomUserInfoPageRetriever retriever;

    private static final int MAX_PAGE_SIZE = 2;
    
    private static final String CUSTOM_USER_INFO_NAME = "skills";

    private static final String CUSTOM_USER_INFO_VALUE = "java";
    
    @Before
    public void setUp() throws Exception {
        retriever = new UsersWithCustomUserInfoPageRetriever(identityAPI, CUSTOM_USER_INFO_NAME, CUSTOM_USER_INFO_VALUE, false, MAX_PAGE_SIZE );
    }
    
    @Test
    public void getMaxPageSize_should_return_the_max_page_size_supplied_in_the_constructor() throws Exception {
        //when
        int maxPageSize = retriever.getMaxPageSize();

        //then
        assertThat(maxPageSize).isEqualTo(MAX_PAGE_SIZE);
    }
    
    
    @Test
    public void nextPage_should_retrive_the_next_available_page_and_move_pointer_to_the_following_one() throws Exception {
        //given
        when(identityAPI.getUserIdsWithCustomUserInfo(CUSTOM_USER_INFO_NAME, CUSTOM_USER_INFO_VALUE, false, 0, MAX_PAGE_SIZE)).thenReturn(Arrays.asList(1L, 2L));
        when(identityAPI.getUserIdsWithCustomUserInfo(CUSTOM_USER_INFO_NAME, CUSTOM_USER_INFO_VALUE, false, MAX_PAGE_SIZE, MAX_PAGE_SIZE)).thenReturn(Arrays.asList(3L, 4L));
        when(identityAPI.getUserIdsWithCustomUserInfo(CUSTOM_USER_INFO_NAME, CUSTOM_USER_INFO_VALUE, false, 2 * MAX_PAGE_SIZE, MAX_PAGE_SIZE)).thenReturn(Arrays.asList(5L));
        when(identityAPI.getUserIdsWithCustomUserInfo(CUSTOM_USER_INFO_NAME, CUSTOM_USER_INFO_VALUE, false, 3 * MAX_PAGE_SIZE, MAX_PAGE_SIZE)).thenReturn(Collections.<Long>emptyList());
        
        //when
        List<Long> page1 = retriever.nextPage();
        List<Long> page2 = retriever.nextPage();
        List<Long> page3 = retriever.nextPage();
        List<Long> page4 = retriever.nextPage();

        //then
        assertThat(page1).containsExactly(1L, 2L);
        assertThat(page2).containsExactly(3L, 4L);
        assertThat(page3).containsExactly(5L);
        assertThat(page4).isEmpty();
    }

}
