/**
 *
 * Copyright (c) 2009-2013
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the distribution.
 * Neither the name of the STFC nor the names of its contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package uk.ac.stfc.topcat.ejb.session;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;

/**
 * This is local interface to the search management stateless session bean
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */

@Local
public interface SearchManagementBeanLocal {
    public ArrayList<TInvestigation> searchBasicInvestigationByKeywords(String topcatSessionId,
            ArrayList<String> keywords) throws TopcatException;

    public ArrayList<TInvestigation> searchBasicInvestigationByKeywordsInServer(String topcatSessionId,
            String facilityName, ArrayList<String> keywords) throws TopcatException;

    public ArrayList<TInvestigation> searchBasicMyInvestigationByKeywords(String topcatSessionId, List<String> keywords)
            throws TopcatException;

    public ArrayList<TInvestigation> searchBasicMyInvestigationByKeywordsInServer(String topcatSessionId,
            String facilityName, ArrayList<String> keywords) throws TopcatException;

    public ArrayList<TInvestigation> searchAdvancedInvestigation(String topcatSessionId,
            TAdvancedSearchDetails searchDetails) throws TopcatException;
    
    public ArrayList<TInvestigation> searchFreeTextInvestigation(String topcatSessionId,
            TAdvancedSearchDetails searchDetails) throws TopcatException;

    public ArrayList<TInvestigation> searchAdvancedInvestigationInServer(String topcatSessionId, String facilityName,
            TAdvancedSearchDetails searchDetails) throws TopcatException;

    public ArrayList<TDataset> searchForDatasetsByParameter(String topcatSessionId, String facilityName,
            TAdvancedSearchDetails searchDetails) throws TopcatException;

    public ArrayList<TDatafile> searchAdvancedDatafileInServer(String topcatSessionId, String facilityName,
            TAdvancedSearchDetails searchDetails) throws TopcatException;
    

}
