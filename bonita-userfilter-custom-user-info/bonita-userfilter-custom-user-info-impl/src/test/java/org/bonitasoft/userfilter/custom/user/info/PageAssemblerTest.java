package org.bonitasoft.userfilter.custom.user.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class PageAssemblerTest {
   
    @Mock
    private PageRetriever<Long> retriever;
    
    @InjectMocks
    private PageAssembler<Long> assembler;

    @SuppressWarnings("unchecked")
    @Test
    public void getAllElementsShouldIterateAllPages() throws Exception {
        //given
        when(retriever.nextPage()).thenReturn(Arrays.asList(1L, 2L, 3L), Arrays.asList(4L, 5L));
        when(retriever.getMaxPageSize()).thenReturn(3);
        
        //when
        List<Long> elements = assembler.getAllElements();

        //then
        assertThat(elements).containsExactly(1L, 2L, 3L, 4L, 5L);
    }
    
}
