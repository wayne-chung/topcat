/**
 * 
 * Copyright (c) 2009-2010
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
package uk.ac.stfc.topcat.gwt.client.widget;

/**
 * Imports
 */
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;

/**
 * This class is a widget. this holds tab panels that has
 * mydatapanel,searchpanel,browsepanel.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class MainPanel extends Composite {
    MyDataPanel myDataPanel;
    MyDownloadPanel myDownloadPanel;
    SearchPanel searchPanel;
    BrowsePanel browsePanel;
    TabPanel tabPanel;
    boolean historyEnabled;

    public MainPanel() {

        tabPanel = new TabPanel();
        tabPanel.setMinTabWidth(60);

        TabItem tbtmMyData = new TabItem("My Data");
        tbtmMyData.setItemId("MyData");
        myDataPanel = new MyDataPanel();
        myDataPanel.setAutoWidth(true);
        myDataPanel.setAutoHeight(true);
        tbtmMyData.add(myDataPanel);
        tabPanel.add(tbtmMyData);
        tbtmMyData.setAutoHeight(true);
        tbtmMyData.setAutoWidth(true);

        TabItem tbtmMyDownload = new TabItem("My Downloads");
        tbtmMyDownload.setItemId("MyDownloads");
        myDownloadPanel = new MyDownloadPanel(tabPanel, tbtmMyDownload);
        myDownloadPanel.setAutoWidth(true);
        myDownloadPanel.setAutoHeight(true);
        tbtmMyDownload.add(myDownloadPanel);
        tabPanel.add(tbtmMyDownload);
        tbtmMyDownload.setAutoHeight(true);
        tbtmMyDownload.setAutoWidth(true);

        TabItem tbtmSearch = new TabItem("Search");
        tbtmSearch.setItemId("Search");
        searchPanel = new SearchPanel();
        searchPanel.setAutoWidth(true);
        searchPanel.setAutoHeight(true);
        tbtmSearch.add(searchPanel);
        tabPanel.add(tbtmSearch);
        tbtmSearch.setAutoHeight(true);
        tbtmSearch.setAutoWidth(true);

        TabItem tbtmBrowse = new TabItem("Browse All Data");
        tbtmBrowse.setItemId("AllData");
        browsePanel = new BrowsePanel();
        tbtmBrowse.add(browsePanel);
        tabPanel.add(tbtmBrowse);
        tbtmBrowse.setAutoHeight(true);
        tbtmBrowse.setAutoWidth(true);

        tabPanel.setWidth("100%");

        initComponent(tabPanel);

        // When the tab is changed update the history with the new tab info
        tabPanel.addListener(Events.Select, new Listener<TabPanelEvent>() {
            @Override
            public void handleEvent(TabPanelEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
                EventPipeLine.getInstance().getHistoryManager().setTabSelected(event.getItem().getItemId());
                if (historyEnabled) {
                    EventPipeLine.getInstance().getHistoryManager().updateHistory();
                }
                historyEnabled = true;
            }
        });

        historyEnabled = false;
    }

    public BrowsePanel getBrowserPanel() {
        return browsePanel;
    }

    public SearchPanel getSearchPanel() {
        return searchPanel;
    }

    public MyDataPanel getMyDataPanel() {
        return myDataPanel;
    }

    public MyDownloadPanel getMyDownloadPanel() {
        return myDownloadPanel;
    }

    public void selectPanelWithoutHistory(String tabPanelId) {
        historyEnabled = false;
        TabItem item = tabPanel.getItemByItemId(tabPanelId);
        if (item == null)
            item = tabPanel.getItem(0);
        tabPanel.setSelection(item);
        historyEnabled = true;
    }
}
