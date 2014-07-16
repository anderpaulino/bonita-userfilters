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

import java.util.List;



/**
 * @author Elias Ricken de Medeiros
 *
 */
public abstract class PageRetriever<T> {
    
    private int maxPageSize;
    
    private int startIndex = 0;

    
    public PageRetriever(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }
    
    protected abstract List<T> getCurrentPage();
    
    public List<T> nextPage() {
        List<T> page = getCurrentPage();
        startIndex += maxPageSize;
        return page;
    }
    
    public int getMaxPageSize() {
        return maxPageSize;
    }
    
    public int getStartIndex() {
        return startIndex;
    }

}
