package ntut.csie.ezScru.web.microservice;

import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.jettison.json.JSONException;

import ntut.csie.ezScru.web.microservice.command.ICommand;
import ntut.csie.ezScru.web.microservice.command.AddAssignedRoleCommand;
import ntut.csie.ezScru.web.microservice.command.CreateAccountCommand;
import ntut.csie.ezScru.web.microservice.command.DeleteAccountCommand;
import ntut.csie.ezScru.web.microservice.command.RemoveAssignRoleCommand;
import ntut.csie.ezScru.web.microservice.command.UpdateAccountCommand;
import ntut.csie.ezScrum.web.dataInfo.AccountInfo;
import ntut.csie.ezScrum.web.dataObject.AccountObject;
import ntut.csie.ezScrum.web.dataObject.NotificationObject;
import ntut.csie.ezScrum.web.helper.AccountHelper;
import ntut.csie.jcis.account.core.LogonException;

public class MicroserviceProxy implements IAccount{
	
	String token;
	private AccountHelper accountHelper;
	private AccountRESTClient accountRESTClient;
	private ICommand mAccountRESTCommand;
	private Invoker invoker = new Invoker();
	private ConnectionQueue notDoneActionQueue;
	public MicroserviceProxy(){
		accountHelper = new AccountHelper();
		accountRESTClient = new AccountRESTClient();
	}
	public MicroserviceProxy(String token){
		accountHelper = new AccountHelper();
		accountRESTClient = new AccountRESTClient(token);
		this.token = token;
		notDoneActionQueue = new ConnectionQueue(accountRESTClient);
	}
	
	public String validateUsername(String inputUsername) {
		String responseFromHelper = accountHelper.validateUsername(inputUsername);
		String response;
		try {
			response = accountRESTClient.validateUsername(inputUsername);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return responseFromHelper;
		}
		return response;
	}
	
	public String getToken(){
		return token;
	}
	
	public void setToken(String token){
		this.token = token;
		accountRESTClient.setToken(token);
	}

	public AccountObject createAccount(AccountInfo accountInfo) {
		AccountObject accountFromHelper = accountHelper.createAccount(accountInfo);
		AccountObject account = null;
		mAccountRESTCommand = new CreateAccountCommand(accountRESTClient, accountInfo);
		invoker.addAction(mAccountRESTCommand);
		try {
			account = (AccountObject) invoker.doCommand();
			return account;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			notDoneActionQueue.queue(mAccountRESTCommand);
			return accountFromHelper;
		}
//			account = accountRESTClient.createAccount(accountInfo);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return accountFromHelper;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return accountFromHelper;
//		}
	}
	
	public AccountObject updateAccount(AccountInfo accountInfo){
		AccountObject accountFromHelper = accountHelper.updateAccount(accountInfo);
		AccountObject account = null;
		mAccountRESTCommand = new UpdateAccountCommand(accountRESTClient,accountInfo);
		invoker.addAction(mAccountRESTCommand);
		try {
			account = (AccountObject) invoker.doCommand();
			return account;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			notDoneActionQueue.queue(mAccountRESTCommand);
			return accountFromHelper;
		}
	}
	
	public boolean deleteAccount(long id){
		boolean deleteFromAccountHelper = accountHelper.deleteAccount(id);
		boolean checkDelete = false;
		mAccountRESTCommand = new DeleteAccountCommand(accountRESTClient, id);
		invoker.addAction(mAccountRESTCommand);
		try {
			checkDelete = (boolean) invoker.doCommand();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			notDoneActionQueue.queue(mAccountRESTCommand);
			return deleteFromAccountHelper;
		}
		return checkDelete;
	}
	
	public AccountObject getAccountById(long id){
		AccountObject accountFromHelper = accountHelper.getAccountById(id);
		AccountObject account = null;
		try {
			account = accountRESTClient.getAccountById(id);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return accountFromHelper;
		} 
		return account;
	}
	
