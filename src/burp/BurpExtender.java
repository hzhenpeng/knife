package burp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import org.apache.commons.lang3.ArrayUtils;

import U2C.Unicode;


public class BurpExtender extends Thread implements IBurpExtender, IExtensionStateListener,IContextMenuFactory,IHttpListener
{
	public String ExtenderName = "knife v0.4";
	public String github = "https://github.com/bit4woo/knife";
	public IBurpCollaboratorClientContext ccc;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;//�����������Ҫ���ڴ������
	public IBurpExtenderCallbacks callbacks;
	public List<String> dismissUrls; //���ڼ�¼���뿴����URL
	//һ���������ĳ��URL����burp http proxy ����ʾ����2�����ܻ��޷�ʵ�֣�����burp���������ơ�
	public GUIU2C U2CWindowFlag;
	
    private HashMap<String,IHttpRequestResponsePersisted> processedRequestResponse;

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
	{
		stdout = new PrintWriter(callbacks.getStdout(), true);
		stdout.println(ExtenderName);
		stdout.println(github);
		this.callbacks=callbacks;
		callbacks.setExtensionName(ExtenderName);
		callbacks.registerExtensionStateListener(this);
		callbacks.registerContextMenuFactory(this);
		ccc = callbacks.createBurpCollaboratorClientContext();
		helpers = callbacks.getHelpers();
		start();
	}

	@Override
	public void extensionUnloaded() {
		stdout.println(ExtenderName+" unloaded!");
	}
	
