package com.cfecweb.leon.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.cfecweb.leon.client.model.ArenewEntity;
import com.cfecweb.leon.client.model.ArenewPayment;
import com.cfecweb.leon.client.model.ArenewPermits;
import com.cfecweb.leon.client.model.ArenewPermitsId;
import com.cfecweb.leon.client.model.FeeTotals;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

/*
 * This class is called when a person clicks on the Add New Permit button. It retrieves a current fishery table, minus the persons current inventory,
 * and presents a popup box with a selectable grid. If a new permit is selected, I automatically renew the permit and update the fee totals, but I 
 * do not select the intend to fish box.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class NewPermitBuilder {
	UserMessages gmsg = new UserMessages();
	BeanModelReader nreader = null;
	ListStore<BeanModel> nstore = null;
	ListLoader npLoader = null;
	CheckBoxSelectionModel npSelectionModel = null;
	Grid<BeanModel> npGrid = null;
	int npListSize = 0;
	String dfee = null;
	InstructionsText inst = new InstructionsText();
	PmtVesSelectFunctions getPmt = new PmtVesSelectFunctions();

	public Window getnewpermit(final getDataAsync service, final TextField cfecid, final ArenewEntity entity, 
			final List<ArenewPermits> plist, final FieldSet tr, final Button newpmt, final EditorGrid<BaseModel> grid, 
			final BaseListLoader loader, final HTML statusBar, final SessionTimer timer, final FeeTotals feeTotals, final String ryear, final ArenewPayment payment) {
		/*
		 * When a person selects the add new permit option for the first time, I require an RPC to build the list based
		 * on their current inventory. After that list has been built and made into a grid, we don't need to rebuild it
		 * every time for performance purposes. So check the list size. If it's greater than 0, skip the RPC call and
		 * just present the already built grid.
		 */
		if (npListSize < 1) {
			RpcProxy proxy = new RpcProxy() {
				public void load(Object loadConfig, final AsyncCallback callback) {
					service.getfshytable(cfecid.getValue().toString(), entity.getResidency(), entity.getPoverty(), ryear, plist, new AsyncCallback<List<com.cfecweb.leon.client.model.GWTfisheryTable>>() {
						public void onFailure(Throwable caught) {
							statusBar.setHTML("<span class='regred12'>*** We are experiencing technical difficulties ***</span>");
    					    gmsg.alert("New Permit Window Error", inst.getTech(), 350);
						}
						public void onSuccess(List<com.cfecweb.leon.client.model.GWTfisheryTable> result) {
							statusBar.setHTML("<span class='regblack12'>CFEC Online Renewal Review Section</span>");
							callback.onSuccess(result);
						}
					});
				}							
			};
			nreader = new BeanModelReader();
		    npLoader = new BaseListLoader(proxy, nreader);
		    nstore = new ListStore<BeanModel>(npLoader);					      
		    npLoader.load();
	    	npSelectionModel = new CheckBoxSelectionModel();
		    npSelectionModel.setSelectionMode(SelectionMode.MULTI);
    		List<ColumnConfig> nconfigs = new ArrayList<ColumnConfig>();
		    nconfigs.add(npSelectionModel.getColumn());
		    ColumnConfig column = new ColumnConfig();
		    column.setId("fishery");
		    column.setHeaderHtml("Fishery");
		    column.setAlignment(HorizontalAlignment.LEFT);
		    column.setWidth(75);
		    nconfigs.add(column);
		    column = new ColumnConfig();
		    column.setId("myfee");
		    column.setHeaderHtml("Your Fee");
		    column.setAlignment(HorizontalAlignment.LEFT);
		    column.setWidth(75);
		    nconfigs.add(column);
		    column = new ColumnConfig();
		    column.setId("description");
		    column.setHeaderHtml("Description");
		    column.setAlignment(HorizontalAlignment.LEFT);
		    column.setWidth(420);
		    nconfigs.add(column);
		    column = new ColumnConfig();
		    column.setId("status");
		    column.setHeaderHtml("Closed");
		    column.setAlignment(HorizontalAlignment.LEFT);
		    column.setWidth(60);
		    nconfigs.add(column);
	        ColumnModel ncm = new ColumnModel(nconfigs);
	        npGrid = new Grid<BeanModel>(nstore, ncm);  
	        npGrid.setSelectionModel(npSelectionModel);
	        npGrid.setAutoExpandColumn("description");  
	        npGrid.setBorders(true);  
		    npGrid.getView().setForceFit(true);
		    npGrid.addStyleName("tableGrids");
	        npGrid.addPlugin(npSelectionModel);
	        npGrid.addListener(Events.CellMouseDown, new Listener<GridEvent>() {
				@Override
				public void handleEvent(final GridEvent be) {
					/*
					 * Only show this dialog when the permit is being selected AND if the fishery falls
					 * into the categories below.
					 */
					if (!(be.getGrid().getSelectionModel().getSelectedItem() == null)) {
						final Listener<MessageBoxEvent> l = new Listener<MessageBoxEvent>() {
					      public void handleEvent(MessageBoxEvent ce) {  
					        //Button btn = ce.getButtonClicked();  
					        //System.out.println(btn.getText());  
					        //if (btn.getText().equalsIgnoreCase("Yes")) {
					        //	
					        //} else {
					        //	System.out.println(be.getGrid().getSelectionModel().getSelectedItem().getPropertyNames());
					        //}
					      }  
					    }; 				    
						if (be.getGrid().getSelectionModel().getSelectedItem().get("fishery").toString().startsWith("M") && 
								be.getGrid().getSelectionModel().getSelectedItem().get("fishery").toString().endsWith("G")) {
							MessageBox.alert("Please read", "You have selected a Gulf of Alaska (GOA) miscellaneous finfish permit. " +
								"This permit is only valid for the Gulf of Alaska (GOA). If you are planning on fishing in the Bering Sea, " +
								"you should unselect this permit and choose the Statewide miscellaneous finfish permit.", l);  
						} else if (be.getGrid().getSelectionModel().getSelectedItem().get("fishery").toString().startsWith("M") && 
								be.getGrid().getSelectionModel().getSelectedItem().get("fishery").toString().endsWith("B")) {
							MessageBox.alert("Please read", "You have selected a Statewide miscellaneous finfish permit. This permit " +
								"is valid for the Gulf of Alaska (GOA) and Bering Sea. If you plan to fish in the Gulf of Alaska (GOA) " +
								"only, you should unselect this permit and choose the Gulf of Alaska (GOA) permit.", l); 
						}
					}
				}			
		    });
		}
		/*
		 * Create the window object, add some buttons and the grid
		 */
	    final Window npWindow = new Window();
        npWindow.setSize(640, 510);
        npWindow.setHeadingHtml("Purchase New Permit");
        npWindow.setLayout(new FitLayout());  
        npWindow.setFrame(true);
        npWindow.setBorders(true);
        npWindow.setIconStyle("icon-table");  
        npWindow.setClosable(false);
        npWindow.setButtonAlign(HorizontalAlignment.CENTER);
        npWindow.setStyleName("newPmtwin");
        Button save = new Button("Save and Return");
        save.setTitle("Save your current selection and return to your review list");
        save.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
		    public void componentSelected(ButtonEvent ce) {
		    	gmsg.waitStart("Please Wait", "Building new Permit list....", "Progress", 300);
		    	timer.timerCancel();
	       	    timer.progReset();
	       	    timer.setTimer(timer.getTime(), entity.getId().getCfecid().toString());
		    	List npList = npSelectionModel.getSelectedItems();
		    	npListSize = npList.size();
		    	npWindow.hide();
		    	if (npList.size() > 0) {
		    		/*
		    		 * Iterator through the list of new permits, only those that have been selected.
		    		 * Then build permit objects for each one, add them to the users permit list.
		    		 */
				    for (Iterator i = npGrid.getSelectionModel().getSelectedItems().iterator(); i.hasNext(); ) {
				    	BeanModel newpermit = (BeanModel) i.next();
				    	ArenewPermits npmt = new ArenewPermits();
				    	ArenewPermitsId npmtId = new ArenewPermitsId();				    	
				    	npmtId.setFishery(newpermit.get("fishery").toString());
				    	npmt.setDescription(newpermit.get("description").toString());
				    	if (newpermit.get("fishery").toString().trim().equalsIgnoreCase("B 06B") ||
				    			newpermit.get("fishery").toString().trim().equalsIgnoreCase("B 61B") ||
				    			newpermit.get("fishery").toString().trim().equalsIgnoreCase("C 06B") ||
				    			newpermit.get("fishery").toString().trim().equalsIgnoreCase("C 61B") ||
				    			newpermit.get("fishery").toString().trim().equalsIgnoreCase("C 09B") ||
				    			newpermit.get("fishery").toString().trim().equalsIgnoreCase("C 91B")) {
				    		dfee = (newpermit.get("myfee").toString());
				    	} else {
				    		dfee = (newpermit.get("myfee").toString());
				    	}
				    	
				    	/*
				    	 * any calculation to poverty status regarding fees has already been made and determined
				    	 * prior to the new permit grid being displayed. Because of that, there is no need to
				    	 * run any of those fee calculations here as that fee is set. So, just set both poverty
				    	 * and non-poverty (and their associated original fee fields) to whatever fee has been
				    	 * pre-determined on the server for new permits based on an already decided poverty status
				    	 * by the user.
				    	 */
	            		npmt.setFee(dfee);
	            		npmt.setOfee(dfee);
	            		npmt.setPfee(dfee);
	            		npmt.setOpfee(dfee);
				    	npmtId.setSerial("Not Issued");
				    	npmtId.setRyear(entity.getId().getRyear());
				    	npmtId.setPyear(entity.getId().getRyear());
				    	npmt.setMsna("0");
				    	npmt.setAdfg("N/A");
				    	npmt.setStatus("Available");
				    	if (newpermit.get("fishery").toString().equalsIgnoreCase("B 06B") || 
				    			newpermit.get("fishery").toString().equalsIgnoreCase("B 61B")) {
				    		npmt.setNotes("If you did not fish this permit last year AND can provide CFEC with supporting IFQ evidence " +
				    			"of less than 8000 lbs of halibut this year, you qualify for the lower fee option");
				    		npmt.setHalibut(true);
				    	} else if (newpermit.get("fishery").toString().equalsIgnoreCase("C 06B") || 
				    			newpermit.get("fishery").toString().equalsIgnoreCase("C 61B") ||
				    			newpermit.get("fishery").toString().equalsIgnoreCase("C 09B") ||
				    			newpermit.get("fishery").toString().equalsIgnoreCase("C 91B")) {
				    		npmt.setNotes("If you did not fish this permit last year AND can provide CFEC with supporting IFQ evidence " +
				    			"of less than 9000 lbs of sablefish this year, you qualify for the lower fee option");
				    		npmt.setSablefish(true);
				    	} else {
				    		npmt.setNotes("No Notes Available");
				    		npmt.setHalibut(false);
					    	npmt.setSablefish(false);
				    	}
				    	npmtId.setCfecid(entity.getId().getCfecid());
				    	npmt.setNewpermit(true);
				    	if (entity.getPoverty().equalsIgnoreCase("true")) {
				    		npmt.setPovertyfee(true);
				    	} else {
				    		npmt.setPovertyfee(false);
				    	}
				    	npmt.setNewpermit(true);
				    	npmt.setMpmt("No");
				    	npmt.setConfirmcode("0000-00000-000000");
				    	npmt.setId(npmtId);				    	
				    	/*
				    	 * Add the new permit object to the grid list
				    	 */
				    	plist.add(npmt);
				    	/*
				    	 * 	Automatically sets this new permit to renewed and updates fee tables
				    	 */
				    	getPmt.NewPermit(feeTotals, entity, npmt, payment);
				    	Log.info(entity.getId().getCfecid() + " is adding a new Permit (Fishery "+npmtId.getFishery()+") to their profile");
				    	/*
				    	 * Finally, delete this permit from the grid store so it can not be purchased twice.
				    	 */
				    	nstore.remove(newpermit);
				    }
		    	}
		    	/*
		    	 * After the list has been rebuilt, send it back to the server for sorting
		    	 * and for poverty fee manipulation, if selected.
		    	 */
		    	service.sortPlist(plist, entity.getPoverty(), new AsyncCallback() {
					public void onFailure(Throwable caught) {
						gmsg.waitStop();
						caught.printStackTrace();			
					}
					public void onSuccess(Object result) {
						gmsg.waitStop();
						plist.clear();
						List<ArenewPermits> p = (List<ArenewPermits>) result;			
						plist.addAll(p);
						/*
						 * refresh and reload the permit grid with the changes 
						 */
						loader.load();
					}
		    	});
				DOM.getElementById("permitsRenewalble").setInnerText(Integer.toString(plist.size()));
				tr.layout();
		    }
	    });
        npWindow.addButton(save);
        
        Button cancel = new Button("Cancel and Return");
        cancel.setTitle("Return to your review list without adding any newly checked fisheries");
        cancel.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
        	public void componentSelected(ButtonEvent ce) {
		    	npWindow.hide();
		    }
        });
        npWindow.addButton(cancel);
        npWindow.add(npGrid);
        return npWindow;
	}
	
	public String getpview(List<ArenewPermits> plist, String xname) {
		StringBuffer p = new StringBuffer("<table id='pmtTable'><tr>");
		p.append("<th><span class='regblue10'>Fishery</span></th>");
    	p.append("<th><span class='regblue10'>Serial Number</span></th>");
    	p.append("<th><span class='regblue10'>ADFG Number</span></th>");
    	p.append("<th><span class='regblue10'>Name</span></th>");
    	p.append("<th><span class='regblue10'>Year</span></th>");
    	p.append("<th><span class='regblue10'>Status</span></th></tr>");
    	int x = 0;
    	if (plist.size() > 0) {
    		while (x < plist.size()) {
	    		p.append("<tr><td><span class='regbrown10' title='").append(plist.get(x).getDescription()).append("'>"+plist.get(x).getId().getFishery()+"</span></td>");
	        	if (plist.get(x).getNewpermit()) {
	        		p.append("<td><span class='regblack10'>Not Issued</span></td>");
	        		p.append("<td><span class='regblack10'>N/A</span></td>");
	        	} else {
	        		p.append("<td><span class='regblack10'>").append(plist.get(x).getId().getSerial()).append("</span></td>");
	        		p.append("<td><span class='regblack10'>").append(plist.get(x).getAdfg()).append("</span></td>");
	        	}	        	
	        	p.append("<td><span class='regblack10'>").append(xname).append("</span></td>");
	        	p.append("<td><span class='regblack10'>").append(plist.get(x).getId().getPyear()).append("</span></td>");
	        	if (plist.get(x).getStatus().equalsIgnoreCase("Pending")) {
	        		p.append("<td><span class='regorange10' title='This Permit is pending renewal as of "+plist.get(x).getReceiveddate()+"'>").append(plist.get(x).getStatus()).append("</span></td></tr>");
	        	} else if (plist.get(x).getStatus().equalsIgnoreCase("Completed")) {
	        		p.append("<td><span class='regred10' title='This Permit has been has been renewed, you should receive the card in the mail shortly'>").append(plist.get(x).getStatus()).append("</span></td></tr>");
	        	} else if (plist.get(x).getStatus().equalsIgnoreCase("Available")) {
	        		p.append("<td><span class='reggreen10' title='This Permit is still available and has not been selected for renweal'>").append(plist.get(x).getStatus()).append("</span></td></tr>");
	        	} else {
	        		p.append("<td><span class='regblue10' title='Please call a CFEC licensing agent on the status of this Permit'>").append(plist.get(x).getStatus()).append("</span></td></tr>");
	        	}	        	
	        	x++;
    		}
    	}
    	p.append("</table>");
		return p.toString();
	}
	
}
