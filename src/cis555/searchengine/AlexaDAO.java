package cis555.searchengine;

import java.io.File;

import cis555.searchengine.utils.DocIDAlexaRanking;
import cis555.searchengine.utils.DocIdPagerankInfo;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class AlexaDAO {
    private static EntityStore store;
    private static PrimaryIndex<String, DocIDAlexaRanking> alexaIndex;

    /**
     * Initialize all the static variables
     *
     * @param dbPath
     *            The file path of the db
     */
    public static void setup(String dbPath) {
        // Create the directory in which this store will live.
        System.out.println("Setting up AlexaDB.");
        File dir = new File(dbPath, "AlexaDB");
        if (dir.mkdirs()) {
            System.out.println("Created AlexaDB directory.");
        }

        EnvironmentConfig envConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();
        envConfig.setAllowCreate(true);
        storeConfig.setAllowCreate(true);

        Environment env = new Environment(dir, envConfig);
        store = new EntityStore(env, "alexaIndexStore", storeConfig);
        alexaIndex = store.getPrimaryIndex(String.class,
        		DocIDAlexaRanking.class);

        ShutdownHook hook = new ShutdownHook(env, store);
        Runtime.getRuntime().addShutdownHook(hook);

    }

    /**
     * Store the given pair of docID and alexa rank in the database.
     * 
     * @param docID
     * @param rank
     */
    public static void putPagerank(String docID, int rank) {
        alexaIndex.put(new DocIDAlexaRanking(docID, rank));
    }

    /**
     * Retrieve a alexa rank object from the database given its docID.
     * 
     * @param docID
     *            The primary key for the alexa rank index.
     * @return DocIdPagerankInfo instance.
     */
    public static DocIDAlexaRanking getAlexaRankObject(String docID) {
        return alexaIndex.get(docID);
    }

    /**
     * Retrieve a alexa rank from the database given its docID.
     * 
     * @param docID
     *            The primary key for the pagerankIndex.
     * @return alexa rank
     */
    public static double getAlexaRank(String docID) {
    	DocIDAlexaRanking info = alexaIndex.get(docID);
        if (info == null)
            return 0;
        return info.getRank();
    }

}
