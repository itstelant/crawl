import java.io.IOException;
import java.io.*;
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.PrintStream;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler{
	
	private String baseurl="http://stackoverflow.com";
/*
 * checks the status of the page
 * 200 means page is Online
 * any other status it means you cannot get in
 * 	
 */
	public int URLstatus (String URL){
		try{
			Connection.Response response = Jsoup.connect(URL).execute();	
			return response.statusCode();
		}catch (HttpStatusException hex){
			return hex.getStatusCode();
		}	
			catch (IOException ex)
		{
			return -1;
		}
	}

	public  void getPageLinks(String URL)throws IOException{

			int statuscode= URLstatus(URL);
	    	
			/*Check if the link is active otherwise go to next page*/
	    	if(statuscode==200){
	     	
	        	String document = Jsoup.connect(URL).get().toString();
	    		Document htmlparse=Jsoup.parse(document);
	    		
	    	    String Aref=htmlparse.body().getElementsByClass("title").toString(); 		
	            String nextpage=htmlparse.body().getElementsByClass("prev-next job-link test-pagination-next").toString();
  
	            /*
	             * Call cleanurl Function
	             */
	            cleanurl(Aref);
	              
	              //Get information for the next page of the results
	              String newurl="";
	              int hrefindex=nextpage.indexOf("href=\"")+6;
	              int endindex=nextpage.indexOf("\" title");
	              String pgurl=nextpage.substring(hrefindex, endindex);
	              newurl= baseurl+pgurl;
	  	        
	              /*
	               * If there is next page call the function and get the infor
	               */
	              if(nextpage != null){
	              	getPageLinks(newurl);
	              }
	    	}
	    	else{
	    		/*
	    		 * If the current page is down, move tot he next one
	    		 */
	    		System.out.println("Received Status,"+statuscode);
	    		if(URL.matches("(.*pg=.*)")){
	    			int pos= URL.indexOf('=')+1;
	    			int length=URL.length();
	    			String curpg=URL.substring(pos,length);
	    			int pagetoint= Integer.parseInt(curpg)+1;
	    			String nextpage=Integer.toString(pagetoint); 
	    			String replaceURL=URL.replace(curpg, nextpage);
	    			getPageLinks(replaceURL);
	    		}
	    	}
	 }
	  
	/* After retrieving the a tag from the HTML clean it up
	 * and get the Href	which will be passed to the function
	 * 
	 * String AllLinks = this string contains all 10 links of 
	 * jobs' post on the current page
     * 
     *   comlinlink.
     */
	public void cleanurl (String AllLinks) throws IOException{
	   /*
	    * This for loops thru the links and it gets only the
	    * post link.
	    * It is added with the base link and then function 
	    * 
	    * comlinks is called
	    * 
	    */
	   
	   String[] splitLinks=AllLinks.split("\n");
	   
		for (int x=0; x< splitLinks.length; x++){
				
			String ref= splitLinks[x];
			int firstpos=ref.indexOf("href=\"")+6;
			int endpos=ref.indexOf("\">");
			
			String complink=ref.substring(firstpos, endpos);
			
			comlinks(baseurl+complink);
		}
	}
	 
   /*
    * Company Link Function
    * 
    * We retrieve the Company's link from the job post. 
    * 
    * Check Scenarios:
    * -Job Post still Available? Output status
    * -No website is listed
    * -Get the company's link and call Function compemail
    */
	public void comlinks (String URL) throws IOException{
		
		int status= URLstatus(URL);
		
		/*
		 * If status is fine then get the company's website
		 * otherwise inform user
		 */
		if(status == 200){
			
			String website = Jsoup.connect(URL).get().toString();
			Document htmlparse=Jsoup.parse(website);
		
			/*
			 * Get company's website
			 */
			String compwebsite=htmlparse.body().getElementsByClass("company-home-page-url").toString();
			
			/*
			 * If no page available then record it
			 * @title= No link output the Company's name at least
			 * else 
			 * get the http... link and call the function 
			 * 
			 *  compemail to get the email
			 */
			if(compwebsite.length()==0){
			
				String title=htmlparse.head().getElementsByTag("title").toString();
				title=title.substring(7,title.indexOf("Jobs &amp"));
				compwebsite="No Website Link for "+ title;
			}
			else{
				compwebsite=compwebsite.substring(36,compwebsite.indexOf("</span"));
			}
		
			compemail(compwebsite);
		
		}
		else{
			
			System.out.println(" Page not available reason, "+ status);
		}
	}
  /*
   * Get the email if exist from the home page of the the company
   * 
   * PureDomain is the domain name of the company without https, htttp or www
   * 
   * It checks the site for href mailto to get the email.
   *  It is possible to find more than one emails, make sure the email is 
   *  company's email and it is not duplicated on the list.
   *  
   *  After getting all the information call OutputCSV to export the information
   *  in a CSV file.
   */
   public void compemail(String URL){
	   String email="";
	   String puredomain="";
	   
	   ArrayList<String> comp_email=new ArrayList<String>();
	   
	   /*
	    * If URL exist get the email
	    * else send the Company's nameS
	    */
	   if(!URL.matches("(^No Website.*)")){
	   try{
		   /*
		    * Process to get the actual domain of the company without
		    * http, https or www
		    */
		   if(URL.matches("(.*www.*)")){
			   if(URL.matches("(https.*)")){
				   puredomain=URL.substring(12,URL.length());
				   if(puredomain.indexOf('/')>-1){
					  
					   puredomain=puredomain.substring(0,puredomain.indexOf('/'));
				   }
			   }
		   
			   else{
				   puredomain=URL.substring(11,URL.length());
				   if(puredomain.indexOf('/')>-1){
						  
					   puredomain=puredomain.substring(0,puredomain.indexOf('/'));
				   }
			   }
		   }
			else if(URL.matches("(https.*)")){
			   puredomain=URL.substring(8,URL.length());
			   if(puredomain.indexOf('/')>-1){
					  
				   puredomain=puredomain.substring(0,puredomain.indexOf('/'));
			   }
		   }
		   else{
			   puredomain=URL.substring(7,URL.length());
			   
			   if(puredomain.indexOf('/')>-1){
					  
				   puredomain=puredomain.substring(0,puredomain.indexOf('/'));
			   }
		   }
		   
		   /*
		    * Get all the Hrefs from the homepage of the company and
		    * check for mailto href. 
		    * If exist get the exact email first check if exist and then add it to the list
		    * 
		    */
		    Document html=Jsoup.connect(URL).get();
		   	Elements el=html.select("a[href]");

		   	for (Element ml : el ){
		   		email=(ml.attr("abs:href"));
		   		
		   		if(email.matches("(mailto:.*)")){
		   			
		   			if(email.matches("(.*@)"+puredomain+"(.*)")){
		   				
		   				int endindex=email.indexOf(puredomain)+puredomain.length();
		   			 email=email.substring(7,endindex);
		   				if(!comp_email.contains(email)){
		   					comp_email.add(email);
		   				}
		   			}
		   		}
		   	}

		   	outputCSV(URL,comp_email);
	   
	   }catch(IOException e){
		   e.getMessage();
	   }
	   }else{
		   outputCSV(URL,comp_email);
	   }
   }
   
   /*
    * It gets the website and the email(s) which will append ont he CSV file.
    * 
    */
   public void outputCSV(String name, ArrayList<String> Email){
	   
	   int totalemails= Email.size();
	   String allemails="";
	   
	   try{
		   /*
		    * If file CompInfo is not exist create it 
		    * 
		    */
		   File outfile=new File("CompInfo.csv");
		   
		   if(!outfile.exists()){
			   	outfile.createNewFile();
		   }
	  
		   /*
		    * Open the file to write into it 
		    */
		   FileOutputStream outputfile=new FileOutputStream("CompInfo.csv",true);
		   PrintStream out=new PrintStream(outputfile,true);
	   
		   /*
		    * Loop thru the list to get the email(s)	
		    */
		   for (int i=0;i<totalemails; i++){
			   if(i!=(totalemails-1)){
				   allemails=Email.get(i);
				   allemails=allemails+" / ";   
		   }
		   else{
			 allemails=allemails+Email.get(i);
		   }
	   }
	   
	   /*
	    * If there was no email output only the link
	    * else output also the email(s)  
	    */
	   if(totalemails==0){
		   out.println(name+", N/A");
	   }else{
		   out.println(name+","+allemails);
	   }
	   
	   out.close();
	   
	   }catch(FileNotFoundException e){
		   e.printStackTrace();
	   }
	   catch(IOException ioe){
		   ioe.printStackTrace();
	   }
   }

	public static void main(String[] args) throws IOException {
			new Crawler().getPageLinks("http://stackoverflow.com/jobs/companies/");
	}
}
