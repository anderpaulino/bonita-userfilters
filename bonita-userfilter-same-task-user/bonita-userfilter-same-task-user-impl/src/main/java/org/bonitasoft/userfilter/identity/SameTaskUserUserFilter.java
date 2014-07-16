/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.userfilter.identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.filter.UserFilterException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;

/**
 * Filters the userId(s) to the ones of the user(s) who executed a task specified by its name.
 * If a large number of task instances have the provided name for the running process, only the first 2000 results are returned.
 * 
 * @author Emmanuel Duchastenier
 */
public class SameTaskUserUserFilter extends AbstractUserFilter {

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        validateStringInputParameterIsNotNulOrEmpty("usertaskName");
    }

    @Override
    public List<Long> filter(final String actorName) throws UserFilterException {
        final String usertaskName = (String) getInputParameter("usertaskName");
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 2000)
                .filter(ArchivedHumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID, getExecutionContext().getProcessInstanceId())
                .filter(ArchivedHumanTaskInstanceSearchDescriptor.NAME, usertaskName).filter(ArchivedHumanTaskInstanceSearchDescriptor.TERMINAL, true);
        SearchResult<ArchivedHumanTaskInstance> searchResult;
        try {
            searchResult = getAPIAccessor().getProcessAPI().searchArchivedHumanTasks(searchOptionsBuilder.done());
        } catch (SearchException e) {
            throw new UserFilterException("Problem searching for task named: " + usertaskName, e);
        }
        if (searchResult.getCount() == 0) {
            throw new UserFilterException("No finished task found with name: " + usertaskName);
        }
        final List<ArchivedHumanTaskInstance> tasks = searchResult.getResult();
        List<Long> userIds = new ArrayList<Long>(tasks.size());
        for (ArchivedHumanTaskInstance archivedTask : tasks) {
            userIds.add(archivedTask.getExecutedBy());
        }
        return Collections.unmodifiableList(userIds);
    }
}
