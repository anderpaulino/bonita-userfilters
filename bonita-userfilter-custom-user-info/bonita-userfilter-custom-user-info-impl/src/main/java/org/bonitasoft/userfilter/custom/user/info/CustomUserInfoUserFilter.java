/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.userfilter.custom.user.info;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.filter.UserFilterException;

/**
 * @author Elias Ricken de Medeiros
 */
public class CustomUserInfoUserFilter extends AbstractUserFilter {

    private static final String AUTO_ASSIGN_KEY = "autoAssign";

    private static final String CUSTOM_USER_INFO_NAME_KEY = "customUserInfoName";

    private static final String CUSTOM_USER_INFO_VALUE_KEY = "customUserInfoValue";

    private static final String USE_PARTIAL_MATCH_KEY = "usePartialMatch";

    private int maxResults = 500;

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        validateStringInputParameterIsNotNulOrEmpty(CUSTOM_USER_INFO_NAME_KEY);
        validateStringInputParameterIsNotNulOrEmpty(CUSTOM_USER_INFO_VALUE_KEY);
    }

    @Override
    public List<Long> filter(String actorName) throws UserFilterException {
        String infoName = getStringInputParameter(CUSTOM_USER_INFO_NAME_KEY);
        String infoValue = getStringInputParameter(CUSTOM_USER_INFO_VALUE_KEY);
        Boolean usePartialMatch = shouldUsePartialMatch();

        IdentityAPI identityAPI = getAPIAccessor().getIdentityAPI();
        ProcessAPI processAPI = getAPIAccessor().getProcessAPI();
        List<Long> usersWithInfo = getAllUserIdsWithInfo(infoName, infoValue, usePartialMatch, identityAPI);
        List<Long> userIdsForActor = getAllUserIdsForActor(actorName, processAPI);
        
        usersWithInfo.retainAll(userIdsForActor);

        return Collections.unmodifiableList(usersWithInfo);
    }

    protected Boolean shouldUsePartialMatch() {
        Boolean usePartialMatch = getOptinalInputParameter(USE_PARTIAL_MATCH_KEY);
        return usePartialMatch == null? false: usePartialMatch;
    }

    private List<Long> getAllUserIdsForActor(String actorName, ProcessAPI processAPI) {
        PageAssembler<Long> pageAssembler = getPageAssember(new UsersOfActorPageRetriever(processAPI, getExecutionContext().getProcessDefinitionId(), actorName, maxResults));
        return pageAssembler.getAllElements();
    }

    private List<Long> getAllUserIdsWithInfo(String infoName, String infoValue, boolean usePartialMatch, IdentityAPI identityAPI) {
        PageAssembler<Long> pageAssembler = getPageAssember(new UsersWithCustomUserInfoPageRetriever(identityAPI, infoName, infoValue, usePartialMatch, maxResults));
        return pageAssembler.getAllElements();
    }
    
    private <T> PageAssembler<T> getPageAssember(PageRetriever<T> pageRetriver) {
        return new PageAssembler<T>(pageRetriver);
    }

    protected void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
    
    @Override
    public boolean shouldAutoAssignTaskIfSingleResult() {
        Boolean autoAssign = getOptinalInputParameter(AUTO_ASSIGN_KEY);
        return autoAssign == null? true : autoAssign;
    }

}
