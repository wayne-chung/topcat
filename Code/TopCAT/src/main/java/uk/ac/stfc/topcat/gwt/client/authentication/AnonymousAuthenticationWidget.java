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
package uk.ac.stfc.topcat.gwt.client.authentication;

/**
 * Imports
 */
import java.util.HashMap;
import java.util.Map;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.gwt.client.LoginInterface;
import uk.ac.stfc.topcat.gwt.client.model.AuthenticationModel;

import com.extjs.gxt.ui.client.widget.Composite;

/**
 * This is an authentication widget that uses the anonymous user name and
 * password.
 */
public class AnonymousAuthenticationWidget extends Composite {
    private AuthenticationModel authenticationModel;
    private TFacility facility = null;
    private LoginInterface loginHandler = null;

    public AnonymousAuthenticationWidget() {
    }

    public void setAuthenticationModel(AuthenticationModel authenticationModel) {
        this.authenticationModel = authenticationModel;
    }

    public void setFacility(TFacility facility) {
        this.facility = facility;
    }

    public void setLoginHandler(LoginInterface loginHandler) {
        this.loginHandler = loginHandler;
    }

    public void authenticate() {
        if (facility != null && loginHandler != null) {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("username", facility.getDefaultUser());
            parameters.put("password", facility.getDefaultPassword());
            // TODO remove hard coded db
            loginHandler.onLoginOk(authenticationModel.getFacilityName(), "db", parameters);
        }
    }
}
