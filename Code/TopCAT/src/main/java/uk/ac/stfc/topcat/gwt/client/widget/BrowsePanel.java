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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.stfc.topcat.core.gwt.module.exception.InternalException;
import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.Resource;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.DownloadButtonEvent;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.AddInvestigationDetailsEvent;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.AddInvestigationDetailsEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.manager.DownloadManager;
import uk.ac.stfc.topcat.gwt.client.model.ICATNode;
import uk.ac.stfc.topcat.gwt.client.model.ICATNodeType;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.CheckCascade;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * This widget shows a tree to browse All data. The hierarchy is:
 * <dl>
 * <dd>-- Facility</dd>
 * <dd>&nbsp;&nbsp;-- Instrument</dd>
 * <dd>&nbsp;&nbsp;&nbsp;&nbsp;-- Investigation</dd>
 * <dd>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-- Dataset or Datafile</dd>
 * <dd>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-- Datafile</dd>
 * </dl>
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class BrowsePanel extends Composite {

    private final UtilityServiceAsync utilityService = UtilityService.Util.getInstance();
    private BaseTreeLoader<ICATNode> loader;
    TreePanel<ICATNode> tree;
    HashMap<String, ArrayList<ICATNode>> logfilesMap = new HashMap<String, ArrayList<ICATNode>>();
    private InvestigationPanel investigationPanel;
    private static final String SOURCE = "BrowsePanel";
    private static Logger rootLogger = Logger.getLogger("");

    public BrowsePanel() {

        LayoutContainer mainContainer = new LayoutContainer();
        mainContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeaderVisible(false);
        contentPanel.setCollapsible(true);
        contentPanel.setLayout(new RowLayout(Orientation.VERTICAL));
        contentPanel.setTopComponent(getToolBar());

        // Add Treepanel
        RpcProxy<ArrayList<ICATNode>> proxy = getProxy();
        
        loader = getLoader(proxy);
        TreeStore<ICATNode> store = new TreeStore<ICATNode>(loader);
        tree = new TreePanel<ICATNode>(store);

        // Add tree listeners and menu
        addExpandListener();
        addBeforeCheckChangeListener();
        addSingleClickListener();
        addDoubleClickListener();
        addChangeListener(); //TODO
        addContextMenu();

        tree.setCaching(true);
        tree.setDisplayProperty("name");
        tree.setCheckable(true);
        tree.setCheckStyle(CheckCascade.CHILDREN);
        tree.setAutoHeight(true);

        VerticalPanel bodyPanel = new VerticalPanel();
        bodyPanel.add(tree);
        bodyPanel.setLayoutOnChange(true);
        bodyPanel.setScrollMode(Scroll.AUTO);
        bodyPanel.setHeight("600px");
        VBoxLayout layout = new VBoxLayout();
        layout.setPadding(new Padding(10));
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);
        bodyPanel.setLayout(layout);
        contentPanel.add(bodyPanel);

        mainContainer.add(contentPanel);

        // Investigation detail
        investigationPanel = new InvestigationPanel(SOURCE);
        investigationPanel.hide();
        mainContainer.add(investigationPanel);

        initComponent(mainContainer);
        setMonitorWindowResize(true);

        // Add listeners
        createAddInvestigationDetailsHandler();
        createLoginHandler();
        createLogoutHandler();
    }

    /**
     * Get a base tree loader that uses the given proxy.
     * 
     * @param proxy
     * @return a BaseTreeLoader
     */
    private BaseTreeLoader<ICATNode> getLoader(RpcProxy<ArrayList<ICATNode>> proxy) {
        BaseTreeLoader<ICATNode> loader = new BaseTreeLoader<ICATNode>(proxy) {
            @Override
            public boolean hasChildren(ICATNode parent) {
                // treeGrid.getStore().getChildCount(parent) !=0
                return logfilesMap.get(parent.getFacility() + parent.getDatafileId()) != null
                        || parent.getNodeType() != ICATNodeType.DATAFILE
                        && parent.getNodeType() != ICATNodeType.UNKNOWN;
            }
        };
        return loader;
    }

    /**
     * Get a ToolBar with a ButtonBar with a download button.
     * 
     * @return a ToolBar
     */
    private ToolBar getToolBar() {
        ToolBar toolBar = new ToolBar();
        ButtonBar buttonBar = new ButtonBar();
        DownloadButton btnDownload = new DownloadButton();
        btnDownload.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                download(((DownloadButtonEvent) ce).getDownloadName());
            }
        });
        buttonBar.add(btnDownload);
        toolBar.add(buttonBar);
        return toolBar;
    }

    /**
     * Get the RPC proxy which will get the information from the server using
     * GWT-RPC AJAX calls each time the user expands the tree to browse.
     * 
     * @return a RpcProxy
     */
    private RpcProxy<ArrayList<ICATNode>> getProxy() {
        RpcProxy<ArrayList<ICATNode>> proxy = new RpcProxy<ArrayList<ICATNode>>() {
            // Get the nodes from the server using GWT-RPC. For the datafiles
            // grouped under datafiles get it from the cache.
            @Override
            protected void load(Object loadConfig, final AsyncCallback<ArrayList<ICATNode>> callback) {
                // Retrieve the datafiles information from the cache instead of
                // going to the GWT-RPC for datafiles grouped under datafiles
                // (log files under RAW files)
                if (loadConfig != null && ((ICATNode) loadConfig).getNodeType() == ICATNodeType.DATAFILE) {
                    String key = ((ICATNode) loadConfig).getFacility() + ((ICATNode) loadConfig).getDatafileId();
                    callback.onSuccess(logfilesMap.get(key));
                    return;
                }
                utilityService.getAllICATNodeDatafiles((ICATNode) loadConfig,
                        new AsyncCallback<HashMap<String, ArrayList<ICATNode>>>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                if (caught instanceof SessionException) {
                                    EventPipeLine.getInstance().checkStillLoggedIn();
                                } else if (caught instanceof InternalException) {
                                    EventPipeLine
                                            .getInstance()
                                            .showErrorDialog(
                                                    "An internal error occured on the server, please see the server logs for more details.");
                                } else {
                                    EventPipeLine.getInstance().showErrorDialog(
                                            "Error retrieving data from server @browse panel. " + caught.getMessage());
                                }
                                callback.onFailure(caught);
                                // TODO This is not working properly, the circle
                                // over the node does not disappear
                            }

                            @Override
                            public void onSuccess(HashMap<String, ArrayList<ICATNode>> result) {
                                ArrayList<ICATNode> rawFiles = result.get("");
                                // remove from the result list
                                result.remove("");
                                for (String key : result.keySet()) {                                      
                                    rootLogger.log(Level.SEVERE, "getProxy() key: " + key + "(" + result.get(key).toString() + ")");
                                    
                                    logfilesMap.put(key, result.get(key));
                                }
                                callback.onSuccess(rawFiles);
                            }

                        });
            }
        };
        return proxy;
    }

    /**
     * Add an Expand Listener to the tree.
     */
    private void addExpandListener() {
        // This handler calls the reloading of the tree if the children are
        // none. Useful when the session expires or user hasn't logged in.
        tree.addListener(Events.Expand, new Listener<TreePanelEvent<ICATNode>>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handleEvent(TreePanelEvent<ICATNode> be) {
                TreePanel<ICATNode>.TreeNode node = be.getNode();
                if (!node.isLeaf() && node.getItemCount() == 0)
                    loader.loadChildren(be.getItem());
            }
        });
    }

    /**
     * Add a BeforeCheckChange Listener to the tree.
     */
    private void addBeforeCheckChangeListener() {
        // This is to check the RAW Datafile checked and check all the children
        tree.addListener(Events.BeforeCheckChange, new Listener<TreePanelEvent<ICATNode>>() {
            @Override
            public void handleEvent(TreePanelEvent<ICATNode> be) {
                // If children of raw datafiles are not loaded then load them.
                ICATNode node = be.getItem();
                if ((node.getNodeType() == ICATNodeType.DATAFILE || node.getNodeType() == ICATNodeType.INVESTIGATION)
                        && loader.hasChildren(node) && tree.getStore().getChildCount(node) == 0) {
                    loader.loadChildren(node);
                }
                // Only allow selection of datafiles, datasets and
                // investigations
                if (node.getNodeType() != ICATNodeType.DATAFILE && node.getNodeType() != ICATNodeType.DATASET
                        && node.getNodeType() != ICATNodeType.INVESTIGATION) {
                    be.setCancelled(true);
                    return;
                }
            }
        });
    }

    /**
     * Add an OnClick Listener to the tree.
     */
    private void addSingleClickListener() {
        // On click show investigation details
        tree.addListener(Events.OnClick, new Listener<TreePanelEvent<ICATNode>>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handleEvent(TreePanelEvent<ICATNode> be) {
                TreePanel<ICATNode>.TreeNode node = be.getNode();
                if (node.getModel().getNodeType() == ICATNodeType.INVESTIGATION) {
                    ICATNode icatnode = node.getModel();
                    EventPipeLine.getInstance().getInvestigationDetails(icatnode.getFacility(),
                            icatnode.getInvestigationId(), SOURCE);
                }
            }
        });
    }

    /**
     * Add an OnDoubleClick Listener to the tree.
     */
    private void addDoubleClickListener() {
        // On double click on datafile node show a parameter window
        tree.sinkEvents(Events.OnDoubleClick.getEventCode());
        tree.addListener(Events.OnDoubleClick, new Listener<TreePanelEvent<ICATNode>>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handleEvent(TreePanelEvent<ICATNode> be) {
                TreePanel<ICATNode>.TreeNode node = be.getNode();
                if (node.getModel().getNodeType() == ICATNodeType.INVESTIGATION) {
                    ICATNode icatnode = node.getModel();
                    EventPipeLine.getInstance().getInvestigationDetails(icatnode.getFacility(),
                            icatnode.getInvestigationId(), SOURCE);
                } else if (node.getModel().getNodeType() == ICATNodeType.DATASET) {
                    ICATNode icatnode = node.getModel();
                    EventPipeLine.getInstance().showParameterWindowWithHistory(icatnode.getFacility(),
                            Constants.DATA_SET, icatnode.getDatasetId(), icatnode.getDatafileName());
                } else if (node.getModel().getNodeType() == ICATNodeType.DATAFILE) {
                    ICATNode icatnode = node.getModel();
                    EventPipeLine.getInstance().showParameterWindowWithHistory(icatnode.getFacility(),
                            Constants.DATA_FILE, icatnode.getDatafileId(), icatnode.getDatafileName());
                }
            }
        });
    }
    
    
    private void addChangeListener() {
        tree.addListener(Events.CheckChange, new Listener<TreePanelEvent<ICATNode>>(){

            @Override
            public void handleEvent(TreePanelEvent<ICATNode> be) {
                // TODO Auto-generated method stub
                ICATNode node = be.getItem();
                if(be.isChecked()){
                    rootLogger.log(Level.SEVERE, "is checked: " + node.getNodeType().name() + "(" + node.getDatafileName() + ")");
                } else {
                    rootLogger.log(Level.SEVERE, "is unchecked: " + node.getNodeType().name() + "(" + node.getDatafileName() + ")");
                }
            }
            
        });
    }
    

    /**
     * Add a ContextMenu to the tree.
     */
    private void addContextMenu() {
        // Context Menu
        tree.setContextMenu(new Menu());
        tree.addListener(Events.ContextMenu, new Listener<TreePanelEvent<ICATNode>>() {
            @Override
            public void handleEvent(TreePanelEvent<ICATNode> be) {
                @SuppressWarnings("unchecked")
                TreePanel<ICATNode>.TreeNode node = be.getNode();
                if (node.getModel().getNodeType() == ICATNodeType.INVESTIGATION) {
                    tree.setContextMenu(getInvestigationMenu());
                } else if (node.getModel().getNodeType() == ICATNodeType.DATASET) {
                    tree.setContextMenu(getDatasetMenu());
                } else if (node.getModel().getNodeType() == ICATNodeType.DATAFILE) {
                    tree.setContextMenu(getDatafileMenu());
                } else {
                    be.setCancelled(true);
                }
            }
        });
    }

    /**
     * Get a context menu specific to an investigation node.
     * 
     * @return a context menu
     */
    private Menu getInvestigationMenu() {
        Menu contextMenu = new Menu();
        contextMenu.setWidth(160);
        MenuItem showInvestigation = new MenuItem();
        showInvestigation.setText("show investigation");
        showInvestigation.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconView()));
        contextMenu.add(showInvestigation);
        showInvestigation.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                if (tree.getSelectionModel().getSelectedItem().getNodeType() == ICATNodeType.INVESTIGATION) {
                    EventPipeLine.getInstance().getInvestigationDetails(
                            tree.getSelectionModel().getSelectedItem().getFacility(),
                            tree.getSelectionModel().getSelectedItem().getInvestigationId(), SOURCE);
                }
            }
        });
        return contextMenu;
    }

    /**
     * Get a context menu specific to an data set node.
     * 
     * @return a context menu
     */
    private Menu getDatasetMenu() {
        Menu contextMenu = new Menu();
        contextMenu.setWidth(160);
        MenuItem showInvestigation = new MenuItem();
        showInvestigation.setText("show data set");
        showInvestigation.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconView()));
        contextMenu.add(showInvestigation);
        showInvestigation.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                EventPipeLine.getInstance().showParameterWindowWithHistory(
                        tree.getSelectionModel().getSelectedItem().getFacility(), Constants.DATA_SET,
                        tree.getSelectionModel().getSelectedItem().getDatasetId(),
                        tree.getSelectionModel().getSelectedItem().getDatasetName());
            }
        });
        return contextMenu;
    }

    /**
     * Get a context menu specific to an data file node.
     * 
     * @return a context menu
     */
    private Menu getDatafileMenu() {
        Menu contextMenu = new Menu();
        contextMenu.setWidth(160);
        MenuItem showInvestigation = new MenuItem();
        showInvestigation.setText("show data file");
        showInvestigation.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconView()));
        contextMenu.add(showInvestigation);
        showInvestigation.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                EventPipeLine.getInstance().showParameterWindowWithHistory(
                        tree.getSelectionModel().getSelectedItem().getFacility(), Constants.DATA_FILE,
                        tree.getSelectionModel().getSelectedItem().getDatafileId(),
                        tree.getSelectionModel().getSelectedItem().getDatafileName());
            }
        });
        return contextMenu;
    }

    /**
     * Download selected datasets and datafiles.
     */
    private void download(String downloadName) {
        List<ICATNode> selectedItems = tree.getCheckedSelection();
        
        for (ICATNode node : selectedItems) {
            if (node.getNodeType() == ICATNodeType.DATASET) {
                rootLogger.log(Level.SEVERE, "DATASET: " + node.getDatasetId() + "(" + node.getDatasetName() + ")"); 
            }
            
            if (node.getNodeType() == ICATNodeType.DATAFILE) {
                ICATNodeType type = node.getNodeType();
                
                rootLogger.log(Level.SEVERE, type.name() + ":: " + node.getDatafileId() + "(" + node.getDatafileName() + ")"); 
            }
        }

        // Create map of selected datasets
        // map: key = facility name, value = list of dataset ids
        HashMap<String, ArrayList<Long>> dsMap = new HashMap<String, ArrayList<Long>>();
        for (ICATNode node : selectedItems) {
            if (node.getNodeType() == ICATNodeType.DATASET) {
                ArrayList<Long> idList = dsMap.get(node.getFacility());
                if (idList == null) {
                    idList = new ArrayList<Long>();
                    dsMap.put(node.getFacility(), idList);
                }
                idList.add(new Long(node.getDatasetId()));
            }
        }

        // Create map of selected datafiles
        // map: key = facility name, value = list of datafile ids
        HashMap<String, ArrayList<Long>> dfMap = new HashMap<String, ArrayList<Long>>();
        for (ICATNode node : selectedItems) {
            if (node.getNodeType() == ICATNodeType.DATAFILE) {
                ArrayList<Long> dsList = dsMap.get(node.getFacility());
                if (dsList != null) {
                    if (dsList.contains(new Long(tree.getStore().getParent(node).getDatasetId()))) {
                        // we have already selected the whole datset so ignore
                        // the file
                        continue;
                    }
                }
                ArrayList<Long> idList = dfMap.get(node.getFacility());
                if (idList == null) {
                    idList = new ArrayList<Long>();
                    dfMap.put(node.getFacility(), idList);
                }
                idList.add(new Long(node.getDatafileId()));
            }
        }

        // Calculate how many batches the data will need to be split into.
        // Different batches are required for each dataset and for each
        // facility. Datafiles from the same facility can be grouped together.
        int requiredBatches = 0;
        for (String facility : dsMap.keySet()) {
            requiredBatches = requiredBatches + dsMap.get(facility).size();
        }
        requiredBatches = requiredBatches + dfMap.size();

        int batchCount = 0;
        // get a download frame for each data set
        for (String facility : dsMap.keySet()) {
            List<Long> idList = dsMap.get(facility);
            for (Long id : idList) {
                batchCount = batchCount + 1;
                // TODO the next two lines where added as a temporary measure
                // until all downloads are via the I.D.S. At which point all of
                // the code needs reviewing.
                List<Long> ids = new ArrayList<Long>(1);
                ids.add(id);
                if (requiredBatches == 1) {
                    DownloadManager.getInstance().downloadDataset(facility, ids, downloadName);
                } else {
                    if (batchCount < 10) {
                        DownloadManager.getInstance().downloadDataset(facility, ids, downloadName + "-0" + batchCount);
                    } else {
                        DownloadManager.getInstance().downloadDataset(facility, ids, downloadName + "-" + batchCount);
                    }
                }
            }
        }

        // get a download frame for each batch of data files, one batch per
        // facility
        for (String facility : dfMap.keySet()) {
            batchCount = batchCount + 1;
            List<Long> idList = dfMap.get(facility);
            if (requiredBatches == 1) {
                DownloadManager.getInstance().downloadDatafiles(facility, idList, downloadName);
            } else {
                if (batchCount < 10) {
                    DownloadManager.getInstance().downloadDatafiles(facility, idList, downloadName + "-0" + batchCount);
                } else {
                    DownloadManager.getInstance().downloadDatafiles(facility, idList, downloadName + "-" + batchCount);
                }
            }
        }

        if (batchCount == 0) {
            EventPipeLine.getInstance().showMessageDialog("Nothing selected for download");
        } /*else if (batchCount == 1) {
            EventPipeLine.getInstance().showMessageDialog(
                    "Your data is being retrieved, this may be from tape, and will automatically start downloading shortly "
                            + "as a single file. The status of your download can be seen from the ‘My Downloads’ tab.");
        } else {
            EventPipeLine.getInstance().showMessageDialog(
                    "Your data is being retrieved, this may be from tape, and will automatically start downloading shortly "
                            + "as " + batchCount
                            + " files. The status of your download can be seen from the ‘My Downloads’ tab.");
        }*/
    }

    /**
     * This method sets the width of the tree.
     * 
     * @param width
     */
    public void setTreeWidth(int width) {
        tree.setWidth(width);
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
     * Setup a handler to react to Login events.
     */
    private void createLoginHandler() {
        LoginEvent.register(EventPipeLine.getEventBus(), new LoginEventHandler() {
            @Override
            public void login(LoginEvent event) {
                List<ICATNode> nodes = tree.getStore().findModels("type", ICATNodeType.FACILITY);
                for (ICATNode node : nodes) {
                    if (node.getFacility().equalsIgnoreCase(event.getFacilityName())) {
                        loader.loadChildren(node);
                    }
                }
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
                // Close the tree and remove the children for the logged out
                // facility
                List<ICATNode> nodes = tree.getStore().findModels("type", ICATNodeType.FACILITY);
                for (ICATNode node : nodes) {
                    if (node.getFacility().equalsIgnoreCase(event.getFacilityName())) {
                        tree.setExpanded(node, false);
                        tree.getStore().removeAll(node);
                    }
                }

                // Hide the investigation details if it is an investigation of
                // the logged out facility
                String invDetailsFacility = investigationPanel.getFacilityName();
                if (!(invDetailsFacility == null) && invDetailsFacility.equalsIgnoreCase(event.getFacilityName())) {
                    investigationPanel.hide();
                    investigationPanel.reset();
                }
            }
        });
    }
}
