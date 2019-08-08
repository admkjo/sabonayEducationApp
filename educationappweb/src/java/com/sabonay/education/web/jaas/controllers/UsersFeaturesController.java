/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sabonay.education.web.jaas.controllers;

import com.sabonay.common.context.SabonayContext;
import com.sabonay.common.jaas.entities.Users;
import com.sabonay.common.jaas.sessionbean.UserAccessEjbLookup;
import com.sabonay.education.common.utils.xEduConstants;
import com.sabonay.education.web.jaas.tablemodel.UserFeatureTableModel;
import com.sabonay.education.web.jaas.tablemodel.UserTableModel;
import com.sabonay.modules.web.jsf.api.annotations.ClearButtonAction;
import com.sabonay.modules.web.jsf.api.annotations.DataPanel;
import com.sabonay.modules.web.jsf.api.annotations.DataTableModelList;
import com.sabonay.modules.web.jsf.api.annotations.DataTableRowSelectionAction;
import com.sabonay.modules.web.jsf.api.annotations.DeleteButtonAction;
import com.sabonay.modules.web.jsf.api.annotations.FormControl;
import com.sabonay.modules.web.jsf.api.annotations.SaveEditButtonAction;
import com.sabonay.modules.web.jsf.api.annotations.SearchButtonAction;
import com.sabonay.modules.web.jsf.component.HtmlDataPanel;
import com.sabonay.modules.web.jsf.component.HtmlFormControl;
import com.sabonay.modules.web.jsf.context.SabonayContextUtils;
import com.sabonay.modules.web.jsf.utilities.JsfUtil;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;

/**
 *
 * @author Agyepong
 */
@Named(value = "usersFeaturesController")
@SessionScoped
public class UsersFeaturesController implements Serializable {

    private Users user;
    private UserFeatureTableModel userTableModel;
    @DataTableModelList(group = "s")
    private List<Users> userList = null;
    @DataPanel(group = "s")
    private HtmlDataPanel<Users> userDataPanel = null;
    @FormControl(group = "s")
    private HtmlFormControl userFormControl;

    /**
     * Creates a new instance of UsersController
     */
    public UsersFeaturesController() {
        //System.out.println("UsersFeaturesController() UserAccessEjbLookup.getUsersSession() " + UserAccessEjbLookup.getUsersSession());
        user = new Users();
        userTableModel = new UserFeatureTableModel();
        userFormControl = new HtmlFormControl();
        userDataPanel = userTableModel.getDataPanel();
        userDataPanel.setHeaderText("Search Text");

        userDataPanel.setVisibleColumns(1, 2);
        userDataPanel.autoBindAndBuild(UsersFeaturesController.class, "s");
        userFormControl.autoBindAndBuild(UsersFeaturesController.class, "s");

        int[] range = new int[]{0, xEduConstants.MAX_RECORDS_RETRIEVE};
        SabonayContext sc = SabonayContextUtils.getSabonayContext();
        //System.out.println("UsersFeaturesController() sc " + sc);
        userList = UserAccessEjbLookup.getUsersSession().findRange(sc, range);
        //System.out.println("UsersFeaturesController() userList " + userList);
        //if (null != userList) {
        //    System.out.println("UsersFeaturesController() userList.size() " + userList.size());
        //}
    }

    private boolean userHasdata(Users user) {
        if ( (null != user) && (null != user.getUsersPK()) &&
             ((null != user.getUsersPK().getSchid()) && (user.getUsersPK().getSchid().length() > 4)) && 
             ((null != user.getUsersPK().getUserid()) && (user.getUsersPK().getUserid().length() > 4)) && 
             ((null != user.getUpassword()) && (user.getUpassword().length() > 6)) ) {
            return true;
        }

        return false;
    }

