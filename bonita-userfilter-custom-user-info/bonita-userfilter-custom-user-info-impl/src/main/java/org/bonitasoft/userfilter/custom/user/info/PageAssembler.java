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

import java.util.ArrayList;
import java.util.List;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class PageAssembler<T> {
    
    
    private PageRetriever<T> pageRetriever;

    public PageAssembler(PageRetriever<T> pageRetriever) {
        this.pageRetriever = pageRetriever;
    }
    

    public List<T> getAllElements() {
        List<T> allElements = new ArrayList<T>();
        List<T> page = null;
        int maxResults = pageRetriever.getMaxPageSize();
        do {
            page = pageRetriever.nextPage();
            allElements.addAll(page);
        } while (page.size() == maxResults);
        return allElements;
    }
    
    
}
