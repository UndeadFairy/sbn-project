package angie;

import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import twitter4j.*;



public class configbuilder {
	
	static void sleep(long ms) {
	    try { Thread.sleep(ms); }
	    catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
	    }
	

    public static boolean testTime(Date inputDate) throws ParseException {
    	// checks if inputTime is in desired interval
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateStart = sdf.parse("2016-04-01");
        Date dateStop = sdf.parse("2016-12-05");

        if (dateStart.compareTo(inputDate) < 0) {
            //System.out.println("Date1 is after Date2");
            if (dateStop.compareTo(inputDate) > 0) {
            	return true;
            }
        }
        return false;
    }
    public static ArrayList<String> verifyPolitician(ArrayList<String> inputList, String sentiment, Twitter twitter) throws ParseException, IOException {
    	// checks if politician has any tweets in given timeframe (3200 twitter limit)
    	String tweetFileName = "src/main/resources/TwitterPoliticians" + sentiment + ".txt";
    	File tweetFile = new File(tweetFileName);
		//tweetFile.delete();
		
    	ArrayList<String> passed = new ArrayList<String>();
    	for (String al : inputList) {
			ArrayList<Status> statuses = new ArrayList<Status>();
			int pageno = 1;
	        int counter = 0;
			while(true) {
			    try {
			        System.out.println("getting tweets...");
			        int size = statuses.size(); // actual tweets count we got
			        Paging page = new Paging(pageno, 200);
			        statuses.addAll(twitter.getUserTimeline(al, page));
			        System.out.println("total got : " + statuses.size());
			        if (statuses.size() == size) { break; } // we did not get new tweets so we have done the job
			        for (Status st : statuses) {
			        	Date createdAt = st.getCreatedAt();
			        	if (testTime(createdAt)) {
			        		counter +=1;
			        	}
			        }
			        pageno++;
			        sleep(1000); // 900 rqt / 15 mn => 1 rqt/s //Every request we sleep for one second
			        }
			    
			    	
			    catch (TwitterException e) {
			        System.out.println(e.getErrorMessage());
			        }
			    }
			System.out.println(al);
			if (counter > 0) {
			    passed.add(al);
			    PrintWriter pw = new PrintWriter(new FileWriter(tweetFileName, true));
                pw.println(al);
        		pw.close();
			}
			
		}
		return passed;
    	
    }
    
    
    public static void downloadTweets (ArrayList<String> politicians, String sentiment, Twitter twitter) throws ParseException, IOException {
    	String tweetFileName = "src/main/resources/TwitterTweetData" + sentiment + ".txt";
    	//String fullTweetFileName = "src/main/resources/TwitterTweetDataFull" + sentiment + ".txt";

		File tweetFile = new File(tweetFileName);
		tweetFile.delete();
		//File tweetFileFull = new File(fullTweetFileName);
		//tweetFileFull.delete();
		
		PrintWriter pw = new PrintWriter(new FileWriter(tweetFileName, true));
		//PrintWriter pwFull = new PrintWriter(new FileWriter(fullTweetFileName, true));
        pw.println("tweet_id" + ";" + "screen_name" + ";" + "user_name" + ";" + "favorite_count" + ";" + "created_at" + ";" + "retweet_count" + ";" + "retweeted_status_user"+ ";" + "retweeted_status_id" + ";" + "tweet_text");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		for (String politician : politicians) {
			ArrayList<Status> statuses = new ArrayList<Status>();
			int pageno = 1;
			while(true) {
			    try {
			        System.out.println("getting tweets...");
			        int size = statuses.size(); // actual tweets count we got
			        Paging page = new Paging(pageno, 200);
			        statuses.addAll(twitter.getUserTimeline(politician, page));
			        System.out.println("total got : " + statuses.size());
			        if (statuses.size() == size) { break; } // we did not get new tweets so we have done the job
			        for (Status st : statuses) {
			        	Date createdAt = st.getCreatedAt();
			        	if (testTime(createdAt)) {
				        	//System.out.println(st.getUser().getName()+" : " + st.getCreatedAt() +" : " +st.getText());
			                //String json = TwitterObjectFactory.getRawJSON(st);
			                //System.out.println(json);
			        		long tweetId = st.getId();
			        		Status retweetedStatus = st.getRetweetedStatus();
			        		long retweetedStatusId = 0;
			        		String retweetedStatusUser = "foo";
			        		if (retweetedStatus != null) {
			        			retweetedStatusId = retweetedStatus.getId();
			        			retweetedStatusUser = retweetedStatus.getUser().getName();
			        		}
			        		String userName = st.getUser().getName();
			        		int favoriteCount = st.getFavoriteCount();
				        	SimpleDateFormat dateFormatPrint = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				        	String createdAtStr = dateFormatPrint.format(createdAt);
			        		int retweetCount = st.getRetweetCount();
			        		String tweetText = st.getText().replace("\n", " ");
				        	//pw.println(TwitterObjectFactory.getRawJSON(st.getUser().getName()+" : " + st.getCreatedAt() +" : " +st.getText()+ "\r\n"));
			                pw.println(tweetId + ";" + politician + ";" + userName + ";" + favoriteCount + ";" + createdAtStr + ";" + retweetCount + ";" + retweetedStatusUser + ";" + retweetedStatusId + ";" + tweetText);
				        	//pwFull.println(TwitterObjectFactory.getRawJSON(st));
			        	}
			        }
			        pageno++;
			        sleep(1000); // 900 rqt / 15 mn => 1 rqt/s //Every request we sleep for one second
			        }
			    
			    	
			    catch (TwitterException e) {
			        System.out.println(e.getErrorMessage());
			        }
			    }
		}
		pw.close();
		//pwFull.close();
    }