    @SaveEditButtonAction(group = "s")
    public String saveEditButtonAction() {
        if (userHasdata(user)) {
            try {
                SabonayContext sc = SabonayContextUtils.getSabonayContext();
                String schuserid = user.getUsersPK().getSchid() + user.getUsersPK().getUserid();
                user.setSchuserid(schuserid);
                if (userFormControl.isTextOnSaveEditButton_Save()) {
                    // we are in insert mode
                    //UsersPK lusersPK = new UsersPK( sc.getClientId(), userid );
                    //Users luser = new Users( lusersPK );
                    int retval = UserAccessEjbLookup.getUsersSession().create(sc, user);
                } else if (userFormControl.isTextOnSaveEditButton_Edit()) {
                    // we are in edit mode
                    int retval = UserAccessEjbLookup.getUsersSession().edit(sc, user);
                }
                clearButtonAction();
            } catch (Exception e) {
            }
        }

        return null;
    }

    @ClearButtonAction(group = "s")
    public String clearButtonAction() {
        try {
            user = null;
            user = new Users();
            userFormControl.setSaveEditButtonTextTo_Save();
        } catch (Exception exp) {
            Logger.getLogger(UsergroupController.class.getName()).log(Level.SEVERE, exp.toString(), exp);
            JsfUtil.addErrorMessage("UsergroupController::clearUserButtonAction() Error occurred in clearing form ");
        }

        JsfUtil.getFacesContext().renderResponse();

        return null;
    }

    @DeleteButtonAction(group = "s")
    public String deleteButtonAction() {
        //System.out.println("UsersController::deleteButtonAction() user " + user);
        if (null != user) {
            //System.out.println("UsersController::deleteButtonAction() user " + user);
            try {
                SabonayContext sc = SabonayContextUtils.getSabonayContext();
                // delete all records for user in usergroup table
                int ugretval = UserAccessEjbLookup.getUsergroupSession().delete(user.getUsersPK().getSchid(), user.getUsersPK().getUserid());

                // now delete the user record
                int retval = UserAccessEjbLookup.getUsersSession().remove(sc, user);
                if (retval > 0) {
                    deleteFromUserslist(user);
                    clearButtonAction();
                }
            } catch (Exception e) {
            }
        }

        return null;
    }

    @DataTableRowSelectionAction(group = "s")
    public String dataTableRowSelectionAction() {
        user = userDataPanel.getRowData();
        //System.out.println("UsersController::dataTableRowSelectionAction() user " + user);

        userFormControl.setSaveEditButtonTextTo_Edit();

        return null;
    }

    @SearchButtonAction(group = "s")
    public String dataTableSearchButtonAction() {
        String searchCriteria = userDataPanel.getSearchCriteria();
        //System.out.println("UsersController::dataTableSearchButtonAction() searchCriteria " + searchCriteria);
        String searchText = userDataPanel.getSearchText();
        //System.out.println("UsersController::dataTableSearchButtonAction() searchText " + searchText);

        if ((null != searchCriteria) && (null != searchText)) {
            userList = UserAccessEjbLookup.getUsersSession().findByAttribute(searchCriteria, searchText);
            if (userList.size() > 0) {
                // if we found record(s) to edit set mode to Edit
                userFormControl.setSaveEditButtonTextTo_Edit();
            }
        }

        return null;
    }

    public HtmlDataPanel<Users> getUserDataPanel() {
        return userDataPanel;
    }

    public void setUserDataPanel(HtmlDataPanel<Users> userDataPanel) {
        this.userDataPanel = userDataPanel;
    }

    public HtmlFormControl getUserFormControl() {
        return userFormControl;
    }

    public void setUserFormControl(HtmlFormControl userFormControl) {
        this.userFormControl = userFormControl;
    }

    public UserFeatureTableModel getUserTableModel() {
        return userTableModel;
    }

    public void setUserTableModel(UserFeatureTableModel userTableModel) {
        this.userTableModel = userTableModel;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public List<Users> getUserList() {
        return userList;
    }

    public void setUserList(List<Users> userList) {
        this.userList = userList;
    }

    public void addToUserslist(Users user) {
        userList.add(user);
    }

    public void deleteFromUserslist(Users user) {
        userList.remove(user);
    }

}