	@Override
	public String getAssignedProject(long accountId) {
		String assignedProjectFromHelper = accountHelper.getAssignedProject(accountId);
		String assingedProject;
		try {
			assingedProject = accountRESTClient.getAssignedProject(accountId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return assignedProjectFromHelper;
		}
		return assingedProject;
	}

	@Override
	public AccountObject addAssignedRole(long accountId, long projectId, String scrumRole) {
		AccountObject accountFromHelper = accountHelper.addAssignedRole(accountId, projectId, scrumRole);

		if(scrumRole.equals("admin")){
			mAccountRESTCommand = new AddAssignedRoleCommand(accountRESTClient, accountId, projectId);
			invoker.addAction(mAccountRESTCommand);
			try {
				AccountObject account = (AccountObject) invoker.doCommand();
				return account;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				notDoneActionQueue.queue(mAccountRESTCommand);
				return accountFromHelper;
			}
		}else{
			return accountFromHelper;
		}	
	}
	
	@Override
	public AccountObject removeAssignRole(long accountId, long projectId, String scrumRole) {
		AccountObject accountFromHelper = accountHelper.removeAssignRole(accountId, projectId, scrumRole);
		
		if(scrumRole.equals("admin")){
			mAccountRESTCommand = new RemoveAssignRoleCommand(accountRESTClient, accountId, projectId);
			invoker.addAction(mAccountRESTCommand);
			try {
				AccountObject account = (AccountObject) invoker.doCommand();
				return account;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				notDoneActionQueue.queue(mAccountRESTCommand);
				return accountFromHelper;
			} 
		}
		return accountFromHelper;
	}
	
	public String getAccountXML(AccountObject account) {
		return accountHelper.getAccountXML(account);
	}
	
	@Override
	public String getAccountListXML() {
		String responseFromHelper = accountHelper.getAccountListXML();
		String response;
		try {
			response = accountRESTClient.getAccountListXML();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return responseFromHelper;
		}
		return response;
	}
	
	public String getManagementView(AccountObject account) {
		if(account.isAdmin() == true)
			return "Admin_ManagementView";
		else 
			return "User_ManagementView";
	}
	
	public AccountObject confirmAccount(String username, String password) throws LogonException{
		
		AccountObject theAccount = null;
		
		try {
			theAccount = accountRESTClient.confirmAccount(username, password);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//			throw new LogonException(false, false);
			AccountObject accountFromHelper = accountHelper.confirmAccount(username, password);
			return accountFromHelper;
		} 
		return theAccount;
	}
	
	public String getNotificationSubscriptStatus(Long account_id, String firebaseToken){
		try{
			return accountRESTClient.getNotificationSubscriptStatus(account_id, firebaseToken);
		}catch(IOException e){
			return "Connection Error : account management.";
		} catch (JSONException e) {
			return "";
		}		
	}
	
	public String subscribeNotification(Long account_id, String firebaseToken){
		try{
			return accountRESTClient.subscribeNotification(account_id, firebaseToken);
		}catch(IOException e){
			return "Connection Error : account management.";
		} catch (JSONException e) {
			return "";
		}		
	}
	
	public String cancelSubscribeNotification(Long account_id, String firebaseToken){
		try{
			return accountRESTClient.cancelSubscribeNotification(account_id, firebaseToken);
		}catch(IOException e){
			return "Connection Error : account management.";
		} catch (JSONException e) {
			return "";
		}		
	}
	
	public String notifyServiceLogout(Long account_id, String firebaseToken){
		try{
			return accountRESTClient.notifyServiceLogout(account_id, firebaseToken);
		}catch(IOException e){
			return "Connection Error : account management.";
		} catch (JSONException e) {
			return "";
		}		
	}
	
	public String sendNotification(Long senderId, ArrayList<Long> recipients_id, NotificationObject notificationObject){
		try{
			ArrayList<Long> recipientsExcludeSender = new ArrayList<Long>();
			for(Long recipient_id : recipients_id){
				if(recipient_id == senderId)
					continue;
				recipientsExcludeSender.add(recipient_id);
			}
			notificationObject.setRecipientsId(recipientsExcludeSender);
			return accountRESTClient.sendNotification(notificationObject);
		}catch(IOException e){
			System.out.println(e);
			return "Connection Error : account management.";
		} catch (JSONException e) {
			return "";
		}		
	}
	
	public String updateProjectsScriptStatus(Long account_id, String projectsStatus){
		try{
			return accountRESTClient.updateProjectsScriptStatus(account_id, projectsStatus);
		}catch(IOException e){
			return "Connection Error : account management.";
		} catch (JSONException e) {
			return "";
		}	
	}
	
	public AccountObject getAccount(String username){
		return accountHelper.getAccount(username);
	}
}