    public static ArrayList<String> loadPoliticians(String loadname) throws IOException {   
	    
    	String tweetFileName = "src/main/resources/" + loadname + ".txt";


    	Scanner s = new Scanner(new File(tweetFileName));
	    ArrayList<String> list = new ArrayList<String>();
	    while (s.hasNext()){
	        list.add(s.next());
	    }
	    s.close();
	    return list;
    }
    
    
	public static void main (String[] args) throws TwitterException, IOException, ParseException{
		
		ConfigurationBuilder cfg= new ConfigurationBuilder();
		cfg.setJSONStoreEnabled(true);
		cfg.setOAuthAccessToken("792417527259402240-v49BmlmHUXWGSYMze0k3Hgdls3PzIAY");
		cfg.setOAuthAccessTokenSecret("dOYrb91H8AY97awtDyNdkvJHUOB5GnkhcZxJnF1ACC60l");
		cfg.setOAuthConsumerKey("GVWdMmKZ2yxJHvaOVXQXabIKq");
		cfg.setOAuthConsumerSecret("gZnSFV2du4JHcY7T35FRi4HYM4AZagNjGsVaRaxzTmo6ETC8hF");
		cfg.setTweetModeExtended(true);
		
		TwitterFactory tf = new TwitterFactory(cfg.build());
		Twitter twitter = tf.getInstance();
//		ArrayList<String> positive_users = loadPoliticians("TwitterPoliticiansPositiveFull");
//		ArrayList<String> negative_users = loadPoliticians("TwitterPoliticiansNegativeFull");

		//ArrayList<String> positive_users = new ArrayList<String>(
			   // Arrays.asList("@zaiapresidente", "@matteosalvinimi", "@matteorenzi", "@meb", "@Giorgiolaporta", "@serracchiani", "@EnricoLetta", "@PaoloGentiloni", "@PietroGrasso", "@mariannamadia", "@pdnetwork", "@angealfa", "@emanuelefiano", "@ale_moretti", "@dariofrance", "@AlessiaMorani", "@fabriziobarca", "@graziano_delrio", "@nzingaretti", "@BeppeSala", "@giorgio_gori", "@PietroGrasso", "@lauraboldrini", "@matteorenzi", "@EnricoLetta", "@romanoprodi", "@PCPadoan", "@angealfa", "@meb", "@CarloCalenda", "@ECofficialmusic", "@graziano_delrio", "@dariofrance", "@glgalletti", "@PaoloGentiloni", "@SteGiannini", "@BeaLorenzin", "@mariannamadia", "@maumartina", "@AndreaOrlandosp", "@PCPadoan", "@robertapinotti", "@PolettiGiuliano", "@fabriziobarca", "@emmabonino", "@Cesare_Damiano", "@l_lanzillotta", "@Maurizio_Lupi", "@GioMelandri", "@RutelliTweet", "@sbonaccini", "@SergioChiampa", "@rosariocrocetta", "@VincenzoDeLuca", "@CatiusciaMarini", "@Oliverio_MarioG", "@F_Pigliaru", "@rossipresidente", "@serracchiani", "@nzingaretti", "@VeltroniWalter", "@enzo_salvi", "@LuigiBrugnaro", "@Antonio_Decaro", "@virginiomerola", "@DarioNardella", "@FlavioTosiTW", "@MCacciari44", "@pierofassino", "@giulianopisapia", "@VeltroniWalter", "@sandrogozi", "@MC_Carro", "@giannicuperlo", "@emanuelefiano", "@bobogiac", "@sandrogozi", "@guerini_lorenzo", "@AlessiaMorani", "@lapopistelli", "@LiaQuartapelle", "@AndreaRomano9", "@ivanscalfarotto", "@MarinaSereni", "@BrunoTabacci", "@VVezzali", "@enrico_zanetti", "@FinocchiaroAnna", "@sandrobondi", "@AntonioDePoli", "@bendellavedova", "@FinocchiaroAnna", "@PietroIchino", "@NenciniPsi", "@AndreaOlivero", "@LauraPuppato", "@RPBWARCHITECTS", "@MaurizioSacconi", "@DenisVerdini", "@simonabonafe", "@gualtierieurope", "@ckyenge", "@ale_moretti", "@alessiamosca", "@giannipittella", "@toiapatrizia", "@flaviozanonato", "@AnnaAscani", "@PPBaretta", "@TeresaBellanova", "@rosy_bindi", "@boccadutri", "@F_Boccia", "@FrancescoBonif1", "@meb", "@bragachiara", "@micaela_campana", "@ernestocarbone", "@MC_Carro", "@KhalidChaouki3", "@CovelloStefania", "@Cesare_Damiano", "@titti_disalvo", "@davidefaraone", "@emanuelefiano", "@dietnam", "@dariofrance", "@GiampaoloGalli", "@PaoloGentiloni", "@bobogiac", "@sandrogozi", "@GiuseppeGuerin1", "@guerini_lorenzo", "@MauroGuerraNL", "@YoramGutgeld", "@flavagno", "@LottiLuca", "@mariannamadia", "@mmauripd", "@gennaromigliore", "@AlessiaMorani", "@AndreaOrlandosp", "@DarioParrini", "@piccolapini", "@FabioPorchat", "@faustorac", "@erealacci", "@MatteoRichetti", "@AndreaRomano9", "@Ettore_Rosato", "@PablitoRossi", "@alessiarotta", "@ivanscalfarotto", "@MarinaSereni", "@itinagli", "@VicoLudovico", "@szampa56"));
	    	//	Arrays.asList("@FabioPorchat", "@faustorac", "@erealacci", "@MatteoRichetti", "@AndreaRomano9", "@Ettore_Rosato", "@PablitoRossi", "@alessiarotta", "@ivanscalfarotto", "@MarinaSereni", "@itinagli", "@VicoLudovico", "@szampa56"));

		//ArrayList<String> negative_users = new ArrayList<String>(
			   // Arrays.asList("@NichiVendola", "@robersperanza", "@ignaziomarino", "@virginiaraggi", "@beppe_grillo", "@renatobrunetta", "@SenatoreMonti", "@berlusconi", "@gasparripdl", "@mara_carfagna", "@BSaltamartini", "@GiorgiaMeloni", "@matteosalvinimi", "@carlosibilia", "@luigidimaio", "@GiuliaDiVita", "@Roberto_Fico", "@PaolaTavernaM5S", "@GiancarloCanc", "@GiuliaSarti86", "@pbersani", "@civati", "@gianfranco_fini", "@MassimoLeaderPD", "@deyoungmuseum", "@RaffaeleFitto", "@FrancoFrattini", "@GiancarloGalan", "@msgelmini", "@Ignazio_LaRussa", "@RobertoMaroni_", "@QuagliarielloG", "@_paolo_romani_", "@grotondi", "@micheleemiliano", "@GiovanniToti", "@zaiapresidente", "@r_formigoni", "@renatapolverini", "@c_appendino", "@demagistris", "@rossidoria", "@LeolucaOrlando1", "@AlemannoTW", "@DSantanche", "@DeborahBergamin", "@BiancofioreMiky", "@CalabriaTw", "@Capezzone", "@M_Fedriga", "@ale_dibattista", "@StefanoFassina", "@claudiofava1", "@NFratoianni", "@N_DeGirolamo", "@GabriGiammanco", "@guglielmopicchi", "@lauraravetto", "@elio_vito", "@MarcoFollini", "@giamma71", "@CorradinoMineo", "@LucioMalan", "@m_montevecchi", "@Ale_Mussolini_", "@AlbertoAirola", "@MBuccarella", "@GianlucaVasto", "@CatalfoNunzia", "@andrea_cioffi", "@BarbaraLezzi", "@Carlo_Martelli", "@vilmamoronese", "@NicolaMorra63", "@vitopetrocelli", "@pugliainvita", "@loscibo", "@BerniniAM", "@senantoniorazzi", "@giacomos", "@tatianabasilio1", "@SilviaBM5S", "@Bernini_P", "@AlfonsoBonafede", "@g_brescia", "@FrancBusinarolo", "@MirkoBusto", "@Azzurra_C", "@berenice0104", "@LaCastelliM5s", "@andreacecconi84", "@silviachimienti", "@TiziCip", "@AndCol81", "@VegaColonnese", "@cla_cominardi", "@Emanuela_Corda", "@crippa5stelle", "@FedericoDinca", "@F_DUva", "@DadoneFabiana", "@FedericaDaga", "@matteodallosso", "@Maxdero", "@DgPilot81", "@dellorco85", "@IvanDellaValle", "@Chiara_DiBe", "@ManlioDS", "@federicadieni", "@MFantinati", "@riccardo_fra", "@Gallinella_F", "@LuigiGallo15", "@giosilvia86", "@GiuliaGrilloM5S", "@mirellaliuzzi", "@robertalombardi", "@ManteroM5S", "@DalilaNesci", "@antoniopalmieri", "@PaoloParentela", "@fabiorampelli", "@carlaruocco1", "@JoleSantelli", "@mariaederaM5S", "@LucaSqueri", "@PatriziaTerzoni", "@AngeloTofalo", "@DaniloToninelli", "@GianlucaVacca", "@SVignaroli", "@ale_villarosa"));
			    //Arrays.asList("@GiuliaGrilloM5S", "@mirellaliuzzi", "@robertalombardi", "@ManteroM5S", "@DalilaNesci", "@antoniopalmieri", "@PaoloParentela", "@fabiorampelli", "@carlaruocco1", "@JoleSantelli", "@mariaederaM5S", "@LucaSqueri", "@PatriziaTerzoni", "@AngeloTofalo", "@DaniloToninelli", "@GianlucaVacca", "@SVignaroli", "@ale_villarosa"));

		
//		String[][] texts = new String[4][3];

		
		//ArrayList<String> positive_passed = verifyPolitician(positive_users,"positive",  twitter);
		//ArrayList<String> negative_passed = verifyPolitician(negative_users, "negative", twitter);
		
		ArrayList<String> positive_passed_loaded = loadPoliticians("TwitterPoliticianspositive");
		ArrayList<String> negative_passed_loaded = loadPoliticians("TwitterPoliticiansnegative");
		
		downloadTweets(positive_passed_loaded, "positive", twitter);
		downloadTweets(negative_passed_loaded, "negative", twitter);

	}}

