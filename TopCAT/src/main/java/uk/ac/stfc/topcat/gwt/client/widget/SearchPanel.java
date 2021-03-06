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
package uk.ac.stfc.topcat.gwt.client.widget;

/**
 * Imports
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.gwt.client.KeywordsSuggestOracle;
import uk.ac.stfc.topcat.gwt.client.Resource;
import uk.ac.stfc.topcat.gwt.client.autocompletewidget.MultipleTextBox;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.callback.InvestigationSearchCallback;
import uk.ac.stfc.topcat.gwt.client.event.AddInvestigationDetailsEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.event.SearchAllButtonEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.AddInvestigationDetailsEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.model.TopcatInvestigation;
import uk.ac.stfc.topcat.gwt.shared.DataSelectionType;
import uk.ac.stfc.topcat.gwt.shared.model.TopcatDataSelection;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;

/**
 * This is a composite widget for Search Panel used in the top level tab.
 * <p>
 *
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class SearchPanel extends Composite implements InvestigationSearchCallback {

    private VerticalPanel topPanel = new VerticalPanel();
    private KeywordsSuggestOracle oracle;
    private MultipleTextBox multipleTextBox;
    private AdvancedSearchSubPanel advancedSearchSubPanel;
    private ParameterSearchSubPanel parameterSearchSubPanel;
    private FacilitiesSearchSubPanel facilitiesSearchSubPanel;
    private FreeTextSearchSubPanel freeTextSearchSubPanel;
    private InvestigationPanel investigationPanel;

    // Radio button for type of search
    RadioButton rdbtnSearchJustMy; // Search just my data
    RadioButton rdbtnSearchAllData; // Search All data

    WaitDialog waitDialog;
    private ListStore<TopcatInvestigation> investigationList = null;
    PagingModelMemoryProxy invPageProxy = null;
    PagingToolBar toolBar = null;
    Grid<TopcatInvestigation> grid;
    private EventPipeLine eventBus;
    private static final String SOURCE = "SearchPanel";

    public SearchPanel() {
        LayoutContainer mainContainer = new LayoutContainer();
        mainContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeaderVisible(false);
        contentPanel.setCollapsible(true);
        contentPanel.setLayout(new RowLayout(Orientation.VERTICAL));

        waitDialog = new WaitDialog();
        waitDialog.setMessage(" Searching...");
        waitDialog.hide();
        oracle = new KeywordsSuggestOracle();

        topPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
        topPanel.setBorders(true);
        contentPanel.setTopComponent(topPanel);
        topPanel.add(getFlexTable());

        // Advanced Search Panel
        TableData td_cntntpnlAdvancedSearch = new TableData();
        td_cntntpnlAdvancedSearch.setHeight("100%");
        td_cntntpnlAdvancedSearch.setWidth("705px");
        topPanel.add(getAdvancedSearchPanel(), td_cntntpnlAdvancedSearch);

        // Parameter Search Panel
        TableData td_cntntpnlParameterSearch = new TableData();
        td_cntntpnlParameterSearch.setHeight("100%");
        td_cntntpnlParameterSearch.setWidth("705px");
        topPanel.add(getParameterSearchPanel(), td_cntntpnlParameterSearch);

        // Facilities Search Panel
        TableData td_cntntpnlFacilitiesSearch = new TableData();
        td_cntntpnlFacilitiesSearch.setHeight("100%");
        td_cntntpnlFacilitiesSearch.setWidth("705px");
        topPanel.add(getFacilitiesSearchPanel(), td_cntntpnlFacilitiesSearch);

        // Free Text Search Panel
        TableData td_cntntpnlFreeTextSearch = new TableData();
        td_cntntpnlFreeTextSearch.setHeight("100%");
        td_cntntpnlFreeTextSearch.setWidth("705px");
        topPanel.add(getFreeTextSearchPanel(), td_cntntpnlFreeTextSearch);
        topPanel.add(new Text(""));


        // Pagination
        invPageProxy = new PagingModelMemoryProxy(investigationList);
        PagingLoader<PagingLoadResult<TopcatInvestigation>> loader = new BasePagingLoader<PagingLoadResult<TopcatInvestigation>>(
                invPageProxy);
        loader.setRemoteSort(true);
        investigationList = new ListStore<TopcatInvestigation>(loader);

        contentPanel.add(getResultsPanel(), new RowData(Style.DEFAULT, 376.0, new Margins()));

        // Pagination Bar
        toolBar = getToolBar();
        toolBar.bind(loader);
        contentPanel.setBottomComponent(toolBar);
        toolBar.refresh();

        mainContainer.add(contentPanel);

        // Investigation detail
        investigationPanel = new InvestigationPanel(SOURCE);
        investigationPanel.hide();
        mainContainer.add(investigationPanel);

        initComponent(mainContainer);
        setMonitorWindowResize(true);

        createAddInvestigationDetailsHandler();
        createLogoutHandler();
    }

    /**
     * @return the advanced search sub panel
     */
    public AdvancedSearchSubPanel getAdvancedSearchSubPanel() {
        return advancedSearchSubPanel;
    }

    /**
     * @return the facilities search sub panel
     */
    public FacilitiesSearchSubPanel getFacilitiesSearchSubPanel() {
        return facilitiesSearchSubPanel;
    }

    /**
     * @return the free text search sub panel
     */
    public FreeTextSearchSubPanel getFreeTextSearchSubPanel() {
        return freeTextSearchSubPanel;
    }


    /**
     * This method is an callback for searching just user investigation using
     * basic keywords.
     */
    public void doSearchJustMyData() {
        TAdvancedSearchDetails searchDetails = new TAdvancedSearchDetails();
        searchDetails.setKeywords(multipleTextBox.getMultiText());
        eventBus.searchForMyInvestigation(searchDetails);
    }

    /**
     * This method is an callback for searches for all investigations using
     * basic keywords.
     */
    public void doSearchAllData() {
        TAdvancedSearchDetails searchDetails = new TAdvancedSearchDetails();
        searchDetails.setKeywords(multipleTextBox.getMultiText());
        eventBus.searchForInvestigation(searchDetails);
    }

    public void setEventBus(EventPipeLine eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * This method is the callback for the searching for investigations with the
     * input search parameters. Submits an AJAX call. on success the table is
     * set with the results.
     */
    @Override
    public void searchForInvestigation(TAdvancedSearchDetails searchDetails) {
        // This method does the search and add the results to the investigation
        // list
        // Add keywords to the search
        searchDetails.setKeywords(multipleTextBox.getMultiText());
        eventBus.searchForInvestigation(searchDetails);
    }

    /**
     * This method sets the result investigations that will be displayed in the
     * results table.
     *
     * @param invList
     *            list of investigations
     */
    public void setInvestigations(ArrayList<TopcatInvestigation> invList) {
        invPageProxy.setData(invList);
        toolBar.refresh();
        investigationPanel.hide();
        investigationPanel.reset();
    }

    /**
     * This method sets the width of the search results table.
     *
     * @param width
     */
    public void setGridWidth(int width) {
        grid.setWidth(width);
    }

    /**
     * Set up a flex table containing the keyword selection and my data / all
     * data button.
     *
     * @return a felex table
     */
    private FlexTable getFlexTable() {
        FlexTable flexTable = new FlexTable();
        flexTable.setCellSpacing(8);
        flexTable.setCellPadding(2);
        flexTable.setHeight("20%");

        LabelField lblfldEywords = new LabelField("Keywords");
        flexTable.setWidget(0, 0, lblfldEywords);

        multipleTextBox = new MultipleTextBox();
        multipleTextBox.setWidth("361px");
        SuggestBox keywords = new SuggestBox(oracle, multipleTextBox);
        flexTable.setWidget(0, 1, keywords);

        Button btnSearch = new Button("Search");
        btnSearch.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                // Check whether to do searching my data or all data
                if (rdbtnSearchAllData.getValue()) {
                    // Search all data
                    doSearchAllData();
                } else if (rdbtnSearchJustMy.getValue()) {
                    // Search just my data
                    doSearchJustMyData();
                }
            }
        });
        flexTable.setWidget(0, 2, btnSearch);

        rdbtnSearchJustMy = new RadioButton("new name", "Search Just My Data");
        rdbtnSearchJustMy.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                EventPipeLine.getEventBus().fireEvent(new SearchAllButtonEvent(false));
            }
        });
        rdbtnSearchJustMy.setValue(true);
        rdbtnSearchJustMy.setHTML(" Search Just My Data");
        flexTable.setWidget(1, 0, rdbtnSearchJustMy);

        rdbtnSearchAllData = new RadioButton("new name", " Search All Data");
        rdbtnSearchAllData.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                EventPipeLine.getEventBus().fireEvent(new SearchAllButtonEvent(true));
            }
        });

        flexTable.setWidget(1, 1, rdbtnSearchAllData);
        flexTable.getFlexCellFormatter().setColSpan(1, 1, 2);
        flexTable.getFlexCellFormatter().setColSpan(1, 0, 2);
        flexTable.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
        flexTable.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
        flexTable.getFlexCellFormatter().setColSpan(0, 1, 2);
        return flexTable;
    }

    /**
     * Set up a ContentPanel containing the advancedSearchSubPanel.
     *
     * @return a ContentPanel containing the advancedSearchSubPanel
     */
    private ContentPanel getAdvancedSearchPanel() {
        ContentPanel cp = new ContentPanel();
        cp.setTitleCollapse(true);
        cp.setFrame(true);
        cp.setExpanded(false);
        cp.setHeadingText("Advanced Search");
        cp.setCollapsible(true);
        cp.addListener(Events.Expand, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        cp.addListener(Events.Collapse, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        advancedSearchSubPanel = new AdvancedSearchSubPanel();
        advancedSearchSubPanel.setInvSearchCallback(this);
        cp.add(advancedSearchSubPanel);
        return cp;
    }

    /**
     * Set up a ContentPanel containing the parameterSearchSubPanel.
     *
     * @return a ContentPanel containing the parameterSearchSubPanel
     */
    private ContentPanel getParameterSearchPanel() {
        ContentPanel cp = new ContentPanel();
        cp.setTitleCollapse(true);
        cp.setFrame(true);
        cp.setExpanded(false);
        cp.setHeadingText("Parameter Search");
        cp.setCollapsible(true);
        cp.addListener(Events.Expand, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        cp.addListener(Events.Collapse, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        parameterSearchSubPanel = new ParameterSearchSubPanel();
        cp.add(parameterSearchSubPanel);
        return cp;
    }

    /**
     * Set up a ContentPanel containing the facilitiesSearchSubPanel.
     *
     * @return a ContentPanel containing the facilitiesSearchSubPanel
     */
    private ContentPanel getFacilitiesSearchPanel() {
        ContentPanel cp = new ContentPanel();
        cp.setTitleCollapse(true);
        cp.setExpanded(false);
        cp.setFrame(true);
        cp.setHeadingText("Facilities Search");
        cp.setCollapsible(true);
        cp.addListener(Events.Expand, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        cp.addListener(Events.Collapse, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        facilitiesSearchSubPanel = new FacilitiesSearchSubPanel();
        cp.add(facilitiesSearchSubPanel);
        return cp;
    }



    /**
     * Set up a ContentPanel containing the freeTextSearchSubPanel.
     *
     * @return a ContentPanel containing the freeTextSearchSubPanel
     */
    private ContentPanel getFreeTextSearchPanel() {
        ContentPanel cp = new ContentPanel();
        cp.setTitleCollapse(true);
        cp.setFrame(true);
        cp.setExpanded(false);
        cp.setHeadingText("Free Text Search");
        cp.setCollapsible(true);
        cp.addListener(Events.Expand, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        cp.addListener(Events.Collapse, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        freeTextSearchSubPanel = new FreeTextSearchSubPanel();
        cp.add(freeTextSearchSubPanel);
        return cp;
    }





    /**
     * Get a panel containing a grid for the search results.
     *
     * @return results panel
     */
    private VerticalPanel getResultsPanel() {
        grid = new Grid<TopcatInvestigation>(investigationList, new ColumnModel(getColumnConfigs()));
        grid.setAutoExpandColumn("title");
        grid.setAutoExpandMin(200);
        grid.setMinColumnWidth(100);
        grid.setToolTip("\"Click\" row to show investigation, \"Double Click\" to show datasets, right click for more options");
        // single click
        grid.addListener(Events.RowClick, new Listener<GridEvent<TopcatInvestigation>>() {
            @Override
            public void handleEvent(GridEvent<TopcatInvestigation> e) {
                eventBus.getInvestigationDetails(e.getModel().getFacilityName(), e.getModel().getInvestigationId(),
                        SOURCE);
            }
        });
        // double click
        grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<TopcatInvestigation>>() {
            @Override
            public void handleEvent(GridEvent<TopcatInvestigation> e) {
                TopcatInvestigation inv = e.getModel();
                eventBus.showDatasetWindowWithHistory(inv.getFacilityName(), inv.getInvestigationId(),
                        inv.getInvestigationTitle());
            }
        });

        grid.setContextMenu(getMenu());
        grid.setAutoHeight(true);
        VerticalPanel bodyPanel = new VerticalPanel();
        bodyPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
        bodyPanel.add(grid);
        return bodyPanel;
    }

    /**
     * Get the list of column configs.
     *
     * @return a list of ColumnConfig
     */
    private List<ColumnConfig> getColumnConfigs() {
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig clmncnfgServerName = new ColumnConfig("facilityName", "Facility Name", 150);
        configs.add(clmncnfgServerName);

        ColumnConfig clmncnfgInvestigationNumber = new ColumnConfig("investigationName", "Investigation Number", 150);
        configs.add(clmncnfgInvestigationNumber);

        ColumnConfig clmncnfgVisitId = new ColumnConfig("visitId", "Visit Id", 150);
        configs.add(clmncnfgVisitId);

        ColumnConfig clmncnfgTitle = new ColumnConfig("title", "Title", 150);
        configs.add(clmncnfgTitle);

        ColumnConfig clmncnfgStartDate = new ColumnConfig("startDate", "Start Date", 150);
        clmncnfgStartDate.setDateTimeFormat(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT));
        configs.add(clmncnfgStartDate);

        ColumnConfig clmncnfgEndDate = new ColumnConfig("endDate", "End Date", 150);
        clmncnfgEndDate.setDateTimeFormat(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT));
        configs.add(clmncnfgEndDate);
        return configs;
    }

    /**
     * Get the context menu.
     *
     * @return the context menu
     */
    private Menu getMenu() {
        Menu contextMenu = new Menu();
        contextMenu.setWidth(180);
        contextMenu.addStyleName("context-menu");
        MenuItem showInvestigation = new MenuItem();
        showInvestigation.setText("Show Investigation Details");
        showInvestigation.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconShowInvestigationDetails()));
        contextMenu.add(showInvestigation);
        showInvestigation.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                eventBus.getInvestigationDetails(grid.getSelectionModel().getSelectedItem().getFacilityName(), grid
                        .getSelectionModel().getSelectedItem().getInvestigationId(), SOURCE);
            }
        });


        MenuItem showSize = new MenuItem();
        showSize.setText("Show Investigation Size");
        showSize.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconFileSize()));

        contextMenu.add(showSize);
        showSize.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                if (grid.getSelectionModel().getSelectedItem() != null) {
                    TopcatInvestigation inv = (TopcatInvestigation) grid.getSelectionModel().getSelectedItem();
                    TopcatDataSelection topcatDataSelection = new TopcatDataSelection();
                    topcatDataSelection.addInvestigation(new Long(inv.getInvestigationId()));

                    EventPipeLine.getInstance().showDataSelectionSizeDialog(inv.getFacilityName(), topcatDataSelection, DataSelectionType.INVESTIGATION);
                }
            }
        });

        MenuItem showDS = new MenuItem();
        showDS.setText("Show Data Sets");
        showDS.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconOpenDataset()));
        contextMenu.add(showDS);
        showDS.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                eventBus.showDatasetWindowWithHistory(grid.getSelectionModel().getSelectedItem().getFacilityName(),
                        grid.getSelectionModel().getSelectedItem().getInvestigationId(), grid.getSelectionModel()
                                .getSelectedItem().getInvestigationTitle());
            }
        });
        return contextMenu;
    }

    /**
     * Get the paging tool bar.
     */
    private PagingToolBar getToolBar() {
        PagingToolBar toolBar = new PagingToolBar(15) {
            @Override
            public void refresh() {
                super.refresh();
                refresh.hide();
            }
        };
        return toolBar;
    }

    /**
     * Setup a handler to react to add investigation details events.
     */
    private void createAddInvestigationDetailsHandler() {
        AddInvestigationDetailsEvent.registerToSource(EventPipeLine.getEventBus(), SOURCE,
                new AddInvestigationDetailsEventHandler() {
                    @Override
                    public void addInvestigationDetails(AddInvestigationDetailsEvent event) {
                        investigationPanel.show();
                        investigationPanel.setInvestigation(event.getInvestigation());
                    }
                });
    }

    /**
     * Setup a handler to react to Logout events.
     */
    private void createLogoutHandler() {
        LogoutEvent.register(EventPipeLine.getEventBus(), new LogoutEventHandler() {
            @Override
            public void logout(LogoutEvent event) {
                clearInvestigationList(event.getFacilityName());
                String invDetailsFacility = investigationPanel.getFacilityName();
                if (!(invDetailsFacility == null) && invDetailsFacility.equalsIgnoreCase(event.getFacilityName())) {
                    investigationPanel.hide();
                    investigationPanel.reset();
                }
            }
        });
    }

    /**
     * Remove all investigations for the given facility.
     *
     * @param facilityName
     */
    private void clearInvestigationList(String facilityName) {
        @SuppressWarnings("unchecked")
        List<TopcatInvestigation> investList = (List<TopcatInvestigation>) invPageProxy.getData();
        if (investList != null) {
            for (Iterator<TopcatInvestigation> it = investList.iterator(); it.hasNext();) {
                if (it.next().getFacilityName().equals(facilityName)) {
                    it.remove();
                }
            }
            invPageProxy.setData(investList);
            toolBar.refresh();
        }
    }
}