	@Override
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		// TODO Auto-generated method stub
		if(messageIsRequest) {
    		IRequestInfo analyzedRequest = helpers.analyzeRequest(messageInfo);
    		String fullUrl= analyzedRequest.getUrl().toString();
    		if(dismissUrls.contains(fullUrl)) {
    			//��������������ʧ�أ�
    			messageInfo.setRequest(null);
    		}
		}
		
	}
	
	
	@Override
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation)
	{ //��Ҫ��ǩ��ע�ᣡ��callbacks.registerContextMenuFactory(this);
	    List<JMenuItem> list = new ArrayList<JMenuItem>();
    	if(true) {//invocation.getToolFlag() == 16
    		
    		JMenuItem menuItem2 = new JMenuItem("^-^ Copy this cookie");
    		menuItem2.addActionListener(new copyThisCookie(invocation));
    		list.add(menuItem2);
    		
    		JMenuItem menuItem = new JMenuItem("^-^ Get latest cookie");
            menuItem.addActionListener(new getLatestCookie(invocation));
            list.add(menuItem);
            
			JMenuItem menuItemUpdateCookie = new JMenuItem("^-^ Update cookie");
			menuItemUpdateCookie.addActionListener(new updateCookie(invocation));	
			list.add(menuItemUpdateCookie);
			
    		JMenuItem menuItem1 = new JMenuItem("^-^ Add host to scope");//��������Сscope�������
    		menuItem1.addActionListener(new addHostToScope(invocation));
    		list.add(menuItem1);

    		/*
    		JMenuItem menuItem3 = new JMenuItem("^-^ Dismiss");
    		//Ŀǰ�뵽�ķ����ǣ��޸�scope��in scope�е���any�������뿴���ļӵ�"exclude from scope";Ȼ������proxy-options-miscellaneous--don't send to proxy if "out of scope";
    		//��scope��Ӱ��ܴ󣬱���session�Ĵ�����spider����Ϊ�ȣ���Ҫ���ؿ��ǣ����Ҹ߼���scope������api��֧�֣�����ʱ����������ܡ�
    		menuItem3.addActionListener(new disMiss(invocation,dismissUrls));
    		list.add(menuItem3);
    		*/
    		/*
			byte context = invocation.getInvocationContext();
			//ֻ�е�ѡ�е���������Ӧ����ʱ�����ʾU2C
			if (context == invocation.CONTEXT_MESSAGE_EDITOR_RESPONSE || context == invocation.CONTEXT_MESSAGE_VIEWER_RESPONSE) {
	    		JMenuItem menuItem4 = new JMenuItem("^-^ U2C");
	    		menuItem4.addActionListener(new U2C(invocation));
	    		list.add(menuItem4);
			}*/
			
    		JMenuItem menuItem4 = new JMenuItem("^-^ U2C(unicode to chinese)");
    		menuItem4.addActionListener(new U2C(invocation));
    		list.add(menuItem4);
    		
        }
    	return list;
	}
	
	public class getLatestCookie implements ActionListener{
		private IContextMenuInvocation invocation;
		//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
		public getLatestCookie(IContextMenuInvocation invocation) {
			this.invocation  = invocation;
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
	      {
			IHttpRequestResponse[] messages = invocation.getSelectedMessages();
			String shortUrl = messages[0].getHttpService().toString();
			/*
        	IRequestInfo analyzedRequest = helpers.analyzeRequest(messages[0]);
    		String url = analyzedRequest.getUrl().toString();
    		String shortUrl = url.substring(0, url.indexOf("/", 8));//���ַ����ĵ�8λ��ʼ����
    		*/
        	//��ȡϵͳ���а�
        	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        	String latestCookie = getLatestCookieFromHistory(shortUrl);
        	if(latestCookie !=null) {
        	StringSelection selection = new StringSelection(latestCookie);
        	//�����ı���ϵͳ���а�
        	clipboard.setContents(selection, null);
        	}

	      }
	}
	
	
	public class addHostToScope implements ActionListener{
		//scope matching is actually String matching!!
		private IContextMenuInvocation invocation;
		//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
		public addHostToScope(IContextMenuInvocation invocation) {
			this.invocation  = invocation;
		}
		@Override
		public void actionPerformed(ActionEvent e)
	    {
	       try{
	        	IHttpRequestResponse[] messages = invocation.getSelectedMessages();
	        	for(IHttpRequestResponse message:messages) {
	        		/*
	        		IRequestInfo analyzedRequest = helpers.analyzeRequest(message);
	        		URL fullUrl= analyzedRequest.getUrl();
	        		String host = fullUrl.getHost();
	        		int port = fullUrl.getPort();
	        		String protocol = fullUrl.getProtocol();
	        		String url = protocol+"://"+host+":"+port;
	        		*/
	        		String url = message.getHttpService().toString();
					URL shortUrl = new URL(url);
		        	//callbacks.printOutput(shortUrl.toString());
		        	callbacks.includeInScope(shortUrl);
	        	}
	        }
	        catch (Exception e1)
	        {
	            BurpExtender.this.callbacks.printError(e1.getMessage());
	        }
	    }
	}
	
	
	
	public class copyThisCookie implements ActionListener{
		private IContextMenuInvocation invocation;
		//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
		public copyThisCookie(IContextMenuInvocation invocation) {
			this.invocation  = invocation;
		}
		@Override
		public void actionPerformed(ActionEvent e)
	    {	String cookie = "";
	        try{
	        	IHttpRequestResponse[] messages = invocation.getSelectedMessages();
	        	IRequestInfo analyzedRequest = helpers.analyzeRequest(messages[0]);//ֻȡ��һ��
	        	List<String> headers = analyzedRequest.getHeaders();
	        	for(String header:headers) {
	        		if(header.toLowerCase().startsWith("cookie:")) {
	        			cookie = header;
	        		}
	        	}
	        	
	        	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	        	StringSelection selection = new StringSelection(cookie);
	        	clipboard.setContents(selection, null);
	        }
	        catch (Exception e1)
	        {
	            BurpExtender.this.callbacks.printError(e1.getMessage());
	        }
	    }
	}
	
	
	public class disMiss implements ActionListener{
		private IContextMenuInvocation invocation;
		private List<String> dismissUrls;
		//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
		public disMiss(IContextMenuInvocation invocation, List<String> dismissUrls) {
			this.invocation  = invocation;
			this.dismissUrls = dismissUrls;
		}
		@Override
		public void actionPerformed(ActionEvent e)
	    {
	       try{
	        	IHttpRequestResponse[] messages = invocation.getSelectedMessages();
	        	for(IHttpRequestResponse message:messages) {
	        		IRequestInfo analyzedRequest = helpers.analyzeRequest(message);
	        		String fullUrl= analyzedRequest.getUrl().toString();
	        		dismissUrls.add(fullUrl);
	        	}
	        }
	        catch (Exception e1)
	        {
	            BurpExtender.this.callbacks.printError(e1.getMessage());
	        }
	    }
	}

	
	public class U2C implements ActionListener{
		private IContextMenuInvocation invocation;
		
		//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
		public U2C(IContextMenuInvocation invocation) {
			this.invocation  = invocation;
		}
		@Override
		public void actionPerformed(ActionEvent e)
	    {	
			try {

				int[] indexs = invocation.getSelectionBounds();
				IHttpRequestResponse[] messages = invocation.getSelectedMessages();

				byte[] req = messages[0].getResponse();
				String selected =new String(req).substring(indexs[0], indexs[1]);
				//callbacks.printOutput(new String(req));
				//callbacks.printOutput(selected);
				
				selected = selected.replace("\"","\\\"");
                selected = Unicode.unicodeDecode(selected);
                
	        	//IRequestInfo analyzedRequest = helpers.analyzeRequest(messages[0]);//ֻȡ��һ��
				if (U2CWindowFlag == null) {
					GUIU2C window = new GUIU2C();
					window.frame.setVisible(true);
					window.u2cTextEditor.setText(selected.getBytes());
				}else {
					U2CWindowFlag.frame.setVisible(true);
					U2CWindowFlag.u2cTextEditor.setText(selected.getBytes());
				}

				
				//String newStr = new String(str.getBytes("GB2312"),"ISO-8859-1");
				
				invocation.getSelectionBounds();
			} catch (Exception ex) {
				//ex.printStackTrace();
				callbacks.printError(ex.getMessage());
			}
	    }
	}

	public class updateCookie implements ActionListener{
		private IContextMenuInvocation invocation;

		public updateCookie(IContextMenuInvocation invocation) {
			this.invocation  = invocation;
		}
		
		/* add content within the location of mouse pointer
		@Override
		public void actionPerformed(ActionEvent event) {
			
			IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
			int[] selectedBounds = invocation.getSelectionBounds();
			byte selectedInvocationContext = invocation.getInvocationContext();
		
			byte[] selectedRequest = selectedItems[0].getRequest();
			
			byte[] preSelectedPortion = Arrays.copyOfRange(selectedRequest, 0, selectedBounds[0]);//pre
			byte[] postSelectedPortion = Arrays.copyOfRange(selectedRequest, selectedBounds[1], selectedRequest.length);//post

			String shorturl = selectedItems[0].getHttpService().toString();
			String latestCookie = getLatestCookieFromHistory(shorturl);
			callbacks.printOutput(latestCookie);
			
			if (latestCookie !=null) {
				byte[] newRequestBytes = ArrayUtils.addAll(preSelectedPortion, helpers.stringToBytes(latestCookie));//pre+cookie
				newRequestBytes = ArrayUtils.addAll(newRequestBytes, postSelectedPortion);//pre+cookie+post
				
				if(selectedInvocationContext == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
					selectedItems[0].setRequest(newRequestBytes);
				}
			}
		}*/
		
		@Override
		public void actionPerformed(ActionEvent event) {
			
			IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
			byte selectedInvocationContext = invocation.getInvocationContext();
		
			byte[] selectedRequest = selectedItems[0].getRequest();
			
			
			String shorturl = selectedItems[0].getHttpService().toString();
			String latestCookie = getLatestCookieFromHistory(shorturl);
			
			if (latestCookie !=null) {
	        	IRequestInfo analyzedRequest = helpers.analyzeRequest(selectedRequest);//ֻȡ��һ��
	        	List<String> headers = analyzedRequest.getHeaders();//a bug here,report to portswigger
	        	//callbacks.printOutput(headers.toString());
	        	String cookie =null;
	        	for(String header:headers) {
	        		if(header.toLowerCase().startsWith("cookie:")) {
	        			cookie = header;
	        			break;
	        		}
	        	}
        		if(cookie !=null) {
        			int index = headers.indexOf(cookie);
        			headers.remove(index);
    	        	headers.add(index,latestCookie);
        		}else {
        			headers.add(latestCookie);
        		}
        		//callbacks.printOutput(headers.toString());
	        	
	        	
	        	byte[] body= Arrays.copyOfRange(selectedRequest, analyzedRequest.getBodyOffset(), selectedRequest.length);
	        	
	        	byte[] newRequestBytes = helpers.buildHttpMessage(headers, body);
				
				if(selectedInvocationContext == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
					selectedItems[0].setRequest(newRequestBytes);
				}
			}
		}
	}
	
	
	public class GUIU2C {
		public JFrame frame;
		public ITextEditor u2cTextEditor;
	
		/**
		 * Create the application.
		 */
		public GUIU2C() {
			initialize();
		}

		/**
		 * Initialize the contents of the frame.
		 */
		private void initialize() {
			frame = new JFrame();
			frame.setTitle("Unicode2Chinese");
			frame.setBounds(100, 100, 832, 300);
			//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent e) {
	        	U2CWindowFlag = null;
	        }
	        @Override
	        public void windowClosed(WindowEvent e) {
	        }
	    });
			
		    u2cTextEditor = callbacks.createTextEditor();
		    //u2cTextEditor.setText("xxxxx".getBytes());
		    u2cTextEditor.setEditable(false);
			frame.getContentPane().add(u2cTextEditor.getComponent(), BorderLayout.CENTER);
			U2CWindowFlag = this;//����������Ϊȫ�ֱ������ر�ʱ��Ϊnull����ֻ֤��һ�����ڱ�������
		}
	}

	public IHttpRequestResponse[] Reverse(IHttpRequestResponse[] input){
	    for (int start = 0, end = input.length - 1; start < end; start++, end--) {
	    	IHttpRequestResponse temp = input[end];
	        input[end] = input[start];
	        input[start] = temp;
	    }
	    return input;
	}
	
	public String getLatestCookieFromHistory(String shortUrl){
		//cookie��path�������޷�����������ݰ��л�ȡ��
    	//List<ICookie> cookies = callbacks.getCookieJarContents();
    	//cookie jar �е�cookie�ķ�Χȷ������Ҫ��domain��path������Ҫ��ע����ʱ�䣬��Ϊcookie jarֻ�������µġ�
    	//���ǲݷ�ʦ��˵�öԣ�ֱ�Ӵ�history���������
    	
    	IHttpRequestResponse[]  historyMessages = Reverse(callbacks.getProxyHistory());
    	//callbacks.printOutput("length of history: "+ historyMessages.length);
    	String lastestCookie =null;
    	for (IHttpRequestResponse historyMessage:historyMessages) {
    		IRequestInfo hisAnalyzedRequest = helpers.analyzeRequest(historyMessage);
    		String hisUrl = hisAnalyzedRequest.getUrl().toString();
    		//String hisShortUrl = hisUrl.substring(0, hisUrl.indexOf("/", 8));
    		String hisShortUrl = historyMessage.getHttpService().toString();
    		//callbacks.printOutput(hisShortUrl);
    		
    		if (hisShortUrl.equals(shortUrl)) {
    			List<String> hisHeaders = hisAnalyzedRequest.getHeaders();
    			for (String hisHeader:hisHeaders) {
    				if (hisHeader.toLowerCase().startsWith("cookie:")) {
    					lastestCookie = hisHeader;
            			if(lastestCookie != null) {
            				return lastestCookie;
            			}
    				}
    			}
    		}
    	}
		return null;
	}
}