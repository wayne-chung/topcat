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
package uk.ac.stfc.topcat.ejb.manager;

import java.net.MalformedURLException;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.core.icat.ICATWebInterfaceBase;
import uk.ac.stfc.topcat.ejb.entity.TopcatIcatServer;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserSession;

/**
 * This is used to get data prior to file upload.
 */

public class UploadManager {

    private final static Logger logger = Logger.getLogger(UploadManager.class.getName());

    /**
     * Create a new dataset.
     * 
     * @param manager
     * @param topcatSessionId
     * @param dataset
     * @return the dataset id
     * @throws TopcatException
     */
    public Long createDataSet(EntityManager manager, String topcatSessionId, TDataset dataset) throws TopcatException {
        logger.info("createDataSet: topcatSessionId (" + topcatSessionId + "), facilityName (" + dataset.getFacilityName() + ")");
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, dataset.getFacilityName());
            return createDataSet(userSession.getIcatSessionId(), userSession.getUserId().getServerId(), dataset);
        } catch (javax.persistence.NoResultException ex) {
            logger.warn("createDataSet: " + ex.getMessage());
        }
        return null;
    }

    private Long createDataSet(String icatSessionId, TopcatIcatServer server, TDataset dataset) throws TopcatException {
        logger.debug("createDataSet: icatSessionId (" + icatSessionId + "), facilityName (" + dataset.getFacilityName() + ")" + " server (" + server.getName() + ")");
        
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.createDataSet(icatSessionId, dataset);
        } catch (MalformedURLException ex) {
            logger.warn("createDataSet: " + ex.getMessage());
        }
        return null;
    }

}
